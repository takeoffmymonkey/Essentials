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
import static com.example.android.essentials.Activities.MyApplication.getDB;

/**
 * Created by takeoff on 003 03 Aug 17.
 */

public final class Settings implements BaseColumns {

    private static SQLiteDatabase db;


    public final static String TABLE_NAME = "SETTINGS";
    public final static String COLUMN_ID = BaseColumns._ID;
    public final static String COLUMN_SOUND_MODE = "SOUND_MODE";
    final static String COLUMN_LISTS_VISIBILITY = "LISTS_VISIBILITY";
    final static String COLUMN_NOTIFICATION_MODE = "NOTIFICATION_MODE";


    public static int getNotificationMode() {
        db = getDB();

        int mode = -1;
        String[] projection = {COLUMN_NOTIFICATION_MODE};
        String selection = COLUMN_ID + "=?";
        String[] selectionArgs = {Integer.toString(1)};
        Cursor c = getDB().query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if (c.getCount() == 1) {
            c.moveToFirst();
            mode = c.getInt(c.getColumnIndex(COLUMN_NOTIFICATION_MODE));
        }
        c.close();

        return mode;
    }


    public static void setNotificationMode(int mode) {
        if (mode == 0 || mode == 1) {
            db = getDB();

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_NOTIFICATION_MODE, mode);
            String selection = COLUMN_ID + "=?";
            String[] selectionArgs = {Integer.toString(1)};
            int success = MyApplication.getDB().update(TABLE_NAME, contentValues, selection, selectionArgs);

            String modeString = null;
            if (success == 1) {
                if (mode == 0) {
                    modeString = "Normal mode";
                } else {
                    modeString = "Test mode";
                }
                Toast.makeText(MyApplication.getAppContext(), modeString, Toast.LENGTH_SHORT).show();
            }
        }
    }


    public static int getListsVisibility() {
        db = getDB();

        int mode = -1;
        String[] projection = {COLUMN_LISTS_VISIBILITY};
        String selection = COLUMN_ID + "=?";
        String[] selectionArgs = {Integer.toString(1)};
        Cursor c = getDB().query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if (c.getCount() == 1) {
            c.moveToFirst();
            mode = c.getInt(c.getColumnIndex(COLUMN_LISTS_VISIBILITY));
        }
        c.close();

        return mode;
    }

    public static void setListsVisibility(int visibility) {
        if (visibility == 0 || visibility == 1) {
            db = getDB();

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_LISTS_VISIBILITY, visibility);
            String selection = COLUMN_ID + "=?";
            String[] selectionArgs = {Integer.toString(1)};
            getDB().update(TABLE_NAME, contentValues, selection, selectionArgs);
        }
    }

    public static int getSoundMode() {
        db = getDB();

        int mode = -1;
        String[] projection = {COLUMN_SOUND_MODE};
        String selection = COLUMN_ID + "=?";
        String[] selectionArgs = {Integer.toString(1)};
        Cursor c = getDB().query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);
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
            db = getDB();

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_SOUND_MODE, mode);
            String selection = COLUMN_ID + "=?";
            String[] selectionArgs = {Integer.toString(1)};
            getDB().update(TABLE_NAME, contentValues, selection, selectionArgs);
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