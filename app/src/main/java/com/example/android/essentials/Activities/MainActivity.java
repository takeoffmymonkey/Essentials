package com.example.android.essentials.Activities;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
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
import com.example.android.essentials.R;
import com.example.android.essentials.SearchableActivity;

import java.io.File;
import java.util.ArrayList;

import static android.R.attr.version;
import static com.example.android.essentials.EssentialsContract.QuestionEntry.COLUMN_ID;
import static com.example.android.essentials.EssentialsContract.QuestionEntry.COLUMN_QUESTION;
import static com.example.android.essentials.EssentialsContract.QuestionEntry.CONTENT_URI;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    String mainPath;
    File mainDir;
    ListView mainList;
    File[] mainFiles;
    ArrayList<String> mainCategories = new ArrayList<String>();
    final ArrayList<String> mainCategoriesPaths = new ArrayList<String>();

    private static final int QUESTION_LOADER = 0;

    SimpleCursorAdapter tempAdapter;

    Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Get main path and files inside
        mainPath = getMainPath();
        mainDir = new File(mainPath);


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
        getLoaderManager().initLoader(QUESTION_LOADER, null, this);

        ListView tempList = (ListView) findViewById(R.id.main_temp_list);
        mCursor = getContentResolver().query(
                CONTENT_URI,  // The content URI of the words table
                null,
                null,                   // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                null);

        tempAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.main_list_item,
                mCursor,
                new String[]{COLUMN_QUESTION},
                new int[]{R.id.main_list_item_text},
                0
        );


        tempList.setAdapter(tempAdapter);

        //==============================================
        /*Cursor mCursor = getContentResolver().query(
                CONTENT_URI,  // The content URI of the words table
                null,                       // The columns to return for each row
                null,                   // Either null, or the word the user entered
                null,                    // Either empty, or the string the user entered
                null);                       // The sort order for the returned rows


        if (null == mCursor) {
            Log.e("WARNING: ", "cursor is null");
        } else if (mCursor.getCount() < 1) {//Cursor is empty, no matches
            Toast.makeText(this, "nothing is found", Toast.LENGTH_SHORT).show();
        } else {//Found results
            Toast.makeText(this, "Cursor has " + mCursor.getCount() + " items", Toast.LENGTH_SHORT).show();
        }*/


    }


    /*Create menu*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //Can be replaced with getComponentName()
        //if this searchable activity is the current activity
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
                searchView.setQuery(cursor.getString(cursor.getColumnIndex(COLUMN_QUESTION)),true);
                return true;
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final ContentResolver resolver = getContentResolver();
                final String[] projection = {COLUMN_ID, COLUMN_QUESTION};
                final String sa1 = "%"+newText+"%"; // contains an "A"
                Cursor cursor = resolver.query(CONTENT_URI, projection, COLUMN_QUESTION + " LIKE ?",
                        new String[] { sa1 }, null);

               /* Cursor cursor = db.query(LOG_TABLE_NAME,
                        new String[]{LOG_VERSION_COLUMN},
                        LOG_VERSION_COLUMN + "=?", new String[]{Integer.toString(version)},
                        null, null, null);*/


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
            return Environment.getExternalStorageDirectory().getPath() + "/Essentials";
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.


        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                QuestionEntry.CONTENT_URI,   // Provider content URI to query
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
