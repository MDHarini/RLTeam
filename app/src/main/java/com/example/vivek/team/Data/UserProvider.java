package com.example.vivek.team.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Vivek on 3/6/2017.
 */

public class UserProvider extends ContentProvider {
    public static final String LOG_TAG = UserProvider.class.getSimpleName();
    //db object to connect to db
    private UserHelper mdbhelper;
    private static final int user = 1;
    public static final int singleUser = 2;
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(UserContract.contentP, UserContract.path_user, user);
        sUriMatcher.addURI(UserContract.contentP, UserContract.path_user + "/#", singleUser);
    }

    @Override
    public boolean onCreate() {
        mdbhelper = new UserHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mdbhelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case user:
                cursor = db.query(UserContract.UserDB.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case singleUser:
                selection = UserContract.UserDB._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(UserContract.UserDB.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
      //  Cursor setNotificationUri (getContext().getContentResolver(), uri);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
     public String getType(Uri uri) {
               final int match = sUriMatcher.match(uri);
                switch (match) {
                        case user:
                                return UserContract.UserDB.CONTENT_LIST_TYPE;
                        case singleUser:
                                return UserContract.UserDB.CONTENT_ITEM_TYPE;
                        default:
                                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
                        }
            }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case user:
                return insertUser(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    public Uri insertUser(Uri uri, ContentValues values) {

        String name = values.getAsString(UserContract.UserDB.COLUMN_NAME);

        if (name == null) {
            throw new IllegalArgumentException("User requires a name");
        }
        // Check that the gender is valid
        Integer gender = values.getAsInteger(UserContract.UserDB.COLUMN_GENDER);
        if (gender == null || !UserContract.UserDB.isValidGender(gender)) {
            throw new IllegalArgumentException("User requires valid gender");
        }
        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer weight = values.getAsInteger(UserContract.UserDB.COLUMN_BIRTHDAY);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("User requires valid date");
        }
        SQLiteDatabase db = mdbhelper.getWritableDatabase();
        long id = db.insert(UserContract.UserDB.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mdbhelper.getWritableDatabase();
            int rowDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case user:
                // Delete all rows that match the selection and selection args
                //return database.delete(UserContract.UserDB.TABLE_NAME, selection, selectionArgs);
                rowDeleted = database.delete(UserContract.UserDB.TABLE_NAME, selection, selectionArgs);
                           break;
            case singleUser:
                // Delete a single row given by the ID in the URI
                selection = UserContract.UserDB._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                //return database.delete(UserContract.UserDB.TABLE_NAME, selection, selectionArgs);
                rowDeleted = database.delete(UserContract.UserDB.TABLE_NAME, selection, selectionArgs);
                                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                     if (rowDeleted != 0) {
                     getContext().getContentResolver().notifyChange(uri, null);
                    }

        // Return the number of rows deleted
           return rowDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case user:
                return updateUser(uri, contentValues, selection, selectionArgs);
            case singleUser:
                // For the user_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = UserContract.UserDB._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateUser(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update users in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more users).
     * Return the number of rows that were successfully updated.
     */
    private int updateUser(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link userEntry#COLUMN_user_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(UserContract.UserDB.COLUMN_NAME)) {
            String name = values.getAsString(UserContract.UserDB.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("User requires a name");
            }
        }

        // If the {@link userEntry#COLUMN_user_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(UserContract.UserDB.COLUMN_GENDER)) {
            Integer gender = values.getAsInteger(UserContract.UserDB.COLUMN_GENDER);
            if (gender == null || !UserContract.UserDB.isValidGender(gender)) {
                throw new IllegalArgumentException("User requires valid gender");
            }
        }

        // If the {@link userEntry#COLUMN_user_WEIGHT} key is present,
//            // check that the weight value is valid.
//            if (values.containsKey(UserContract.UserDB.COLUMN_user_WEIGHT)) {
//                // Check that the weight is greater than or equal to 0 kg
//                Integer weight = values.getAsInteger(userEntry.COLUMN_user_WEIGHT);
//                if (weight != null && weight < 0) {
//                    throw new IllegalArgumentException("user requires valid weight");
//                }
//            }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mdbhelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
       // return database.update(UserContract.UserDB.TABLE_NAME, values, selection, selectionArgs);
        int rowsUpdated = database.update(UserContract.UserDB.TABLE_NAME, values, selection, selectionArgs);

                        // If 1 or more rows were updated, then notify all listeners that the data at the
                               // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
                 }

                       // Return the number of rows updated
        return rowsUpdated;
    }
}

