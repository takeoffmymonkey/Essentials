package com.example.android.essentials;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.example.android.essentials.Activities.MainActivity;
import com.example.android.essentials.EssentialsContract.NotificationsEntry;

import static android.content.ContentValues.TAG;
import static com.example.android.essentials.Activities.MainActivity.db;

/**
 * Created by takeoff on 027 27 Jul 17.
 */

public class NotificationPublisher extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static String QUESTION = "question";
    public static String QUESTION_LEVEL = "question-level";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Create notification manager
        NotificationManager notificationManager =
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
                rescheduleAlarmAfterNotification((int) id, question, currentLevel);
            }
        }
        cursor.close();
    }


    //Recreate same notification with updated time
    private void rescheduleAlarmAfterNotification(int id, String question, int level) {
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
        long delay;
        switch (level) {
            case 0: {
                delay = Schedule.LEVEL_0;
                break;
            }
            case 1: {
                delay = Schedule.LEVEL_1;
                break;
            }
            case 2: {
                delay = Schedule.LEVEL_2;
                break;
            }
            case 3: {
                delay = Schedule.LEVEL_3;
                break;
            }
            case 4: {
                delay = Schedule.LEVEL_4;
                break;
            }
            default: {
                delay = Schedule.LEVEL_0;
                break;
            }
        }

        //Create notification
        MainActivity.scheduleNotification(id, question, level,
                MainActivity.getNotification(question, relativePath), delay);

        //Update time
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotificationsEntry.COLUMN_TIME_EDITED, System.currentTimeMillis());
        String selection2 = NotificationsEntry.COLUMN_ID + "=?";
        String[] selectionArgs2 = {Integer.toString(id)};
        long r = db.update(NotificationsEntry.TABLE_NAME, contentValues, selection2, selectionArgs2);
        Log.e(TAG, "updating notification time: " + r);

    }
}
