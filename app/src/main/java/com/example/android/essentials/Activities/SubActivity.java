package com.example.android.essentials.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.example.android.essentials.Adapters.ExpandableListAdapter;
import com.example.android.essentials.Adapters.ExpandableNavAdapter;
import com.example.android.essentials.EssentialsContract.QuestionEntry;
import com.example.android.essentials.Question;
import com.example.android.essentials.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.android.essentials.Activities.MainActivity.TAG;
import static com.example.android.essentials.Activities.MainActivity.db;

public class SubActivity extends AppCompatActivity {

    String mainPath;
    String subPath;
    String subRelativePath;
    String subActivityName;
    String subTableName; //CS_FILES
    ArrayList<String> subListOfDirs = new ArrayList<String>();
    ArrayList<String> subListOfFiles = new ArrayList<String>();
    ListView subDirsList;
    ExpandableListView subExpList;
    ExpandableListView subExpNav;
    ArrayList<Question> questions = new ArrayList<Question>();
    ExpandableListAdapter subExpListAdapter;
    ExpandableNavAdapter subExpNavAdapter;
    String[] subPathArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        //Get and set main and current dir's full and relative paths
        mainPath = MainActivity.getMainPath();
        subPath = getIntent().getStringExtra("subPath");
        subRelativePath = "/" + subPath.substring(mainPath.length() + 1);
        Log.e(TAG, "Full sub path: " + subPath);
        Log.e(TAG, "Relative sub path: " + subRelativePath);

        //Set subActivity name
        subActivityName = subRelativePath.substring(subRelativePath.lastIndexOf("/") + 1);
        setTitle(subActivityName);

        //Get subTableName
        subTableName = MainActivity.relativePathToTableName(subRelativePath);

        //Create separate arrays for files and dirs in the current path
        MainActivity.setListsOfFilesAndDirs(subTableName, subListOfDirs, subListOfFiles);

        //Make list for dirs and set array adapter
        subDirsList = (ListView) findViewById(R.id.sub_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_main_list,
                R.id.main_list_item_text, subListOfDirs);
        subDirsList.setAdapter(adapter);

        //Set clicklistener on list
        subDirsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SubActivity.this, SubActivity.class);
                intent.putExtra("subPath", mainPath +
                        subRelativePath + "/" + subListOfDirs.get((int) id));
                view.getContext().startActivity(intent);

            }
        });

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

        //Enable back option
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    /*Create menu*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }


    /*Menu options*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_sync:

                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*Prepare questions list for adapter*/
    private void prepareQuestionsList() {
        for (int i = 0; i < subListOfFiles.size(); i++) {
            //Get path of the question
            String name = subListOfFiles.get(i);
            String path = mainPath + subRelativePath + "/" + name;
            Log.e(TAG, "Path of question: " + path);

            //Rename question if it has question text provided
            String[] projection = {QuestionEntry.COLUMN_QUESTION};
            String selection = QuestionEntry.COLUMN_NAME + "=?";
            String[] selectionArgs = {name};
            Cursor cursor = db.query(subTableName,
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


    /*Prepare navigation data for adapter*/
    private void prepareNavData() {
        String[] tempPath = subPath.split("/", -1);
        subPathArray = new String[tempPath.length - 4];
        for (int i = 3; i < (tempPath.length - 1); i++) {
            subPathArray[i - 3] = tempPath[i];
        }
        String str = Arrays.toString(subPathArray);
        Log.e(TAG, "prepared nav data: " + str);
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
            for (int i = 0; i < id; i++) {//Form new path
                tempSubPath += "/" + tempSubPathArray[i + 1];
            }
            intent = new Intent(SubActivity.this, SubActivity.class);
            intent.putExtra("subPath", tempSubPath);
        }


        //Start intent
        v.getContext().startActivity(intent);
    }


}
