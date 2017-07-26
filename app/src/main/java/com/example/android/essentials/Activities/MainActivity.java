package com.example.android.essentials.Activities;

import android.app.LoaderManager;
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
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

import com.example.android.essentials.EssentialsContract.QuestionEntry;
import com.example.android.essentials.EssentialsContract.TagEntry;
import com.example.android.essentials.EssentialsDbHelper;
import com.example.android.essentials.R;
import com.example.android.essentials.SearchableActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "ESSENTIALS: ";
    public static SQLiteDatabase db;
    public static String mainPath; // /storage/sdcard0/Essentials
    String currentRelativePath; //""
    String currentTableName; //FILES
    ArrayList<String> listOfDirs;
    ListView mainList;


    private static final int TAG_LOADER = 0;
    SimpleCursorAdapter suggestionsAdapter;


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
        listOfDirs = getListOfDirs(currentTableName);
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


    /*Create item_suggestions list and set adapter*/
    private void prepareSuggestions() {
        //Get cursor
        Cursor suggestionsCursor = getContentResolver().query(
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //Can be replaced with getComponentName() if this searchable activity is the current activity
        ComponentName componentName = new ComponentName(this, SearchableActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        searchView.setSubmitButtonEnabled(true);

        searchView.setSuggestionsAdapter(suggestionsAdapter);

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
                searchView.setQuery(cursor.getString(cursor.getColumnIndex(TagEntry.COLUMN_SUGGESTION)), false);
                return true;
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Bundle appData = new Bundle();
                appData.putString("path", "dasdadasdadasd"); // put extra data to Bundle
                searchView.setAppSearchData(appData); // pass the search context data
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final ContentResolver resolver = getContentResolver();
                final String[] projection = {
                        TagEntry.COLUMN_ID,
                        TagEntry.COLUMN_SUGGESTION,
                        TagEntry.COLUMN_PATH};
                final String sa1 = "%" + newText + "%";
                Cursor cursor = resolver.query(TagEntry.CONTENT_URI, projection, TagEntry.COLUMN_SUGGESTION + " LIKE ?",
                        new String[]{sa1}, null);

                suggestionsAdapter.changeCursor(cursor);
                return false;
            }
        });

        return true;
    }


    public boolean sync(String relativePath) {

        Log.e(TAG, relativePath);

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
                    contentValues.put(QuestionEntry.COLUMN_NAME, file.getName().toLowerCase());
                    contentValues.put(QuestionEntry.COLUMN_FOLDER, 1);
                    db.insert(table, null, contentValues);
                    sync(relativePath + "/" + file.getName());
                } else {//This is a file
                    if (!file.getName().equalsIgnoreCase("tags.txt")) {//This is a question file
                        contentValues.put(QuestionEntry.COLUMN_NAME, file.getName().toLowerCase());
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
                    name = name.replaceAll("\uFEFF", "").toLowerCase();
                    String tagPath = relativePath + "/" + name;
                    Log.e(TAG, tagPath);

                    //Insert question if there is one
                    String question = separated[1].trim();
                    Log.e(TAG, "QUESTION: " + question);
                    if (!question.equals("") && !question.isEmpty()) {//We have a question here
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(QuestionEntry.COLUMN_QUESTION, question);
                        int rows = db.update(table,
                                contentValues,
                                QuestionEntry.COLUMN_NAME + "=?",
                                new String[]{name});
                        Log.e(TAG, "searching for name: " + name);
                        Log.e(TAG, "changed rows: " + rows);
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
                //You'll need to add proper error handling here
            }
        }

        return true;
    }


    /*Converts relative path to name of the table with listing of current files*/
    public static String relativePathToTableName(String relativePath) {
        String[] locations = relativePath.split("/");
        Log.e(TAG, Arrays.toString(locations));

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < locations.length; i++) {
            sb.append(locations[i].replaceAll(" ", "_"));
            sb.append("_");
        }
        sb.append("FILES");
        Log.e(TAG, sb.toString());
        return sb.toString();
    }


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


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                TagEntry.CONTENT_URI,   // Provider content URI to query
                null,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        suggestionsAdapter.swapCursor(data);
        Toast.makeText(this, "fds", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        suggestionsAdapter.swapCursor(null);
    }


    /*Menu options*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_sync:
                sync(mainPath);
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
    private ArrayList<String> getListOfDirs(String currentTableName) {

        //Get cursor with only folders
        String[] projection = {QuestionEntry.COLUMN_NAME};
        String selection = QuestionEntry.COLUMN_FOLDER + "=?";
        String[] selectionArgs = {Integer.toString(1)};
        Cursor dirsCursor = db.query(
                currentTableName,
                projection,
                selection,
                selectionArgs,
                null, null, null);

        //Create list of folders
        int numberOfDirs = dirsCursor.getCount();
        ArrayList<String> listOfDirs = new ArrayList<String>();
        if (numberOfDirs > 0) {
            dirsCursor.moveToFirst();
            for (int i = 0; i < numberOfDirs; i++) {
                listOfDirs.add(dirsCursor.getString(
                        dirsCursor.getColumnIndex(QuestionEntry.COLUMN_NAME)).toUpperCase());
                dirsCursor.moveToNext();
            }
        }
        dirsCursor.close();

        return listOfDirs;
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
}
