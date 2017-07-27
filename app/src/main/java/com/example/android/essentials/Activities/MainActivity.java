package com.example.android.essentials.Activities;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.Notification;
import android.app.NotificationManager;
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
import android.os.SystemClock;
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
import android.widget.ListView;

import com.example.android.essentials.EssentialsContract.QuestionEntry;
import com.example.android.essentials.EssentialsContract.TagEntry;
import com.example.android.essentials.EssentialsDbHelper;
import com.example.android.essentials.NotificationPublisher;
import com.example.android.essentials.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static int notificationId = 0;
    private static final int TAG_LOADER = 0;
    public static final String TAG = "ESSENTIALS: ";
    public static SQLiteDatabase db;
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

        //Create db
        EssentialsDbHelper dbHelper = new EssentialsDbHelper(this);
        db = dbHelper.getReadableDatabase();

        //Get main path, set relative path and get currentTableName
        mainPath = getMainPath();
        currentRelativePath = "";
        currentTableName = relativePathToTableName(currentRelativePath);

        //Sync data
        try {
            sync(currentRelativePath);
        } catch (SQLiteException e) {
            Log.e(TAG, e.toString());
        }

        //For debugging
        testTagsTable();
        testQuestionsTable(currentRelativePath);

        //Make list of folders in the current dir and set adapter
        setListsOfFilesAndDirs(currentTableName, listOfDirs, null);
        mainList = (ListView) findViewById(R.id.main_list);
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
    public boolean sync(String relativePath) {
        String fullPath = mainPath + relativePath;
        File dir = new File(fullPath);
        File tagsFile = new File(fullPath, "tags.txt");
        String table = null;

        //Go through all files in the dir
        if (dir.exists()) {
            //Create a table for the current folder
            table = createQuestionsTable(relativePath);

            //Add all its content to the table
            File[] files = dir.listFiles();
            for (File file : files) {
                ContentValues contentValues = new ContentValues();
                if (file.isDirectory()) {//This is a dir
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
        }

        //add tags from tags.txt to tags table
        if (tagsFile.exists()) {
            try {
                //Parse file by line
                BufferedReader br = new BufferedReader(new FileReader(tagsFile));
                String line;
                while ((line = br.readLine()) != null) {
                    //Separate name, question and tags in fileTags and create path of this fileTags
                    String[] separated = line.split(":");
                    String name = separated[0].trim();
                    name = name.replaceAll("\uFEFF", "");
                    String tagPath = relativePath + "/" + name;

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

                    //Insert each tag into tags table and specify its fileTags name
                    String[] tags = separated[2].split(",");
                    for (String tag : tags) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(TagEntry.COLUMN_PATH, tagPath);
                        contentValues.put(TagEntry.COLUMN_SUGGESTION, tag);
                        getContentResolver().insert(TagEntry.CONTENT_URI, contentValues);
                    }
                }
                br.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

        return true;
    }


    /*Convert relative path to name of the table with listing of current files*/
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


    /*Create Questions table for the specified relative path*/
    public static String createQuestionsTable(String relativePath) {
        String table = relativePathToTableName(relativePath);
        String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " + table + " ("
                + QuestionEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + QuestionEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + QuestionEntry.COLUMN_FOLDER + " INTEGER NOT NULL, "
                + QuestionEntry.COLUMN_QUESTION + " TEXT, "
                + QuestionEntry.COLUMN_LEVEL + " INTEGER DEFAULT 0, "
                + QuestionEntry.COLUMN_TIME + " INTEGER);";
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);
        Log.e(TAG, "created table for path: " + relativePath + " with name: " + table);
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
                //add path of the queried file to search data
                CursorAdapter ca = searchView.getSuggestionsAdapter();
                Cursor cursor = ca.getCursor();
                ArrayList<String> paths = new ArrayList<String>();
                paths.add(cursor.getString(cursor.getColumnIndex(TagEntry.COLUMN_PATH)));
                Bundle appData = new Bundle();
                appData.putStringArrayList("paths", paths);
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
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_sync:
                sync(mainPath);
                return true;
            case R.id.action_5:
                scheduleNotification(getNotification("5 second delay"), 5000);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*Return main path (/storage/sdcard0/Essentials) */
    static String getMainPath() {
        //Check if card is mount
        boolean cardMount = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (!cardMount) {
            Log.e(TAG, "No sd card");
            return "Card not found";
        } else {//Card is mount
            Log.e(TAG, "Main path: " +
                    Environment.getExternalStorageDirectory().getPath() + "/Essentials");
            return Environment.getExternalStorageDirectory().getPath() + "/Essentials";
        }
    }


    /*Create array list of directories in the current folder*/
    static void setListsOfFilesAndDirs(
            String currentTableName,
            ArrayList<String> listOfDirs,
            @Nullable ArrayList<String> listOfFiles) {

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


    /*FOR DEBUGGING PURPOSES: show the look of FILES table for the specified relative path*/
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
            Log.e(TAG, "LEVEL: " + c1.getInt(c1.getColumnIndex(QuestionEntry.COLUMN_LEVEL)));
            Log.e(TAG, "--------------------------------------------------------");
            Log.e(TAG, "TIME: " + c1.getInt(c1.getColumnIndex(QuestionEntry.COLUMN_TIME)));
            Log.e(TAG, "--------------------------------------------------------");
            c1.moveToNext();
            Log.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
        Log.e(TAG, "========================================================");
        c1.close();
    }


    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private Notification getNotification(String question) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText("Hello");
        builder.setSmallIcon(R.drawable.ic_launcher_round);
        builder.setAutoCancel(true);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, SearchableActivity.class);
        /*The stack builder object will contain an artificial back stack for the started Activity.
        This ensures that navigating backward from the Activity leads out of your application to
        the Home screen.*/
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(SearchableActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }


    void notifyQuestion() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_round)
                        .setContentTitle("My notification My notification My notification My notification My notification My notification")
                        .setContentText("Hello World! Hello World! Hello World! Hello World! Hello World! Hello World! Hello World!");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, SearchableActivity.class);

        /*The stack builder object will contain an artificial back stack for the started Activity.
        This ensures that navigating backward from the Activity leads out of your application to
        the Home screen.*/
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(SearchableActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationId allows you to update the notification later on.
        notificationManager.notify(notificationId, mBuilder.build());

    }


}
