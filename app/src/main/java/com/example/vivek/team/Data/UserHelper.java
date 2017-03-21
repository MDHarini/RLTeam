package com.example.vivek.team.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.vivek.team.Data.UserContract.UserDB;
/**
 * Created by Vivek on 3/5/2017.
 */

public class UserHelper extends SQLiteOpenHelper  {
    public static final int DATABASE_VERSION =1;
    public static final String DATABASE_NAME = "UserDB.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + UserDB.TABLE_NAME + " (" +
                    UserDB._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserDB.COLUMN_NAME + " TEXT NOT NULL, " +
                    UserDB.COLUMN_USERID + " TEXT NOT NULL, "+
                    UserDB.COLUMN_GENDER + " INTEGER NOT NULL, "+
                    UserDB.COLUMN_BIRTHDAY + " INTEGER NOT NULL DEFAULT 0);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UserDB.TABLE_NAME;

    public UserHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
