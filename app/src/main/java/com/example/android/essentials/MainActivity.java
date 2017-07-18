package com.example.android.essentials;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static String mainPath;
    public static File mainDir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ListView dirList = (ListView) findViewById(R.id.main_list);


        //Arrays for current dir files, category names and their paths
        File[] dirFiles;
        ArrayList<String> dirCategories = new ArrayList<String>();
        final ArrayList<String> dirCategoriesPaths = new ArrayList<String>();


        //Check if card is mount
        boolean cardMount = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (!cardMount) {
            Log.e("WARNING: ", "No sd card");
        } else {//Card is mount
            //Store global path and folder file
            mainPath = Environment.getExternalStorageDirectory().getPath() + "/Essentials";
            mainDir = new File(mainPath);

            //Save paths of all files in the current dir
            dirFiles = mainDir.listFiles();
            for (int i = 0; i < dirFiles.length; i++) {
                dirCategoriesPaths.add(dirFiles[i].getAbsolutePath());
            }

            //add category names to dirCategories dirList
            for (int i = 0; i < dirFiles.length; i++) {
                String category = dirCategoriesPaths.get(i).substring(dirCategoriesPaths.get(i)
                        .lastIndexOf("/") + 1);
                dirCategories.add(category);
            }

        }


        //Set array adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.main_list_item,
                R.id.main_list_item_text, dirCategories);
        dirList.setAdapter(adapter);


        //Set clicklistener
        dirList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("currentPath", dirCategoriesPaths.get((int) id));
                view.getContext().startActivity(intent);

            }
        });
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
        }
        return super.onOptionsItemSelected(item);
    }

}
