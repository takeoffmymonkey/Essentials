package com.example.android.essentials;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.android.essentials.Activities.MainActivity;
import com.example.android.essentials.EssentialsContract.NotificationsEntry;
import com.example.android.essentials.EssentialsContract.QuestionEntry;

import static com.example.android.essentials.Activities.MainActivity.TAG;
import static com.example.android.essentials.Activities.MainActivity.db;

/**
 * Created by takeoff on 018 18 Jul 17.
 */

public class Question {

    private String question;
    private String fileFullPath;
    private int level;
    private String fileName;
    private String relativeFolderPath;
    private String tableName;

    public Question(String question, String fileFullPath, int level) {
        this.question = question;
        this.fileFullPath = fileFullPath;
        this.level = level;
        setFileName();
        setRelativeFolderPath();
        setTableName(relativeFolderPath);
    }

    public String getQuestion() {
        return question;
    }

    public String getFileFullPath() {
        return fileFullPath;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;

        //Update Questions table
        ContentValues contentValues = new ContentValues();
        contentValues.put(QuestionEntry.COLUMN_LEVEL, level);
        String selection = QuestionEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {getFileName()};
        db.update(tableName, contentValues, selection, selectionArgs);

        //Update Notifications table
        //Check if question exist
        Boolean questionExists = false;
        String[] projection2 = {NotificationsEntry.COLUMN_QUESTION};
        String selection2 = NotificationsEntry.COLUMN_QUESTION + "=?";
        String[] selectionArgs2 = {getQuestion()};
        Cursor c = db.query(NotificationsEntry.TABLE_NAME, projection2, selection2, selectionArgs2,
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
        MainActivity.testNotificationTable();

    }

    private void addNotification() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotificationsEntry.COLUMN_QUESTION, getQuestion());
        String relativePath = MainActivity.getRelativePathFromFull(getFileFullPath());
        contentValues.put(NotificationsEntry.COLUMN_RELATIVE_PATH, relativePath);
        contentValues.put(NotificationsEntry.COLUMN_LEVEL, 1);
        contentValues.put(NotificationsEntry.COLUMN_TIME_EDITED, System.currentTimeMillis());
        long r = db.insert(NotificationsEntry.TABLE_NAME, null, contentValues);
        Log.e(TAG, "adding to notification table response: " + r);
    }

    private void deleteNotification() {
        String selection = NotificationsEntry.COLUMN_QUESTION + "=?";
        String[] selectionArgs = {getQuestion()};
        long r = db.delete(NotificationsEntry.TABLE_NAME, selection, selectionArgs);
        Log.e(TAG, "deleting from notification table response: " + r);
    }

    private void updateNotification(int level) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotificationsEntry.COLUMN_LEVEL, level);
        contentValues.put(NotificationsEntry.COLUMN_TIME_EDITED, System.currentTimeMillis());
        String selection = NotificationsEntry.COLUMN_QUESTION + "=?";
        String[] selectionArgs = {getQuestion()};
        long r = db.update(NotificationsEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        Log.e(TAG, "updating notification table response: " + r);
    }


    public String getFileName() {
        return fileName;
    }

    public String getTableName() {
        return tableName;
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
        Log.e(TAG, "Leveled up");
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
        Log.e(TAG, "Leveled down");
        return newLevel;
    }

    private void setFileName() {
        fileName = MainActivity.getLastValueOfPath(fileFullPath);
        Log.e(TAG, "Set file name of the question: " + fileName);
    }

    private void setRelativeFolderPath() {
        relativeFolderPath = MainActivity.getRelativePathOfDirForFile(fileFullPath);
        Log.e(TAG, "Set relative folder path of the question: " + relativeFolderPath);
    }

    private void setTableName(String relativeFolderPath) {
        tableName = MainActivity.relativePathToTableName(relativeFolderPath);
        Log.e(TAG, "Set table name of the question: " + tableName);
    }

    private void setQuestionNotification() {
        //set delay
        int level = getLevel();
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
        MainActivity.scheduleNotification(this, MainActivity.getNotification(getQuestion(),
                MainActivity.getRelativePathFromFull(getFileFullPath())),
                delay);

        Log.e(TAG, "Set notification time: " + delay + " For question: " + getQuestion());
    }


}
