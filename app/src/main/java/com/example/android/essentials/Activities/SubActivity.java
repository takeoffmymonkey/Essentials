package com.example.android.essentials.Activities;

import android.content.Intent;
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
import com.example.android.essentials.Question;
import com.example.android.essentials.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class SubActivity extends AppCompatActivity {

    String subPath;
    String[] subPathArray;
    String subRelativePath;
    File subDir;
    String subActivityName;
    File[] subAllFiles;
    ArrayList<String> subCategoriesNames = new ArrayList<String>();
    ArrayList<String> subQuestionsNames = new ArrayList<String>();
    final ArrayList<String> subCategoriesPaths = new ArrayList<String>();
    final ArrayList<String> subQuestionPaths = new ArrayList<String>();
    ListView subList;
    ExpandableListView subExpList;
    ExpandableListView subExpNav;
    ExpandableListAdapter subExpListAdapter;
    ExpandableNavAdapter subExpNavAdapter;

    ArrayList<Question> questions = new ArrayList<Question>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);


        //Get subActivity path
        subPath = getIntent().getStringExtra("subPath");


        //Set subActivity name
        subActivityName = subPath.substring(subPath.lastIndexOf("/") + 1);
        setTitle(subActivityName);


        //Get relative path
        subRelativePath = subPath.substring(MainActivity.getMainPath().length() + 1);


        //Get dir file, get all its files and folders
        subDir = new File(subPath);
        subAllFiles = subDir.listFiles();


        //Separate files from folders and store files' paths and names (not folders' yet)
        ArrayList<File> foldersTemp = new ArrayList<File>();
        for (File file :
                subAllFiles) {
            if (file.isDirectory()) { //file is a folder
                foldersTemp.add(file);
            } else { // file is a file... yep
                //store its path and name (question)
                String path = file.getAbsolutePath();
                subQuestionPaths.add(path);
                String question = path.substring(path.lastIndexOf("/") + 1);
                question = question.substring(0, question.indexOf('.'));
                subQuestionsNames.add(question);
            }
        }


        //Now store folders paths and names if it is not named as %file%.files
        for (int i = 0; i < foldersTemp.size(); i++) {
            //Get folder name
            String folderPath = foldersTemp.get(i).getAbsolutePath();
            String folderName = folderPath.substring(folderPath.lastIndexOf("/") + 1);
            //Search for it in file names
            boolean found = false;
            for (int a = 0; a < subQuestionsNames.size(); a++) {
                if (folderName.equals(subQuestionsNames.get(a) + ".files")) {
                    found = true;
                    break;
                }
            }
            if (!found) {//name was not found
                //store its path
                subCategoriesPaths.add(folderPath);
                //store its name as name of category
                subCategoriesNames.add(folderName);
            }
        }


        //Make list and set array adapter
        subList = (ListView) findViewById(R.id.sub_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.main_list_item,
                R.id.main_list_item_text, subCategoriesNames);
        subList.setAdapter(adapter);


        //Set clicklistener on list
        subList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(SubActivity.this, SubActivity.class);
                intent.putExtra("subPath", subCategoriesPaths.get((int) id));
                view.getContext().startActivity(intent);

            }
        });


        //Make expandable list and set adapter
        subExpList = (ExpandableListView) findViewById(R.id.sub_exp_list);
        prepareListData();
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
            case R.id.main_menu_search:
                return true;
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*Prepare questions for adapter*/
    private void prepareListData() {
        for (int i = 0; i < subQuestionsNames.size(); i++) {
            questions.add(new Question(subQuestionsNames.get(i), subQuestionPaths.get(i)));
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
        Log.e("WARNING: ", "prepared nav data: " + str);

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
