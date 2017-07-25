package com.example.android.essentials;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by takeoff on 021 21 Jul 17.
 */

public final class EssentialsContract {

    //The "Content authority" is a name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.essentials";


    //base of all URI's which apps will use to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_QUESTIONS = "questions";

    public static final String PATH_TAGS = "tags";



    //Inner class that defines constant values for the questions database table.
    public static final class QuestionEntry implements BaseColumns {

        public final static String TABLE_NAME = "questions_7686_5763";
        public final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_QUESTION = "SUGGEST_COLUMN_TEXT_1";



        //The content URI to access the pet data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_QUESTIONS);

        //The MIME type of the {@link #CONTENT_URI} for a list of questions.
        //vnd.android.cursor.dir/com.example.android.essentials/questions
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUESTIONS;

        //The MIME type of the {@link #CONTENT_URI} for a single question.
        //vnd.android.cursor.item/com.example.android.essentials/questions
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUESTIONS;


    }


    public static final class TagEntry implements BaseColumns{
        public static final String TABLE_NAME = "TAGS";
        public final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_SUGGESTION = "SUGGEST_COLUMN_TEXT_1";
        public final static String COLUMN_PATH = "PATH";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TAGS);

        //vnd.android.cursor.dir/com.example.android.essentials/tags
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TAGS;

        //vnd.android.cursor.item/com.example.android.essentials/tags
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TAGS;


    }

}
