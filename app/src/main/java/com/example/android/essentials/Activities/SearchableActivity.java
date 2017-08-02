package com.example.android.essentials.Activities;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.example.android.essentials.Adapters.ExpandableListAdapter;
import com.example.android.essentials.EssentialsContract.QuestionEntry;
import com.example.android.essentials.EssentialsDbHelper;
import com.example.android.essentials.Question;
import com.example.android.essentials.R;

import java.io.File;
import java.util.ArrayList;

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
    private EssentialsDbHelper dbHelper;
    private static SQLiteDatabase db;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new EssentialsDbHelper(this);
        db = dbHelper.getReadableDatabase();

        mainPath = MainActivity.getMainPath();

        if (handleIntent()) {


            //Make expandable list and set adapter
            expList = (ExpandableListView) findViewById(R.id.search_exp_list);
            prepareQuestionsList();
            expListAdapter = new ExpandableListAdapter(this, questions);
            expList.setAdapter(expListAdapter);

            //Enable back option
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();

    }

    public boolean handleIntent() {
        boolean isFile = true;
        // Get the intent, verify the action and get the additional data (relativePath)
        Intent intent = getIntent();
        relativePath = intent.getStringExtra("relativePath");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
            if (appData != null) {
                relativePaths = appData.getStringArrayList("relativePaths");
                //Check if this is a folder
                String fullPath = mainPath + relativePaths.get(0);
                if ((new File(fullPath)).isDirectory()) {
                    isFile = false;
                    Intent intent2 = new Intent(MyApplication.getAppContext(), SubActivity.class);
                    intent2.putExtra("subPath", fullPath);
                    startActivity(intent2);
                }
            }
        } else if (relativePath != null) {
            relativePaths.add(relativePath);
        }
        return isFile;
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

            //Get name of the file
            String name = fileRelativePath.substring(fileRelativePath.lastIndexOf("/") + 1);

            //Get table of the file
            String folderRelativePath = fileRelativePath.substring(0, fileRelativePath.lastIndexOf("/"));
            String tableName = MainActivity.relativePathToTableName(folderRelativePath);

            //Rename question if it has question text provided, add level
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
                }

            }
            cursor.close();
            //Add question object to the list of questions
            questions.add(new Question(name, fileFullPath, db));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
