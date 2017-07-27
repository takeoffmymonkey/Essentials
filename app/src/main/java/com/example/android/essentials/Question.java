package com.example.android.essentials;

import android.util.Log;

import static com.example.android.essentials.Activities.MainActivity.TAG;

/**
 * Created by takeoff on 018 18 Jul 17.
 */

public class Question {

    private String question;

    private String filePath;

    private int level;

    public Question(String question, String filePath, int level) {
        this.question = question;
        this.filePath = filePath;
        this.level = level;
    }

    public String getQuestion() {
        return question;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLevel() {
        return level;
    }

    public void levelUp() {
        int level = getLevel();
        Log.e(TAG, "Leveled up");
    }

    public void levelDown() {
        int level = getLevel();
        Log.e(TAG, "Leveled down");
    }


}
