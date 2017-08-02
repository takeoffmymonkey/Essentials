package com.example.android.essentials.Activities;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.android.essentials.EssentialsDbHelper;

public class MyApplication extends Application {

    private static Context context;

    EssentialsDbHelper dbHelper;
    private static SQLiteDatabase db;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
        dbHelper = new EssentialsDbHelper(this);
        db = dbHelper.getReadableDatabase();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static SQLiteDatabase getDB() {
        return MyApplication.db;
    }
}