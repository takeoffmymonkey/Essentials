package com.example.android.essentials;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.essentials.EssentialsContract.QuestionEntry;

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

        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " + QuestionEntry.TABLE_NAME + " ("
                + QuestionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + QuestionEntry.COLUMN_QUESTION + " TEXT NOT NULL, "
                + QuestionEntry.COLUMN_QUESTION_TAGS + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);

        String[] temp = {"str", "fss"};
        for (String i : temp) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(QuestionEntry.COLUMN_QUESTION, i);
            db.insert(QuestionEntry.TABLE_NAME, null, contentValues);
        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}