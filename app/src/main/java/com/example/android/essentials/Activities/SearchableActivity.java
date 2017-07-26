package com.example.android.essentials.Activities;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

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

    ArrayList<String> paths = new ArrayList<>();
    ArrayList<Question> questions = new ArrayList<Question>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        handleIntent();

        prepareQuestionsList();


        //Enable back option
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();

    }

    public void handleIntent() {
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            TextView tv = (TextView) findViewById(R.id.search_query);
            Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
            if (appData != null) {
                paths = appData.getStringArrayList("paths");
                tv.setText(paths.get(0));
            } else {
                tv.setText(query);
            }
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
        for (int i = 0; i < paths.size(); i++) {
            //Get path of the question
            String path = paths.get(i);
            Log.e(TAG, "working with path: " + path);

            //Get name of the file
            String name = path.substring(path.lastIndexOf("/") + 1);
            Log.e(TAG, "prepared name for question: " + name);

            //Get table of the file
            String folderRelativePath = path.substring(0, path.lastIndexOf("/"));
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
            questions.add(new Question(name, path));
        }
    }
}
