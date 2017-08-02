package com.example.android.essentials;

import android.app.Notification;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.example.android.essentials.Activities.MainActivity;
import com.example.android.essentials.Activities.MyApplication;

import static android.content.ContentValues.TAG;

/**
 * Created by takeoff on 021 21 Jul 17.
 */

public final class EssentialsContract {

    //The "Content authority" is a name for the entire content provider
    static final String CONTENT_AUTHORITY = "com.example.android.essentials";


    //base of all URI's which apps will use to contact the content provider.
    private static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    static final String PATH_TAGS = "tags";


    public static final class TagEntry implements BaseColumns {
        public static final String TABLE_NAME = "TAGS";
        public final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_SUGGESTION = "SUGGEST_COLUMN_TEXT_1";
        public final static String COLUMN_PATH = "PATH";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TAGS);

        //vnd.android.cursor.dir/com.example.android.essentials/tags
        static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TAGS;

        //vnd.android.cursor.item/com.example.android.essentials/tags
        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TAGS;


    }


    public static final class QuestionEntry implements BaseColumns {
        public final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "NAME";
        public final static String COLUMN_FOLDER = "FOLDER";
        public final static String COLUMN_QUESTION = "QUESTION";
    }


    public static final class NotificationsEntry implements BaseColumns {
        public final static String TABLE_NAME = "NOTIFICATIONS";
        public final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_QUESTION = "QUESTION";
        public final static String COLUMN_RELATIVE_PATH = "RELATIVE_PATH";
        public final static String COLUMN_LEVEL = "LEVEL";
        public final static String COLUMN_TIME_EDITED = "TIME_EDITED";
    }


    public static final class Settings implements BaseColumns {
        public final static String TABLE_NAME = "SETTINGS";
        public final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_SOUND_MODE = "SOUND_MODE";


        public static int getMode(SQLiteDatabase db) {
            int mode = -1;
            String selection = COLUMN_ID + "=?";
            String[] selectionArgs = {Integer.toString(1)};
            Cursor c = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
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

        public static void setMode(int mode, SQLiteDatabase db) {
            if (mode > -1 && mode < 3) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_SOUND_MODE, mode);
                String selection = COLUMN_ID + "=?";
                String[] selectionArgs = {Integer.toString(1)};
                db.update(TABLE_NAME, contentValues, selection, selectionArgs);
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

}
