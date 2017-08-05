package com.example.android.essentials;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.essentials.Activities.MyApplication;
import com.example.android.essentials.EssentialsContract.TagEntry;

import static android.content.ContentValues.TAG;


/**
 * Created by takeoff on 021 21 Jul 17.
 */

public class EssentialsContentProvider extends ContentProvider {

    //URI matcher code for the content URI for the questions table
    private static final int TAGS = 200;
    private static final int TAG_ID = 201;

    //UriMatcher object to match a content URI to a corresponding code.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(EssentialsContract.CONTENT_AUTHORITY,
                EssentialsContract.PATH_TAGS, TAGS);

        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(EssentialsContract.CONTENT_AUTHORITY,
                EssentialsContract.PATH_TAGS + "/#", TAG_ID);
    }


    @Override
    public boolean onCreate() {
        return true;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TAGS:
                return TagEntry.CONTENT_LIST_TYPE;
            case TAG_ID:
                return TagEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case TAGS:
                //Multiple rows - perform a query
                cursor = MyApplication.getDB().query(TagEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TAG_ID:
                //Single row - extract ID from the URI and perform a query
                selection = TagEntry.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = MyApplication.getDB().query(TagEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        try {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (NullPointerException e) {
            Log.e(TAG, e.toString());
        }

        // Return the cursor
        return cursor;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TAGS:
                // Insert the new pet with the given values
                long id = MyApplication.getDB().insert(TagEntry.TABLE_NAME, null, values);
                // If the ID is -1, then the insertion failed. Log an error and return null.
                if (id == -1) {
                    return null;
                }
                // Notify all listeners that the data has changed for the pet content URI
                try {
                    getContext().getContentResolver().notifyChange(uri, null);
                } catch (NullPointerException e) {
                    Log.e(TAG, e.toString());
                }
                // Return the new URI with the ID (of the newly inserted row) appended at the end
                return ContentUris.withAppendedId(uri, id);
        }
        return null;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
