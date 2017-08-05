package com.example.android.essentials;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.essentials.EssentialsContract.NotificationsEntry;
import com.example.android.essentials.EssentialsContract.QuestionEntry;
import com.example.android.essentials.EssentialsContract.TagEntry;

/**
 * Created by takeoff on 021 21 Jul 17.
 */

public class EssentialsDbHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "essentials";
    private static final int DATABASE_VERSION = 1;


    public EssentialsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create Main Questions table
        String table = "FILES";
        String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " + table + " ("
                + QuestionEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + QuestionEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + QuestionEntry.COLUMN_FOLDER + " INTEGER NOT NULL, "
                + QuestionEntry.COLUMN_QUESTION + " TEXT);";
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);

        //Create TAGS table
        String SQL_CREATE_TAGS_TABLE = "CREATE TABLE " + TagEntry.TABLE_NAME + " ("
                + TagEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TagEntry.COLUMN_PATH + " TEXT NOT NULL, "
                + TagEntry.COLUMN_SUGGESTION + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_TAGS_TABLE);

        //Create NOTIFICATIONS table
        String SQL_CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + NotificationsEntry.TABLE_NAME + " ("
                + NotificationsEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NotificationsEntry.COLUMN_QUESTION + " TEXT NOT NULL UNIQUE, "
                + NotificationsEntry.COLUMN_RELATIVE_PATH + " TEXT NOT NULL UNIQUE, "
                + NotificationsEntry.COLUMN_LEVEL + " INTEGER, "
                + NotificationsEntry.COLUMN_TIME_EDITED + " INTEGER);";
        db.execSQL(SQL_CREATE_NOTIFICATIONS_TABLE);

        //Create NOTIFICATION table
        String SQL_CREATE_SETTINGS_TABLE = "CREATE TABLE " + Settings.TABLE_NAME + " ("
                + Settings.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Settings.COLUMN_LISTS_VISIBILITY + " INTEGER DEFAULT 1, "
                + Settings.COLUMN_NOTIFICATION_MODE + " INTEGER DEFAULT 0, "
                + Settings.COLUMN_SOUND_MODE + " INTEGER DEFAULT 2);";
        db.execSQL(SQL_CREATE_SETTINGS_TABLE);
        ContentValues contentValues = new ContentValues();
        contentValues.put(Settings.COLUMN_SOUND_MODE, 2);
        contentValues.put(Settings.COLUMN_LISTS_VISIBILITY, 1);
        contentValues.put(Settings.COLUMN_NOTIFICATION_MODE, 0);
        db.insert(Settings.TABLE_NAME, null, contentValues);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
