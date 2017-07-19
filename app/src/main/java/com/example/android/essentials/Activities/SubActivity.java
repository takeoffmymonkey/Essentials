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
import com.example.android.essentials.Question;
import com.example.android.essentials.R;
import java.io.File;
import java.util.ArrayList;

public class SubActivity extends AppCompatActivity {

    String subPath;
    String subRelativePath;
    File subDir;
    String subActivityName;
    File[] subAllFiles;
    File[] subFolders;
    File[] subFiles;
    ArrayList<String> subCategoriesNames;
    ArrayList<String> subQuestionsNames;
    final ArrayList<String> subQuestionPaths = new ArrayList<String>();
    ListView subList;
    ExpandableListView subExpList;
    ExpandableListAdapter subExpListAdapter;
    ArrayList<Question> questions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);


        //Get subActivity path
        subPath = getIntent().getStringExtra("subPath");
        Log.e("WARNING: ", "subPath: " + subPath);


        //Set activity name
        subActivityName = subPath.substring(subPath.lastIndexOf("/") + 1);
        setTitle(subActivityName);


        //Get relative path
        subRelativePath = subPath.substring(MainActivity.mainPath.length() + 1);


        //Names and paths of current dir's categories and questions
        subCategoriesNames = new ArrayList<String>();
        subQuestionsNames = new ArrayList<String>();
        final ArrayList<String> subCategoriesPaths = new ArrayList<String>();


        //Get dir file, get all its files and folders
        subDir = new File(subPath);
        subAllFiles = subDir.listFiles();


        //Separate files from folders and store files' paths and names (not folders' yet)
        ArrayList<File> filesTemp = new ArrayList<File>();
        ArrayList<File> foldersTemp = new ArrayList<File>();
        for (int i = 0; i < subAllFiles.length; i++) {
            if (subAllFiles[i].isDirectory()) { //file is a folder
                foldersTemp.add(subAllFiles[i]);
            } else { // file is a file... yep
                filesTemp.add(subAllFiles[i]);
                //store its path
                String path = subAllFiles[i].getAbsolutePath();
                subQuestionPaths.add(path);
                //store its name as name of question
                String question = path.substring(path.lastIndexOf("/") + 1);
                question = question.substring(0, question.indexOf('.'));
                subQuestionsNames.add(question);
            }
        }


        //Now store folders paths and names if it is not named as %file%.files
        for (int i = 0; i < foldersTemp.size(); i++) {
            //Get folder name
            String folderPath = foldersTemp.get(i).getAbsolutePath();
            Log.e("WARNING: ", "folderPath: " + folderPath);
            String folderName = folderPath.substring(folderPath.lastIndexOf("/") + 1);
            Log.e("WARNING: ", "folderName: " + folderName);
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


        //Move temp files and folders array lists to arrays
        // TODO: 019 19 Jul 17 why do i do this?
        int foldersSize = foldersTemp.size();
        subFolders = new File[foldersSize];
        for (int i = 0; i < foldersSize; i++) {
            subFolders[i] = foldersTemp.get(i);
        }
        int filesSize = filesTemp.size();
        subFiles = new File[filesSize];
        for (int i = 0; i < filesSize; i++) {
            subFiles[i] = filesTemp.get(i);
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
        questions = new ArrayList<Question>();

        for (int i = 0; i < subQuestionsNames.size(); i++) {
            questions.add(new Question(subQuestionsNames.get(i), subQuestionPaths.get(i)));
        }
    }

}