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


import com.example.android.essentials.EssentialsContract.TagEntry;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.example.android.essentials.Activities.MainActivity.db;

/**
 * Created by takeoff on 021 21 Jul 17.
 */

public class EssentialsContentProvider extends ContentProvider {

    /**
     * URI matcher code for the content URI for the questions table
     */

    private static final int TAGS = 200;
    private static final int TAG_ID = 201;


    /**
     * URI matcher code for the content URI for a single question in the questions table
     */



    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        //com.example.android.essentials questions


        sUriMatcher.addURI(EssentialsContract.CONTENT_AUTHORITY,
                EssentialsContract.PATH_TAGS, TAGS);


        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.

        sUriMatcher.addURI(EssentialsContract.CONTENT_AUTHORITY,
                EssentialsContract.PATH_TAGS + "/#", TAG_ID);
    }


    /**
     * Database helper object
     */
    private EssentialsDbHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new EssentialsDbHelper(getContext());
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
                cursor = db.query(TagEntry.TABLE_NAME,
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
                cursor = db.query(TagEntry.TABLE_NAME,
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
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

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
                long id = db.insert(TagEntry.TABLE_NAME, null, values);
                Log.e ("WARNING: ", Long.toString(id));
                // If the ID is -1, then the insertion failed. Log an error and return null.
                if (id == -1) {
                    return null;
                }
                // Notify all listeners that the data has changed for the pet content URI
                getContext().getContentResolver().notifyChange(uri, null);
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
