package com.example.android.essentials.Activities;

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

import com.example.android.essentials.R;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static String mainPath;
    File mainDir;
    ListView mainList;
    File[] mainFiles;
    ArrayList<String> mainCategories = new ArrayList<String>();
    final ArrayList<String> mainCategoriesPaths = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Check if card is mount
        boolean cardMount = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (!cardMount) {
            Log.e("WARNING: ", "No sd card");
        } else {//Card is mount
            //Store global path and folder file
            mainPath = Environment.getExternalStorageDirectory().getPath() + "/Essentials";
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
        }
        return super.onOptionsItemSelected(item);
    }

}
