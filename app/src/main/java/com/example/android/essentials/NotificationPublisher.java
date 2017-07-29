package com.example.android.essentials;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.example.android.essentials.EssentialsContract.QuestionEntry;

import static com.example.android.essentials.Activities.MainActivity.TAG;
import static com.example.android.essentials.Activities.MainActivity.db;

/**
 * Created by takeoff on 027 27 Jul 17.
 */

public class NotificationPublisher extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static String QUESTION_TABLE = "question-table";
    public static String QUESTION_FILE = "question-file";
    public static String QUESTION_LEVEL = "question-level";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(TAG, "3 NotificationPublisher.onReceive: 1 received intent: "
                + intent.toString());

        //Create notification manager
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.e(TAG, "3 NotificationPublisher.onReceive: 2 created notificationManager: "
                + notificationManager.toString());

        //Get resulting notification from received intent
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        Log.e(TAG, "3 NotificationPublisher.onReceive: 3 created notification from received intent (by NOTIFICATION): "
                + notification.toString());

        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        Log.e(TAG, "3 NotificationPublisher.onReceive: 3 created id from received intent (by NOTIFICATION_ID): "
                + id);

        //Check if level is still actual
        String questionFileName = intent.getStringExtra(QUESTION_FILE);
        String questionTableName = intent.getStringExtra(QUESTION_TABLE);
        int exLevel = intent.getIntExtra(QUESTION_LEVEL, 0);
        int currentLevel = 0;
        String[] projection = {QuestionEntry.COLUMN_LEVEL};
        String selection = QuestionEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {questionFileName};
        Cursor cursor = db.query(questionTableName,
                projection,
                selection,
                selectionArgs,
                null, null, null);
        if (cursor.getCount() == 1) { //Row is found
            cursor.moveToFirst();
            currentLevel = cursor.getInt(cursor.getColumnIndex(QuestionEntry.COLUMN_LEVEL));
            Log.e(TAG, "3 NotificationPublisher.onReceive: 4 currentLevel: " + currentLevel +
                    " exLevel: " + exLevel);
            if (currentLevel == exLevel && currentLevel != 0) { //Level is still the same, fire notification
                notificationManager.notify(id, notification);
                Log.e(TAG, "3 NotificationPublisher.onReceive: 5 triggered notificationManager.notify for id: "
                        + id + " and notification: " + notification);
            }
        }
        cursor.close();
    }
}
