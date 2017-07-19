package com.example.android.essentials;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubActivity extends AppCompatActivity {

    String subPath;
    File subDir;
    String subActivityName;

    File[] subAllFiles;
    File[] subFolders;
    File[] subFiles;

    ArrayList<String> subCategories;

    ListView subList;
    ExpandableListView subExpList;
    ExpandableListAdapter subExpListAdapter;
    List<String> listDataHeader;
    HashMap<String, String> listDataChild;


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


        //Arrays for current dir category names and their paths
        subCategories = new ArrayList<String>();
        final ArrayList<String> subCategoriesPaths = new ArrayList<String>();


        //Get dir file, get all its files and folders
        subDir = new File(subPath);
        subAllFiles = subDir.listFiles();


        //Separate files from folders and get folders paths and categories names
        ArrayList<File> filesTemp = new ArrayList<File>();
        ArrayList<File> foldersTemp = new ArrayList<File>();
        for (int i = 0; i < subAllFiles.length; i++) {
            if (subAllFiles[i].isDirectory()) { //file is a folder
                foldersTemp.add(subAllFiles[i]);
                //store its path
                subCategoriesPaths.add(subAllFiles[i].getAbsolutePath());
                //store its name as name of category
                String category = subCategoriesPaths.get(i).substring(subCategoriesPaths.get(i)
                        .lastIndexOf("/") + 1);
                subCategories.add(category);
            } else { // file is a file... yep
                filesTemp.add(subAllFiles[i]);
            }
        }


        //Make list and set array adapter
        subList = (ListView) findViewById(R.id.sub_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.main_list_item,
                R.id.main_list_item_text, subCategories);
        subList.setAdapter(adapter);


        //Set clicklistener on it
        subList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(SubActivity.this, SubActivity.class);
                intent.putExtra("subPath", subCategoriesPaths.get((int) id));
                view.getContext().startActivity(intent);

            }
        });


        //Make expandable list
        subExpList = (ExpandableListView) findViewById(R.id.sub_exp_list);

        //Prepare list data
        prepareListData();


        //Set adapter
        subExpListAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        subExpList.setAdapter(subExpListAdapter);


        //Enable back option
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }


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


    /*
         * Preparing the list data
         */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, String>();

        // Adding header data
        listDataHeader.add("Header 1");
        listDataHeader.add("Header 2");
        listDataHeader.add("Header 3");

        // Adding child data
        String child1 = "Child 1";
        String child2 = "Child 2";
        String child3 = "Child 3";


        listDataChild.put(listDataHeader.get(0), child1); // Header, Child data
        listDataChild.put(listDataHeader.get(1), child2);
        listDataChild.put(listDataHeader.get(2), child3);
    }

}



        /*// Listview Group click listener
        subExpList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Listview Group expanded listener
        subExpList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Listview Group collasped listener
        subExpList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();

            }
        });

        // Listview on child click listener
        subExpList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(
                        getApplicationContext(),
                        listDataHeader.get(groupPosition)
                                + " : "
                                + listDataChild.get(
                                listDataHeader.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });*/