package com.example.android.essentials;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView) findViewById(R.id.test_webv);

        //This is sdcard0
        //Access to secondary storage is available through getExternalFilesDirs(String),
        //getExternalCacheDirs(), and getExternalMediaDirs().
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e("WARNING: ", "No sd card");
        } else {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    "Essentials/princ.htm");
            if (file.exists()) {
                webView.loadUrl("file://" + Environment.getExternalStorageDirectory()
                        + "/Essentials/princ.htm");
            } else Log.e("WARNING: ", "File not found");

        }


        //Text size and zoomable
        WebSettings webSettings = webView.getSettings();
        webSettings.setTextZoom(140);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

    }
}
