package com.example.android.essentials;

import java.io.File;

/**
 * Created by takeoff on 018 18 Jul 17.
 */

public class Question {

    private String question;

    private String filePath;


    public Question(String question, String filePath) {
        this.question = question;
        this.filePath = filePath;
    }

    public String getQuestion() {
        return question;
    }

    public String getFilePath() {
        return filePath;
    }

}
