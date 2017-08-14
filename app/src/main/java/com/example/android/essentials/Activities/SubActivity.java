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
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.example.android.essentials.Adapters.ExpandableDirsAdapter;
import com.example.android.essentials.Adapters.ExpandableListAdapter;
import com.example.android.essentials.Adapters.ExpandableNavAdapter;
import com.example.android.essentials.EssentialsContract.QuestionEntry;
import com.example.android.essentials.EssentialsContract.TagEntry;
import com.example.android.essentials.Question;
import com.example.android.essentials.R;
import com.example.android.essentials.Settings;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.android.essentials.Activities.MainActivity.rescheduleNotifications;
import static com.example.android.essentials.Activities.MainActivity.suggestionsAdapter;
import static com.example.android.essentials.Activities.MainActivity.suggestionsCursor;

public class SubActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TAG_LOADER = 0;
    String mainPath;
    String subPath;
    String subRelativePath;
    String subActivityName;
    String subTableName;
    ArrayList<String> subListOfDirs = new ArrayList<String>();
    ArrayList<String> subListOfFiles = new ArrayList<String>();
    ExpandableListView subExpList;
    ExpandableListView subExpNav;
    ExpandableListView subExpDirs;
    ArrayList<Question> questions = new ArrayList<Question>();
    ExpandableListAdapter subExpListAdapter;
    ExpandableNavAdapter subExpNavAdapter;
    ExpandableDirsAdapter subExpDirsAdapter;
    String[] subPathArray;
    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        actionBar = getSupportActionBar();

        //Get and set main and current dir's full and relative relativePaths
        mainPath = MainActivity.getMainPath();
        subPath = getIntent().getStringExtra("subPath");
        subRelativePath = "/" + subPath.substring(mainPath.length() + 1);

        //Set subActivity name
        subActivityName = subRelativePath.substring(subRelativePath.lastIndexOf("/") + 1);
        setTitle(subActivityName);

        //Get subTableName
        subTableName = MainActivity.relativePathToTableName(subRelativePath);

        //Create separate arrays for files and dirs in the current relativePath
        MainActivity.setListsOfFilesAndDirs(subTableName, subListOfDirs, subListOfFiles);

        //Make expandable list and set adapter
        subExpList = (ExpandableListView) findViewById(R.id.sub_exp_list);
        prepareQuestionsList();
        subExpListAdapter = new ExpandableListAdapter(this, questions);
        subExpList.setAdapter(subExpListAdapter);

        //Make expandable navigator
        subExpNav = (ExpandableListView) findViewById(R.id.sub_exp_navigate);
        prepareNavData();
        subExpNavAdapter = new ExpandableNavAdapter(this, subPathArray);
        subExpNav.setAdapter(subExpNavAdapter);

        //Set click listener on navigation exp list
        subExpNav.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                navigateToActivity(v, id);
                return false;
            }
        });

        //Make expandable dirs list
        subExpDirs = (ExpandableListView) findViewById(R.id.sub_exp_dirs);
        subExpDirsAdapter = new ExpandableDirsAdapter(this, subListOfDirs);
        subExpDirs.setAdapter(subExpDirsAdapter);
        for (int i = 0; i < subExpDirsAdapter.getGroupCount(); i++)
            subExpDirs.expandGroup(i);

        //Expand list if it is the only question and no folders
        if (subExpListAdapter.getGroupCount() == 1 && subExpDirsAdapter.getChildrenCount(0) == 0)
            subExpList.expandGroup(0);


        //Set click listener on navigation exp list
        subExpDirs.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(SubActivity.this, SubActivity.class);
                intent.putExtra("subPath", mainPath +
                        subRelativePath + "/" + subListOfDirs.get((int) id));
                //Turn on bars
                Settings.setListsVisibility(1);
                v.getContext().startActivity(intent);
                return false;
            }
        });

        //Set visibility of lists
        if (Settings.getListsVisibility() == 1) {//Lists should be visible
            subExpNav.setVisibility(View.VISIBLE);
            subExpDirs.setVisibility(View.VISIBLE);
            actionBar.show();
        } else if (Settings.getListsVisibility() == 0) {//Lists are hidden
            subExpNav.setVisibility(View.GONE);
            subExpDirs.setVisibility(View.GONE);
            actionBar.hide();
        }
        if (subListOfDirs.size() == 0) {//hide dirs if there are none
            subExpDirs.setVisibility(View.GONE);
        }

        //Enable back option
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(MainActivity.TAG, e.toString());
        }

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(TAG_LOADER, null, this);

        //Create item_suggestions list and set adapder
        prepareSuggestions();
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
                //Not sure I need to close this one
                cursor.close();
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
            case R.id.action_hide_lists:
                showHideExpLists();
                return true;
            case R.id.action_sound_mode:
                int currentMode = Settings.getSoundMode();
                if (currentMode < 2) {
                    Settings.setSoundMode(currentMode + 1);
                } else if (currentMode == 2) {
                    Settings.setSoundMode(0);
                }
                return true;
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_sync:
                MainActivity.sync(subRelativePath);
                recreate();
                return true;
            case R.id.action_restart_notifications:
                rescheduleNotifications();
                return true;
            case R.id.action_notification_mode:
                int currentNotificationMode = Settings.getNotificationMode();
                if (currentNotificationMode == 0) {
                    Settings.setNotificationMode(1);
                } else if (currentNotificationMode == 1) {
                    Settings.setNotificationMode(0);
                }
                rescheduleNotifications();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*Prepare questions list for adapter*/
    private void prepareQuestionsList() {
        for (int i = 0; i < subListOfFiles.size(); i++) {
            //Get relativePath of the question
            String name = subListOfFiles.get(i);
            String path = mainPath + subRelativePath + "/" + name;

            //Rename question if it has question text provided
            String[] projection = {QuestionEntry.COLUMN_QUESTION};
            String selection = QuestionEntry.COLUMN_NAME + "=?";
            String[] selectionArgs = {name};
            Cursor cursor = MyApplication.getDB().query(subTableName,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null);
            if (cursor.getCount() == 1) { //Row is found
                cursor.moveToFirst();
                String q = cursor.getString(cursor.getColumnIndex(QuestionEntry.COLUMN_QUESTION));
                if (q != null) {//There is a question provided
                    name = q;
                } else {//question is not provided, get file name without extension
                    name = name.substring(0, name.lastIndexOf("."));
                }
            }
            cursor.close();

            //Add question object to the list of questions
            questions.add(new Question(name, path));
        }
    }


    /*Prepare navigation data for adapter*/
    private void prepareNavData() {
        String[] tempPath = subPath.split("/", -1);
        subPathArray = new String[tempPath.length - 4];
        for (int i = 3; i < (tempPath.length - 1); i++) {
            subPathArray[i - 3] = tempPath[i];
        }
        String str = Arrays.toString(subPathArray);
    }


    /*Navigate to main or sub activity based on clicked child element*/
    private void navigateToActivity(View v, long id) {
        Intent intent;
        String tempSubPath = "";
        String[] tempSubPathArray = subPath.split("/", -1);

        //Prepare intent
        if (id == 0) {//main activity selected
            intent = new Intent(SubActivity.this, MainActivity.class);
        } else { //sub activity selected
            id += 3;
            for (int i = 0; i < id; i++) {//Form new relativePath
                tempSubPath += "/" + tempSubPathArray[i + 1];
            }
            intent = new Intent(SubActivity.this, SubActivity.class);
            intent.putExtra("subPath", tempSubPath);
        }

        //Turn on bars
        Settings.setListsVisibility(1);

        //Start intent
        v.getContext().startActivity(intent);
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


    private void showHideExpLists() {
        if (Settings.getListsVisibility() == 1) {//Lists are visible
            //Make lists invisible
            subExpDirs.setVisibility(View.GONE);
            subExpNav.setVisibility(View.GONE);
            actionBar.hide();
            //Update settings
            Settings.setListsVisibility(0);
        } else if (Settings.getListsVisibility() == 0) {//Lists are invisible
            //Make lists visible
            subExpDirs.setVisibility(View.VISIBLE);
            subExpNav.setVisibility(View.VISIBLE);
            actionBar.show();
            //Update settings
            Settings.setListsVisibility(1);
        }
        if (subListOfDirs.size() == 0) {//hide dirs if there are none
            subExpDirs.setVisibility(View.GONE);
        }
    }

}
