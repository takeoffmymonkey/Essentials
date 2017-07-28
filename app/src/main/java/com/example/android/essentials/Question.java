package com.example.android.essentials;

import android.content.ContentValues;
import android.util.Log;

import com.example.android.essentials.Activities.MainActivity;
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
        //Update db
        ContentValues contentValues = new ContentValues();
        contentValues.put(QuestionEntry.COLUMN_LEVEL, level);
        String selection = QuestionEntry.COLUMN_NAME + "=?";
        String[] selectionArgs = {fileName};
        db.update(tableName, contentValues, selection, selectionArgs);
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
        MainActivity.scheduleNotification(MainActivity.getNotification(getQuestion(),
                MainActivity.getRelativePathFromFull(getFileFullPath())),
                delay);

        Log.e(TAG, "Set notification time: " + delay + " For question: " + getQuestion());
    }

}
