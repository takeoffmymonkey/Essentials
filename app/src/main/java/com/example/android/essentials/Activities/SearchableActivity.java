package com.example.android.essentials.Activities;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.example.android.essentials.Adapters.ExpandableListAdapter;
import com.example.android.essentials.EssentialsContract.QuestionEntry;
import com.example.android.essentials.Question;
import com.example.android.essentials.R;

import java.util.ArrayList;

import static com.example.android.essentials.Activities.MainActivity.TAG;
import static com.example.android.essentials.Activities.MainActivity.db;

/**
 * Created by takeoff on 020 20 Jul 17.
 */

public class SearchableActivity extends AppCompatActivity {

    ArrayList<String> relativePaths = new ArrayList<>();
    String relativePath = null;
    ArrayList<Question> questions = new ArrayList<Question>();
    ExpandableListView expList;
    ExpandableListAdapter expListAdapter;
    String mainPath;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        handleIntent();

        mainPath = MainActivity.getMainPath();

        //Make expandable list and set adapter
        expList = (ExpandableListView) findViewById(R.id.search_exp_list);
        prepareQuestionsList();
        expListAdapter = new ExpandableListAdapter(this, questions);
        expList.setAdapter(expListAdapter);

        //Enable back option
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();

    }

    public void handleIntent() {
        // Get the intent, verify the action and get the additional data (relativePath)
        Intent intent = getIntent();
        relativePath = intent.getStringExtra("relativePath");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
            if (appData != null) {
                relativePaths = appData.getStringArrayList("relativePaths");
            }
        } else if (relativePath != null) {
            relativePaths.add(relativePath);
        }
    }

    /*Menu options*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*Prepare questions list for adapter*/
    private void prepareQuestionsList() {
        for (int i = 0; i < relativePaths.size(); i++) {
            //Get fileRelativePath of the question and create full relativePath
            String fileRelativePath = relativePaths.get(i);
            String fileFullPath = mainPath + fileRelativePath;
            Log.e(TAG, "working with fileRelativePath: " + fileRelativePath);
            Log.e(TAG, "working with fileFullPath: " + fileFullPath);

            //Get name of the file
            String name = fileRelativePath.substring(fileRelativePath.lastIndexOf("/") + 1);
            Log.e(TAG, "prepared name for question: " + name);

            //Get table of the file
            String folderRelativePath = fileRelativePath.substring(0, fileRelativePath.lastIndexOf("/"));
            Log.e(TAG, "prepared folderRelativePath for question: " + folderRelativePath);
            String tableName = MainActivity.relativePathToTableName(folderRelativePath);
            Log.e(TAG, "prepared table name for question: " + tableName);

            //Rename question if it has question text provided
            String[] projection = {QuestionEntry.COLUMN_QUESTION};
            String selection = QuestionEntry.COLUMN_NAME + "=?";
            String[] selectionArgs = {name};
            Cursor cursor = db.query(tableName,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null);
            if (cursor.getCount() == 1) { //Row is found
                cursor.moveToFirst();
                String q = cursor.getString(cursor.getColumnIndex(QuestionEntry.COLUMN_QUESTION));
                if (q != null) {//There is a question provided
                    name = q;
                    Log.e(TAG, "New name of question: " + name);
                }
            }
            cursor.close();
            //Add question object to the list of questions
            questions.add(new Question(name, fileFullPath));
        }
    }
}
