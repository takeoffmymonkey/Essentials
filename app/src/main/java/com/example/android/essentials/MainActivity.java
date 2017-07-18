package com.example.android.essentials;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    public static String mainPath;
    public static File mainDir;
    ArrayList<String> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView list = (ListView) findViewById(R.id.main_list);

        categories = new ArrayList<String>();

        //Check if card is mount
        boolean cardMount = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (!cardMount) {
            Log.e("WARNING: ", "No sd card");
        } else {//Card is mount
            //Store global path and folder file
            mainPath = Environment.getExternalStorageDirectory().getPath() + "/Essentials";
            Log.e("WARNING: ", "path: " + mainPath);
            mainDir = new File(mainPath);
            File[] mainFiles = mainDir.listFiles();
            Log.e("WARNING: ", "mainFiles size: " + mainFiles.length);

            //update array list
            for (int i = 0; i < mainFiles.length; i++) {
                String absPath = mainFiles[i].getAbsolutePath();
                categories.add(absPath.substring(absPath.lastIndexOf("/") + 1));
            }

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.main_list_item,
                R.id.main_list_item_text, categories);

        list.setAdapter(adapter);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
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
