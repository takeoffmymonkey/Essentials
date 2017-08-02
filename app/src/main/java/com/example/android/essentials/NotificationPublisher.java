package com.example.android.essentials;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.android.essentials.Activities.MainActivity;
import com.example.android.essentials.Activities.MyApplication;
import com.example.android.essentials.EssentialsContract.NotificationsEntry;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by takeoff on 027 27 Jul 17.
 */

public class NotificationPublisher extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static String QUESTION = "question";
    public static String QUESTION_LEVEL = "question-level";

    public NotificationManager notificationManager;

    private EssentialsDbHelper dbHelper;
    private static SQLiteDatabase db;

    @Override
    public void onReceive(Context context, Intent intent) {


        dbHelper = new EssentialsDbHelper(MyApplication.getAppContext());
        db = dbHelper.getReadableDatabase();

        //Create notification manager
        notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Get resulting notification from received intent
        Notification notification = intent.getParcelableExtra(NOTIFICATION);

        long id = intent.getLongExtra(NOTIFICATION_ID, 0);

        //Check if level is still actual
        int exLevel = intent.getIntExtra(QUESTION_LEVEL, 0);
        String question = intent.getStringExtra(QUESTION);
        int currentLevel;
        String[] projection = {NotificationsEntry.COLUMN_LEVEL};
        String selection = NotificationsEntry.COLUMN_QUESTION + "=?";
        String[] selectionArgs = {question};
        Cursor cursor = db.query(NotificationsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null);
        if (cursor.getCount() == 1) { //Row is found
            cursor.moveToFirst();
            currentLevel = cursor.getInt(cursor.getColumnIndex(NotificationsEntry.COLUMN_LEVEL));
            if (currentLevel == exLevel && currentLevel != 0) { //Level is still the same, fire notification
                notificationManager.notify((int) id, notification);
                Log.e(TAG, "NotificationPublisher.onReceive(): firing notification at: " +
                        new Date(System.currentTimeMillis()));
                refreshAlarm((int) id, question, currentLevel);
            }
        }
        cursor.close();
        db.close();
    }


    //Recreate same notification with updated time
    public static void refreshAlarm(int id, String question, int level) {
        String relativePath = null;

        //Get path
        String[] projection = {NotificationsEntry.COLUMN_RELATIVE_PATH};
        String selection = NotificationsEntry.COLUMN_ID + "=?";
        String[] selectionArgs = {Integer.toString(id)};
        Cursor cursor = db.query(NotificationsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null);
        if (cursor.getCount() == 1) { //Row is found
            cursor.moveToFirst();
            relativePath = cursor.getString(cursor.getColumnIndex
                    (NotificationsEntry.COLUMN_RELATIVE_PATH));
        }
        cursor.close();

        //Set delay
        long delay = Schedule.getDelayByLevel(level);

        //Create notification
        MainActivity.scheduleNotification(id, question, level,
                MainActivity.getNotification(question, relativePath), delay);

        Log.e(TAG, "NotificationPublisher.refreshAlarm(): " +
                "Re-creating notification with delay of seconds: "
                + TimeUnit.MILLISECONDS.toSeconds(delay));

        //Update time
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotificationsEntry.COLUMN_TIME_EDITED, System.currentTimeMillis());
        String selection2 = NotificationsEntry.COLUMN_ID + "=?";
        String[] selectionArgs2 = {Integer.toString(id)};
        db.update(NotificationsEntry.TABLE_NAME, contentValues, selection2, selectionArgs2);
        Log.e(TAG, "NotificationPublisher.refreshAlarm(): " +
                "Setting new last time edited to: " + new Date(System.currentTimeMillis()));

    }
}
