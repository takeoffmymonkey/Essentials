package com.example.android.essentials;

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
}
