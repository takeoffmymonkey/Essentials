package com.example.android.essentials;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

        //Create TAGS table
        String SQL_CREATE_TAGS_TABLE = "CREATE TABLE " + TagEntry.TABLE_NAME + " ("
                + TagEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TagEntry.COLUMN_PATH + " TEXT NOT NULL, "
                + TagEntry.COLUMN_SUGGESTION + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_TAGS_TABLE);




        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " + QuestionEntry.TABLE_NAME + " ("
                + QuestionEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + QuestionEntry.COLUMN_QUESTION + " TEXT NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);




    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
