package com.pycitup.pyc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by rishabhpugalia on 25/10/14.
 */
public class PostDatabase extends SQLiteOpenHelper {

    private static final String TAG = "PostDatabase";

    // Database Specific Details
    private static final int DB_VERSION = 2;
    private static final String DB_NAME = "test_db";

    // `posts` table details
    public static final String TABLE_POSTS = "posts";
    public static final String ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";

    private static final String CREATE_TABLE_POSTS =
            "CREATE TABLE " + TABLE_POSTS
            + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_TITLE + " TEXT NOT NULL, "
            + COL_CONTENT + " TEXT NOT NULL);";


    public PostDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Called when the database is created for the
        // first time. This is where the creation of
        // tables and the initial population of the tables should happen.

        db.execSQL(CREATE_TABLE_POSTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Called when the database needs to be upgraded.
        // The implementation should use this method to
        // drop tables, add tables, or do anything else
        // it needs to upgrade to the new schema version.

        Log.w(TAG, "Upgrading database. Existing contents will be lost. ["
                + oldVersion + "] -> [" + newVersion + "]");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);

        // Create after dropping
        onCreate(db);
    }
}
