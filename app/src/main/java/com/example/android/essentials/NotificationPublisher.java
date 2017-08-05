package com.example.android.essentials;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android.essentials.Activities.MainActivity;
import com.example.android.essentials.Activities.MyApplication;
import com.example.android.essentials.EssentialsContract.NotificationsEntry;

/**
 * Created by takeoff on 027 27 Jul 17.
 */

public class NotificationPublisher extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static String QUESTION = "question";
    public static String QUESTION_LEVEL = "question-level";

    public NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {

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
        String selection = NotificationsEntry.COLUMN_ID + "=?";
        String[] selectionArgs = {Long.toString(id)};
        Cursor cursor = MyApplication.getDB().query(NotificationsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null);
        if (cursor.getCount() == 1) { //Row is found
            cursor.moveToFirst();
            currentLevel = cursor.getInt(cursor.getColumnIndex(NotificationsEntry.COLUMN_LEVEL));
            if (currentLevel == exLevel && currentLevel != 0) { //Level is still the same, fire notification
                notificationManager.notify((int) id, notification);
                refreshAlarm((int) id, question, currentLevel);
            }
        }
        cursor.close();
    }


    //Recreate same notification with updated time
    public static void refreshAlarm(int id, String question, int level) {
        String relativePath = null;

        //Get path
        String[] projection = {NotificationsEntry.COLUMN_RELATIVE_PATH};
        String selection = NotificationsEntry.COLUMN_ID + "=?";
        String[] selectionArgs = {Integer.toString(id)};
        Cursor cursor = MyApplication.getDB().query(NotificationsEntry.TABLE_NAME,
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

        //Update time
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotificationsEntry.COLUMN_TIME_EDITED, System.currentTimeMillis());
        String selection2 = NotificationsEntry.COLUMN_ID + "=?";
        String[] selectionArgs2 = {Integer.toString(id)};
        MyApplication.getDB().update(NotificationsEntry.TABLE_NAME, contentValues, selection2, selectionArgs2);
    }
}
