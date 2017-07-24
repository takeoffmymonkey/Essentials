package com.example.android.essentials;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by takeoff on 020 20 Jul 17.
 */

public class SearchableActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        handleIntent();
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
                String path = appData.getString("path");
                tv.setText(path);
            } else {
                tv.setText(query);
            }
            //doMySearch(query);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            Uri data = intent.getData();
            String query = intent.getStringExtra(SearchManager.QUERY);
            TextView tv = (TextView) findViewById(R.id.search_query);
            tv.setText(query);
            Toast.makeText(this, data.toString(), Toast.LENGTH_SHORT).show();
            //showResult(data);
        }
    }
}
