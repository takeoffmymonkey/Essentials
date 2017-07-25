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

    String mainPath;
    File mainDir;
    ListView mainList;
    File[] mainFiles;
    ArrayList<String> mainCategories = new ArrayList<String>();
    final ArrayList<String> mainCategoriesPaths = new ArrayList<String>();

    private static final int TAG_LOADER = 0;

    SimpleCursorAdapter tempAdapter;

    Cursor mCursor;

    public static SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Create db
        EssentialsDbHelper dbHelper = new EssentialsDbHelper(this);
        db = dbHelper.getReadableDatabase();


        //Get main path and files inside
        mainPath = getMainPath();
        mainDir = new File(mainPath);

        syncTags("");

        //Save paths of all files in the current dir
        mainFiles = mainDir.listFiles();
        for (File file :
                mainFiles) {
            mainCategoriesPaths.add(file.getAbsolutePath());
        }

        //add category names to mainCategories mainList
        for (int i = 0; i < mainFiles.length; i++) {
            String category = mainCategoriesPaths.get(i).substring(mainCategoriesPaths.get(i)
                    .lastIndexOf("/") + 1);
            mainCategories.add(category);
        }


        //Make list and set array adapter
        mainList = (ListView) findViewById(R.id.main_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.main_list_item,
                R.id.main_list_item_text, mainCategories);
        mainList.setAdapter(adapter);


        //Set clicklistener on list
        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("subPath", mainCategoriesPaths.get((int) id));
                view.getContext().startActivity(intent);

            }
        });


        //==================================================
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(TAG_LOADER, null, this);

        ListView tempList = (ListView) findViewById(R.id.main_temp_list);
        mCursor = getContentResolver().query(
                TagEntry.CONTENT_URI,  // The content URI of the words table
                null,
                null,                   // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                null);

        tempAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.main_list_item,
                mCursor,
                new String[]{TagEntry.COLUMN_SUGGESTION},
                new int[]{R.id.main_list_item_text},
                0
        );


        tempList.setAdapter(tempAdapter);


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

        searchView.setSuggestionsAdapter(tempAdapter);
/*        searchView.setQueryRefinementEnabled(true);
        searchView.setIconifiedByDefault(false);*/

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
                final String[] projection = {TagEntry.COLUMN_ID, TagEntry.COLUMN_SUGGESTION};
                final String sa1 = "%" + newText + "%";
                Cursor cursor = resolver.query(TagEntry.CONTENT_URI, projection, TagEntry.COLUMN_SUGGESTION + " LIKE ?",
                        new String[]{sa1}, null);

                tempAdapter.changeCursor(cursor);
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
                syncTags(mainPath);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static String getMainPath() {
        //Check if card is mount
        boolean cardMount = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (!cardMount) {
            Log.e("WARNING: ", "No sd card");
            return "Card not found";
        } else {//Card is mount
            Log.e("WARNING: ", Environment.getExternalStorageDirectory().getPath() + "/Essentials");
            return Environment.getExternalStorageDirectory().getPath() + "/Essentials";
        }
    }


    public boolean syncTags(String relativePath) {

        Log.e("WARNING: ", relativePath);


        String fullPath = mainPath + "/" + relativePath;
        File dir = new File(fullPath);
        File tagsFile = new File(fullPath, "tags.txt");

        //add tags from tags.txt to tags table
        if (tagsFile.exists()) {
            try {
                //Parse file by line
                BufferedReader br = new BufferedReader(new FileReader(tagsFile));
                String line;
                while ((line = br.readLine()) != null) {
                    //Separate fileTags name from tags and create path of this fileTags
                    String[] separated = line.split(":");
                    String name = separated[0].trim();
                    name = name.replaceAll("\uFEFF", "");
                    String tagPath = relativePath + "/" + name;

                    Log.e("WARNING: ", tagPath);

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

        //Go through all files in the dir
        if (dir.exists()) {
            //Create a table for the current folder
            createQuestionsTable(relativePath);

            //Add all its content to the table
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {//This is a dir
                    syncTags(relativePath + "/" + file.getName());
                } else {//This is a file
                    if (!file.getName().equalsIgnoreCase("tags")) {//This is a question file

                    }
                }
            }
        }

        return true;
    }


    public static String pathToTableName(String relativePath) {
        String[] locations = relativePath.split("/");
        Log.e("WARNING: ", Arrays.toString(locations));

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < locations.length; i++) {
            sb.append(locations[i].replaceAll(" ", "_"));
            sb.append("_");
        }
        sb.append("FILES");
        Log.e("WARNING: ", sb.toString());
        return sb.toString();
    }


    public static void createQuestionsTable(String relativePath) {
        String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " + pathToTableName(relativePath) + " ("
                + QuestionEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + QuestionEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + QuestionEntry.COLUMN_FOLDER + " INTEGER NOT NULL, "
                + QuestionEntry.COLUMN_QUESTION + " TEXT, "
                + QuestionEntry.COLUMN_LEVEL + " INTEGER DEFAULT 0, "
                + QuestionEntry.COLUMN_TIME + " INTEGER);";
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);
        Log.e("WARNING: ", "created table for path: " + relativePath);
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
        tempAdapter.swapCursor(data);
        Toast.makeText(this, "fds", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        tempAdapter.swapCursor(null);
    }
}
