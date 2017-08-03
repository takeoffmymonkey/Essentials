package com.example.android.essentials;

import android.app.Notification;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.example.android.essentials.Activities.MainActivity;
import com.example.android.essentials.Activities.MyApplication;

import static android.content.ContentValues.TAG;

/**
 * Created by takeoff on 003 03 Aug 17.
 */

public final class Settings implements BaseColumns {

    private static SQLiteDatabase db;


    public final static String TABLE_NAME = "SETTINGS";
    public final static String COLUMN_ID = BaseColumns._ID;
    public final static String COLUMN_SOUND_MODE = "SOUND_MODE";
    public final static String COLUMN_LISTS_VISIBILITY = "LISTS_VISIBILITY";

    public static int getListsVisibility() {
        db = MyApplication.getDB();

        int mode = -1;
        String[] projection = {COLUMN_LISTS_VISIBILITY};
        String selection = COLUMN_ID + "=?";
        String[] selectionArgs = {Integer.toString(1)};
        Cursor c = MyApplication.getDB().query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if (c.getCount() == 1) {
            c.moveToFirst();
            mode = c.getInt(c.getColumnIndex(COLUMN_LISTS_VISIBILITY));
        }
        c.close();

        return mode;
    }

    public static void setListsVisibility(int visibility) {
        if (visibility == 0 || visibility == 1) {
            db = MyApplication.getDB();

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_LISTS_VISIBILITY, visibility);
            String selection = COLUMN_ID + "=?";
            String[] selectionArgs = {Integer.toString(1)};
            MyApplication.getDB().update(TABLE_NAME, contentValues, selection, selectionArgs);
        }
    }

    public static int getSoundMode() {
        db = MyApplication.getDB();

        int mode = -1;
        String[] projection = {COLUMN_SOUND_MODE};
        String selection = COLUMN_ID + "=?";
        String[] selectionArgs = {Integer.toString(1)};
        Cursor c = MyApplication.getDB().query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if (c.getCount() == 1) {
            c.moveToFirst();
            mode = c.getInt(c.getColumnIndex(COLUMN_SOUND_MODE));
        }
        c.close();

        switch (mode) {
            case 0: {
                mode = 0;
                break;
            }
            case 1: {
                mode = Notification.DEFAULT_SOUND;
                break;
            }
            case 2: {
                mode = Notification.DEFAULT_VIBRATE;
                break;
            }
            default: {
                mode = Notification.DEFAULT_VIBRATE;
                break;
            }
        }

        return mode;
    }

    public static void setSoundMode(int mode) {
        if (mode > -1 && mode < 3) {
            db = MyApplication.getDB();

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_SOUND_MODE, mode);
            String selection = COLUMN_ID + "=?";
            String[] selectionArgs = {Integer.toString(1)};
            MyApplication.getDB().update(TABLE_NAME, contentValues, selection, selectionArgs);
            MainActivity.rescheduleNotifications();
            String modeString = null;
            if (mode == 0) {
                modeString = "Silent mode";
            } else if (mode == 1) {
                modeString = "Sound mode";
            } else if (mode == 2) {
                modeString = "Vibration mode";
            }
            Toast.makeText(MyApplication.getAppContext(), modeString, Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Such mode is not permitted");
        }
    }
}