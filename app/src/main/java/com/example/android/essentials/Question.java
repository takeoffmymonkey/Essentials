package com.example.android.essentials;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.android.essentials.Activities.MainActivity;
import com.example.android.essentials.Activities.MyApplication;
import com.example.android.essentials.EssentialsContract.NotificationsEntry;

/**
 * Created by takeoff on 018 18 Jul 17.
 */

public class Question {

    private String question;
    private String fileFullPath;


    public Question(String question, String fileFullPath) {
        this.question = question;
        this.fileFullPath = fileFullPath;
    }

    public String getQuestion() {
        return question;
    }

    public String getFileFullPath() {
        return fileFullPath;
    }

    public int getLevel() {
        int level = 0;

        String[] projection = {NotificationsEntry.COLUMN_LEVEL};
        String selection = NotificationsEntry.COLUMN_QUESTION + "=?";
        String[] selectionArgs = {getQuestion()};
        Cursor c = MyApplication.getDB().query(NotificationsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null);
        if (c.getCount() == 1) {//Question exists
            c.moveToFirst();
            level = c.getInt(c.getColumnIndex(NotificationsEntry.COLUMN_LEVEL));
        }
        c.close();

        return level;
    }

    public void setLevel(int level) {
        //Update Notifications table
        //Check if question exist
        Boolean questionExists = false;
        String[] projection2 = {NotificationsEntry.COLUMN_QUESTION};
        String selection2 = NotificationsEntry.COLUMN_QUESTION + "=?";
        String[] selectionArgs2 = {getQuestion()};
        Cursor c = MyApplication.getDB().query(NotificationsEntry.TABLE_NAME,
                projection2,
                selection2,
                selectionArgs2,
                null, null, null);
        if (c.getCount() == 1) {//Question exists
            questionExists = true;
        }
        c.close();

        //Choose action to perform
        if (questionExists && level == 0) {
            deleteNotification();
        } else if (!questionExists && level > 0) {
            addNotification();
        } else {
            updateNotification(level);
        }
    }

    private void addNotification() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotificationsEntry.COLUMN_QUESTION, getQuestion());
        String relativePath = MainActivity.getRelativePathFromFull(getFileFullPath());
        contentValues.put(NotificationsEntry.COLUMN_RELATIVE_PATH, relativePath);
        contentValues.put(NotificationsEntry.COLUMN_LEVEL, 1);
        contentValues.put(NotificationsEntry.COLUMN_TIME_EDITED, System.currentTimeMillis());
        MyApplication.getDB().insert(NotificationsEntry.TABLE_NAME, null, contentValues);

    }

    private long getNotificationId() {
        long notificationId = -1;
        String[] projection = {NotificationsEntry.COLUMN_ID};
        String selection = NotificationsEntry.COLUMN_QUESTION + "=?";
        String[] selectionArgs = {getQuestion()};
        Cursor c = MyApplication.getDB().query(NotificationsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null);
        if (c.getCount() == 1) {//Question exists
            c.moveToFirst();
            notificationId = c.getLong(c.getColumnIndex(NotificationsEntry.COLUMN_ID));
        }
        c.close();
        return notificationId;
    }

    private void deleteNotification() {
        String selection = NotificationsEntry.COLUMN_QUESTION + "=?";
        String[] selectionArgs = {getQuestion()};
        MyApplication.getDB().delete(NotificationsEntry.TABLE_NAME,
                selection,
                selectionArgs);
    }

    private void updateNotification(int level) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotificationsEntry.COLUMN_LEVEL, level);
        contentValues.put(NotificationsEntry.COLUMN_TIME_EDITED, System.currentTimeMillis());
        String selection = NotificationsEntry.COLUMN_QUESTION + "=?";
        String[] selectionArgs = {getQuestion()};
        MyApplication.getDB().update(NotificationsEntry.TABLE_NAME,
                contentValues,
                selection,
                selectionArgs);

    }

    public int levelUp() {
        int currentLevel = getLevel();
        int newLevel;
        if (currentLevel < 4) {
            newLevel = ++currentLevel;
            setLevel(newLevel);
        } else {
            newLevel = currentLevel;
        }
        setQuestionNotification();
        return newLevel;
    }

    public int levelDown() {
        int currentLevel = getLevel();
        int newLevel;
        if (currentLevel > 0) {
            newLevel = --currentLevel;
            setLevel(newLevel);
        } else {
            newLevel = currentLevel;
        }
        setQuestionNotification();
        return newLevel;
    }

    private void setQuestionNotification() {
        //set delay
        int level = getLevel();
        long delay = Schedule.getDelayByLevel(level);

        //Create notification
        MainActivity.scheduleNotification(getNotificationId(), getQuestion(), getLevel(),
                MainActivity.getNotification(getQuestion(), MainActivity.getRelativePathFromFull
                        (getFileFullPath())),
                delay);
    }
}
