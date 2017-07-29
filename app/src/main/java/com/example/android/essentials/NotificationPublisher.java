package com.example.android.essentials;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.example.android.essentials.Activities.MainActivity.TAG;

/**
 * Created by takeoff on 027 27 Jul 17.
 */

public class NotificationPublisher extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

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
        Question question = intent.getParcelableExtra("question");
        int currentLevel = question.getLevel();
        int exLevel = intent.getIntExtra("level", 0);
        Log.e(TAG, "3 NotificationPublisher.onReceive: 4 currentLevel: " + currentLevel +
                " exLevel: " + exLevel);

        if (currentLevel == exLevel) {
            //Trigger resulting notification
            notificationManager.notify(id, notification);
            Log.e(TAG, "3 NotificationPublisher.onReceive: 5 triggered notificationManager.notify for id: "
                    + id + " and notification: " + notification);
        }


    }
}
