package com.example.android.essentials.Activities;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.android.essentials.EssentialsContract.NotificationsEntry;
import com.example.android.essentials.EssentialsContract.QuestionEntry;
import com.example.android.essentials.EssentialsContract.TagEntry;
import com.example.android.essentials.EssentialsDbHelper;
import com.example.android.essentials.NotificationPublisher;
import com.example.android.essentials.R;
import com.example.android.essentials.Schedule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    public static Context context;
    private static final int TAG_LOADER = 0;
    public static final String TAG = "ESSENTIALS: ";
    private EssentialsDbHelper dbHelper;
    private static SQLiteDatabase db;
    public static String mainPath; // /storage/sdcard0/Essentials
    String currentRelativePath; //""
    String currentTableName; //FILES
    ArrayList<String> listOfDirs = new ArrayList<String>();
    ListView mainList;
    static Cursor suggestionsCursor;
    static SimpleCursorAdapter suggestionsAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Update context
        MainActivity.context = getApplicationContext();

        //Create db
        dbHelper = new EssentialsDbHelper(this);
        db = dbHelper.getReadableDatabase();

        //Get main relativePath, set relative relativePath and get currentTableName
        mainPath = getMainPath();
        currentRelativePath = "";
        currentTableName = relativePathToTableName(currentRelativePath);

        //Sync data
        //sync(currentRelativePath);

        //For debugging
        testTagsTable();
        testQuestionsTable(currentRelativePath);


        //Empty
        Button resync = (Button) findViewById(R.id.resync);
        View emptyView = findViewById(R.id.main_empty_view);
        resync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync(currentRelativePath);
                recreate();
            }
        });

        //Make list of folders in the current dir and set adapter
        setListsOfFilesAndDirs(currentTableName, listOfDirs, null, db);
        mainList = (ListView) findViewById(R.id.main_list);
        mainList.setEmptyView(emptyView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_main_list,
                R.id.main_list_item_text, listOfDirs);
        mainList.setAdapter(adapter);

        //Set clicklistener on list
        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("subPath", mainPath + "/" + listOfDirs.get((int) id));
                view.getContext().startActivity(intent);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(TAG_LOADER, null, this);

        //Create item_suggestions list and set adapder
        prepareSuggestions();

    }


    /*Create TAG table with all tags and create all Question tables*/
    public static boolean sync(String relativePath) {
        Log.e(TAG, "request for sync");
        String fullPath = mainPath + relativePath;
        File dir = new File(fullPath);
        File tagsFile = new File(fullPath, "tags.txt");
        String table = null;
        boolean resyncing = false;

        //Go through all files in the dir
        Log.e(TAG, "dir.exists(): " + dir.exists());
        if (dir.exists()) {
            try {//Create a table for the current folder
                table = createQuestionsTable(relativePath);
            } catch (SQLiteException e) { //Table already exists
                Log.e(TAG, e.toString());
                table = relativePathToTableName(relativePath);
                resyncing = true;
            }

            //We are in fresh syncing mode
            if (!resyncing) {
                //Add all its content to the table
                File[] files = dir.listFiles();
                for (File file : files) {
                    ContentValues contentValues = new ContentValues();
                    if (file.isDirectory() && !file.getName().endsWith(".files")) {//This is a dir
                        contentValues.put(QuestionEntry.COLUMN_NAME, file.getName());
                        contentValues.put(QuestionEntry.COLUMN_FOLDER, 1);
                        db.insert(table, null, contentValues);
                        sync(relativePath + "/" + file.getName());
                    } else {//This is a file
                        if (!file.getName().equalsIgnoreCase("tags.txt")) {//This is a question file
                            contentValues.put(QuestionEntry.COLUMN_NAME, file.getName());
                            contentValues.put(QuestionEntry.COLUMN_FOLDER, 0);
                            db.insert(table, null, contentValues);
                        }
                    }
                }
            } else {//We are in resyncing mode
                Log.e(TAG, "we are resyncing");
                //Get new file listing and separate files from dirs
                File[] newFiles = dir.listFiles();
                ArrayList<File> newDirsList = new ArrayList<File>();
                ArrayList<File> newFilesList = new ArrayList<File>();
                for (File file : newFiles) {
                    if (file.isDirectory() && !file.getName().endsWith(".files")) {//It's a dir
                        newDirsList.add(file);
                        sync(relativePath + "/" + file.getName());
                    } else if (file.isFile() && !file.getName().equalsIgnoreCase("tags.txt")) {//A file
                        newFilesList.add(file);
                    }
                }

                //Get old file listing and separate files from dirs
                String[] projection = {QuestionEntry.COLUMN_NAME, QuestionEntry.COLUMN_FOLDER};
                Cursor c = db.query(table, projection, null, null, null, null, null);
                int rows = c.getCount();
                ArrayList<String> oldDirsList = new ArrayList<>();
                ArrayList<String> oldFilesList = new ArrayList<>();
                if (rows > 0) {
                    c.moveToFirst();
                    for (int i = 0; i < rows; i++) {
                        int folder = c.getInt(c.getColumnIndex(QuestionEntry.COLUMN_FOLDER));
                        String name = c.getString(c.getColumnIndex(QuestionEntry.COLUMN_NAME));
                        if (folder == 1) {//This is a folder
                            oldDirsList.add(name);
                        } else {//It's a file
                            oldFilesList.add(name);
                        }
                        c.moveToNext();
                    }
                }
                c.close();

                //Now compare old and new files
                //Check if dir is in list
                for (File file : newDirsList) {
                    boolean found = false;
                    String search = file.getName();
                    for (int i = 0; i < oldDirsList.size(); i++) {
                        if (search.equalsIgnoreCase(oldDirsList.get(i))) {
                            found = true;
                            Log.e(TAG, "sync(): found same dir in oldDirs: " + search);
                            //Remove it from old list
                            oldDirsList.remove(i);
                            break;
                        }
                    }
                    if (!found) {//Dir wasn't found
                        Log.e(TAG, "sync(): couldn't find same dir in oldDirs: " + search);
                        //Add new row to the table
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(QuestionEntry.COLUMN_NAME, search);
                        contentValues.put(QuestionEntry.COLUMN_FOLDER, 1);
                        long r = db.insert(table, null, contentValues);
                        Log.e(TAG, "sync(): adding new row: " + r);
                    }
                }
                //Same for files: check if dir is in list
                for (File file : newFilesList) {
                    boolean found = false;
                    String search = file.getName();
                    for (int i = 0; i < oldFilesList.size(); i++) {
                        if (search.equalsIgnoreCase(oldFilesList.get(i))) {
                            found = true;
                            Log.e(TAG, "sync(): found same file in oldFiles: " + search);
                            //Remove it from old list
                            oldFilesList.remove(i);
                            break;
                        }
                    }
                    if (!found) {//File wasn't found
                        Log.e(TAG, "sync(): couldn't find same file in oldFiles: " + search);
                        //Add new row to the table
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(QuestionEntry.COLUMN_NAME, search);
                        contentValues.put(QuestionEntry.COLUMN_FOLDER, 0);
                        long r = db.insert(table, null, contentValues);
                        Log.e(TAG, "sync(): adding new row: " + r);
                    }
                }

                //Now we have 2 lists of old dirs and old files, remove all references of it
                //Delete dirs references
                for (String oldDir : oldDirsList) {
                    //Remove from the table
                    String selection = QuestionEntry.COLUMN_NAME + "=?";
                    String[] selectionArgs = {oldDir};
                    long r = db.delete(table, selection, selectionArgs);
                    Log.e(TAG, "sync(): deleting dir from question table: " + r);
                    //Remove from tags table
                    String dirRelativePath = relativePath + "/" + oldDir;
                    String selection2 = TagEntry.COLUMN_PATH + "=?";
                    String[] selectionArgs2 = {dirRelativePath};
                    long r2 = db.delete(TagEntry.TABLE_NAME, selection2, selectionArgs2);
                    Log.e(TAG, "sync(): deleting dir from tags table: " + r2);
                }
                //Delete files references
                for (String oldFile : oldFilesList) {
                    //Remove from the table
                    String selection = QuestionEntry.COLUMN_NAME + "=?";
                    String[] selectionArgs = {oldFile};
                    long r = db.delete(table, selection, selectionArgs);
                    Log.e(TAG, "sync(): deleting file from question table: " + r);
                    //Remove from tags table
                    String fileRelativePath = relativePath + "/" + oldFile;
                    String selection2 = TagEntry.COLUMN_PATH + "=?";
                    String[] selectionArgs2 = {fileRelativePath};
                    long r2 = db.delete(TagEntry.TABLE_NAME, selection2, selectionArgs2);
                    Log.e(TAG, "sync(): deleting file from tags table: " + r2);
                    //Remove from notifications table
                    String selection3 = NotificationsEntry.COLUMN_RELATIVE_PATH + "=?";
                    long r3 = db.delete(NotificationsEntry.TABLE_NAME, selection3, selectionArgs2);
                    Log.e(TAG, "sync(): deleting file from notification table: " + r3);
                }
            }

            //add tags from tags.txt to tags table
            if (tagsFile.exists()) {
                try {
                    //Parse file by line
                    BufferedReader br = new BufferedReader(new FileReader(tagsFile));
                    String line;
                    while ((line = br.readLine()) != null) {
                        //Separate name, question and tags in fileTags and create relativePath of this fileTags
                        String[] separated = line.split(":");
                        String name = separated[0].trim();
                        name = name.replaceAll("\uFEFF", "");
                        String tagPath = relativePath + "/" + name;

                        //Check if file exists
                        File file = new File(mainPath + tagPath);
                        Log.e(TAG, "file: " + file.getAbsolutePath() + " exists: " + file.exists());
                        if (file.exists()) {
                            //Insert question if there is one
                            String question = separated[1].trim();
                            if (!question.equalsIgnoreCase("") && !question.isEmpty()) {//We have a question here
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(QuestionEntry.COLUMN_QUESTION, question);
                                db.update(table,
                                        contentValues,
                                        QuestionEntry.COLUMN_NAME + "=?",
                                        new String[]{name});
                            }

                            //Check if current path is already in the tags table
                            String[] projection = {TagEntry.COLUMN_ID};
                            String selection = TagEntry.COLUMN_PATH + "=?";
                            String[] selectionArgs = {tagPath};
                            Cursor c = db.query(TagEntry.TABLE_NAME,
                                    projection,
                                    selection,
                                    selectionArgs,
                                    null, null, null);
                            int rows = c.getCount();
                            if (rows > 0) { //There are rows with such path
                                c.moveToFirst();
                                for (int i = 0; i < rows; i++) {//Delete all rows
                                    int id = c.getInt(c.getColumnIndex(TagEntry.COLUMN_ID));
                                    String selection2 = NotificationsEntry.COLUMN_ID + "=?";
                                    String[] selectionArgs2 = {Integer.toString(id)};
                                    long r = db.delete(TagEntry.TABLE_NAME, selection2, selectionArgs2);
                                    Log.e(TAG, "deleting previous tag for path: " + tagPath + " response: " + r);
                                    c.moveToNext();
                                }
                            }
                            c.close();

                            //Insert each tag into tags table and specify its fileTags name
                            String[] tags = separated[2].split(",");
                            for (String tag : tags) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(TagEntry.COLUMN_PATH, tagPath);
                                contentValues.put(TagEntry.COLUMN_SUGGESTION, tag);
                                db.insert(TagEntry.TABLE_NAME, null, contentValues);
                                //getContentResolver().insert(TagEntry.CONTENT_URI, contentValues);
                            }
                        }
                    }
                    br.close();
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
        return true;
    }


    /*Convert relative relativePath to name of the table with listing of current files*/

    public static String relativePathToTableName(String relativePath) {
        Log.e(TAG, "relativePathToTableName received: " + relativePath);
        //Path should start with /
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }
        String[] locations = relativePath.split("/");
        Log.e(TAG, "relativePathToTableName made array: " + Arrays.toString(locations));
        Log.e(TAG, Arrays.toString(locations));
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < locations.length; i++) {
            sb.append(locations[i].replaceAll(" ", "_"));
            sb.append("_");
        }
        sb.append("FILES");
        Log.e(TAG, "relativePathToTableName return: " + sb.toString());
        return sb.toString();
    }


    /*Create Questions table for the specified relative relativePath*/
    public static String createQuestionsTable(String relativePath) {
        String table = relativePathToTableName(relativePath);
        String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " + table + " ("
                + QuestionEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + QuestionEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + QuestionEntry.COLUMN_FOLDER + " INTEGER NOT NULL, "
                + QuestionEntry.COLUMN_QUESTION + " TEXT);";
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);
        Log.e(TAG, "created table for relativePath: " + relativePath + " with name: " + table);
        return table;
    }


    /*Create item_suggestions list and set adapter*/
    private void prepareSuggestions() {
        //Get cursor
        suggestionsCursor = getContentResolver().query(
                TagEntry.CONTENT_URI,
                null,
                null,                   // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                null);

        //Create adapter
        suggestionsAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.item_suggestions,
                suggestionsCursor,
                new String[]{TagEntry.COLUMN_SUGGESTION, TagEntry.COLUMN_PATH},
                new int[]{R.id.item_suggestions_text1, R.id.item_suggestions_text2},
                0
        );

        suggestionsCursor.close();

    }


    /*Create menu*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Create menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //Set up searchView menu item and adapter
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(this, SearchableActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        searchView.setSubmitButtonEnabled(true);
        searchView.setSuggestionsAdapter(suggestionsAdapter);

        //set OnSuggestionListener
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                // Add clicked text to search box
                CursorAdapter ca = searchView.getSuggestionsAdapter();
                Cursor cursor = ca.getCursor();
                cursor.moveToPosition(position);
                searchView.setQuery(cursor.getString(cursor.getColumnIndex
                        (TagEntry.COLUMN_SUGGESTION)), true);
                return true;
            }
        });

        //set OnQueryTextListener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //add relativePath of the queried file to search data
                CursorAdapter ca = searchView.getSuggestionsAdapter();
                Cursor cursor = ca.getCursor();
                ArrayList<String> paths = new ArrayList<String>();
                paths.add(cursor.getString(cursor.getColumnIndex(TagEntry.COLUMN_PATH)));
                Bundle appData = new Bundle();
                appData.putStringArrayList("relativePaths", paths);
                searchView.setAppSearchData(appData);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Update cursor on typing
                final ContentResolver resolver = getContentResolver();
                final String[] projection = {
                        TagEntry.COLUMN_ID,
                        TagEntry.COLUMN_SUGGESTION,
                        TagEntry.COLUMN_PATH};
                final String sa1 = "%" + newText + "%";
                Cursor cursor = resolver.query(
                        TagEntry.CONTENT_URI,
                        projection,
                        TagEntry.COLUMN_SUGGESTION + " LIKE ?",
                        new String[]{sa1},
                        null);
                suggestionsAdapter.changeCursor(cursor);
                return false;
            }
        });

        return true;
    }


    /*Menu options*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_sync:
                sync(currentRelativePath);
                recreate();
                return true;
            case R.id.action_restart_notifications:
                rescheduleNotifications();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*Return main relativePath (/storage/sdcard0/Essentials) */
    static String getMainPath() {
        //Check if card is mount
        boolean cardMount = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (!cardMount) {
            Log.e(TAG, "No sd card");
            return "Card not found";
        } else {//Card is mount
            Log.e(TAG, "Main relativePath: " +
                    Environment.getExternalStorageDirectory().getPath() + "/Essentials");
            return Environment.getExternalStorageDirectory().getPath() + "/Essentials";
        }
    }


    /*Create array list of directories in the current folder*/
    static void setListsOfFilesAndDirs(
            String currentTableName,
            ArrayList<String> listOfDirs,
            @Nullable ArrayList<String> listOfFiles, SQLiteDatabase db) {

        //Create cursor based on whether only dirs are need or files too
        String[] projection = {QuestionEntry.COLUMN_NAME, QuestionEntry.COLUMN_FOLDER};

        Cursor cursor = db.query(
                currentTableName,
                projection,
                null,
                null,
                null, null, null);

        //Add files and folders to corresponding array lists
        int numberOfRows = cursor.getCount();
        if (numberOfRows > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < numberOfRows; i++) {
                int folder = cursor.getInt(cursor.getColumnIndex(QuestionEntry.COLUMN_FOLDER));
                String name = cursor.getString(cursor.getColumnIndex(QuestionEntry.COLUMN_NAME));
                if (folder == 1) {//This is a folder
                    Log.e(TAG, "!!name = " + name);
                    Log.e(TAG, "!!listOfDirs = " + listOfDirs);
                    listOfDirs.add(name);
                } else if (folder == 0 && listOfFiles != null && !name.equalsIgnoreCase("tags.txt")) {
                    //this is file
                    listOfFiles.add(name);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
    }


    /*Instantiate and return a new Loader for the given ID*/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                TagEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }


    /*Called when a previously created loader has finished its load*/
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Refresh cursor
        suggestionsAdapter.swapCursor(data);
    }


    /*Called when a previously created loader is being reset, and thus making its data unavailable*/
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        suggestionsAdapter.swapCursor(null);
    }


    /*FOR DEBUGGING PURPOSES: show the look of TAGS table*/
    static void testTagsTable() {
        Cursor c = db.query(TagEntry.TABLE_NAME, null, null, null, null, null, null);
        Log.e(TAG, "========================================================");
        Log.e(TAG, "TAGS TABLE");
        Log.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            Log.e(TAG, "SUGG: " + c.getString(c.getColumnIndex(TagEntry.COLUMN_SUGGESTION)));
            Log.e(TAG, "--------------------------------------------------------");
            Log.e(TAG, "PATH: " + c.getString(c.getColumnIndex(TagEntry.COLUMN_PATH)));
            Log.e(TAG, "--------------------------------------------------------");
            c.moveToNext();
            Log.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
        Log.e(TAG, "========================================================");
        c.close();

    }


    /*FOR DEBUGGING PURPOSES: show the look of FILES table for the specified relative relativePath*/
    static void testQuestionsTable(String relativePath) {

        Cursor c1 = db.query(relativePathToTableName(relativePath), null, null, null, null, null, null);
        Log.e(TAG, "========================================================");
        Log.e(TAG, "QUESTIONS TABLE");
        Log.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        c1.moveToFirst();
        for (int i = 0; i < c1.getCount(); i++) {
            Log.e(TAG, "NAME: " + c1.getString(c1.getColumnIndex(QuestionEntry.COLUMN_NAME)));
            Log.e(TAG, "--------------------------------------------------------");
            Log.e(TAG, "FOLDER: " + c1.getInt(c1.getColumnIndex(QuestionEntry.COLUMN_FOLDER)));
            Log.e(TAG, "--------------------------------------------------------");
            Log.e(TAG, "QUESTION: " + c1.getString(c1.getColumnIndex(QuestionEntry.COLUMN_QUESTION)));
            Log.e(TAG, "--------------------------------------------------------");
            c1.moveToNext();
            Log.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
        Log.e(TAG, "========================================================");
        c1.close();
    }


    /*FOR DEBUGGING PURPOSES: show the look of FILES table for the specified relative relativePath*/
    public static void testNotificationTable() {

        Cursor c1 = db.query(NotificationsEntry.TABLE_NAME, null, null, null, null, null, null);
        Log.e(TAG, "========================================================");
        Log.e(TAG, "NOTIFICATION TABLE");
        Log.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        c1.moveToFirst();
        for (int i = 0; i < c1.getCount(); i++) {
            Log.e(TAG, "ID: " + c1.getInt(c1.getColumnIndex(NotificationsEntry.COLUMN_ID)));
            Log.e(TAG, "--------------------------------------------------------");
            Log.e(TAG, "QUESTION: " + c1.getString(c1.getColumnIndex(NotificationsEntry.COLUMN_QUESTION)));
            Log.e(TAG, "--------------------------------------------------------");
            Log.e(TAG, "RELATIVE_PATH: " + c1.getString(c1.getColumnIndex(NotificationsEntry.COLUMN_RELATIVE_PATH)));
            Log.e(TAG, "--------------------------------------------------------");
            Log.e(TAG, "TIME_EDITED: " + c1.getString(c1.getColumnIndex(NotificationsEntry.COLUMN_TIME_EDITED)));
            Log.e(TAG, "--------------------------------------------------------");
            Log.e(TAG, "LEVEL: " + c1.getInt(c1.getColumnIndex(NotificationsEntry.COLUMN_LEVEL)));
            Log.e(TAG, "--------------------------------------------------------");
            c1.moveToNext();
            Log.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
        Log.e(TAG, "========================================================");
        c1.close();
    }

    public static void scheduleNotification(long id, String question, int level,
                                            Notification notification, long delay) {

        //Create intent and add resulting notification in it
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        notificationIntent.putExtra(NotificationPublisher.QUESTION, question);
        notificationIntent.putExtra(NotificationPublisher.QUESTION_LEVEL, level);

        //Set time delay and alarm + pending intent
        long futureInMillis = System.currentTimeMillis() + delay;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) id, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //If there is already an alarm scheduled for the same IntentSender, that previous
        //alarm will first be canceled.
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);

        Log.e(TAG, "scheduleNotification(): setting alarm for id: " + id + " at: " + new Date(futureInMillis));
        Log.e(TAG, "which is now with delay of seconds: " + TimeUnit.MILLISECONDS.toSeconds(delay));

    }


    public static Notification getNotification(String question, String relativePath) {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, SearchableActivity.class);
        resultIntent.putExtra("relativePath", relativePath);

        //Make artificial back stack to go back to Home screen on back passed
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(SearchableActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        resultIntent.hashCode(),
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(question);
        builder.setContentText(relativePath);
        builder.setSmallIcon(R.drawable.ic_launcher_round);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setContentIntent(resultPendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(relativePath));
        builder.setPriority(2);

        return builder.build();
    }


    public static String getLastValueOfPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }


    public static String getRelativePathFromFull(String fullPath) {
        String relativePath = fullPath.substring(getMainPath().length(), fullPath.length());
        Log.e(TAG, "getRelativePathFromFull received: " + fullPath);
        Log.e(TAG, "getRelativePathFromFull return: " + relativePath);
        return relativePath;
    }


    public static String getRelativePathOfDirForFile(String fileFullPath) {
        File file = new File(fileFullPath);
        if (file.isFile()) {
            String relativeFilePath = MainActivity.getRelativePathFromFull(fileFullPath);
            String relativeDirPath = relativeFilePath.substring(0,
                    relativeFilePath.lastIndexOf("/") + 1);
            Log.e(TAG, "getRelativePathOfDirForFile received: " + fileFullPath);
            Log.e(TAG, "getRelativePathOfDirForFile return: " + relativeDirPath);
            return relativeDirPath;
        } else {
            return "No file found at specified path";
        }
    }

    public static void rescheduleNotifications() {
        int id;
        String question;
        String relativePath;
        int level;
        long timeEdited;

        Log.e(TAG, "rescheduleNotifications()" + new Date(System.currentTimeMillis()));

        //Parse table
        Cursor c = db.query(NotificationsEntry.TABLE_NAME, null, null, null, null, null, null);
        int rows = c.getCount();
        if (rows > 0) {//Should be at least 1 row
            c.moveToFirst();
            for (int i = 0; i < rows; i++) {
                id = c.getInt(c.getColumnIndex(NotificationsEntry.COLUMN_ID));
                question = c.getString(c.getColumnIndex(NotificationsEntry.COLUMN_QUESTION));
                relativePath = c.getString(c.getColumnIndex(NotificationsEntry.COLUMN_RELATIVE_PATH));
                timeEdited = c.getLong(c.getColumnIndex(NotificationsEntry.COLUMN_TIME_EDITED));
                level = c.getInt(c.getColumnIndex(NotificationsEntry.COLUMN_LEVEL));
                long timeToAlarm = timeEdited + Schedule.getDelayByLevel(level);
                long currentTime = System.currentTimeMillis();

                //check if alarm is expired
                if (currentTime > timeToAlarm) {//alarm is expired, need to notify immediately
                    //Create notification
                    Log.e(TAG, "rescheduling with 1 sec delay expired notification with id: " + id);

                    MainActivity.scheduleNotification(id, question, level,
                            MainActivity.getNotification(question, relativePath), 5000);
                } else { // alarm is not expired yet
                    long newDelay = timeToAlarm - currentTime;
                    Log.e(TAG, "rescheduling active notification with id: " + id +
                            " with updated new delay of seconds: "
                            + TimeUnit.MILLISECONDS.toSeconds(newDelay));
                    MainActivity.scheduleNotification(id, question, level,
                            MainActivity.getNotification(question, relativePath),
                            newDelay);
                }
                c.moveToNext();
            }
        }
        c.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
