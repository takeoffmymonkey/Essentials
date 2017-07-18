package com.example.android.essentials;

import java.io.File;

/**
 * Created by takeoff on 018 18 Jul 17.
 */

public class Question {

    private String question;

    private File file;


    public Question(String question, File file) {
        this.question = question;
        this.file = file;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
