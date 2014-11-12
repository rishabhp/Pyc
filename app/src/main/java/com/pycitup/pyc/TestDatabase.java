package com.pycitup.pyc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by rishabhpugalia on 25/10/14.
 */
public class TestDatabase extends SQLiteOpenHelper {

    private static final String TAG = "TestDatabase";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "test_db";

    public static final String TABLE_TUTORIALS = "tutorials";
    public static final String ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_URL = "url";

    private static final String CREATE_TABLE_TUTORIALS =
            "create table " + TABLE_TUTORIALS
            + " (" + ID + " integer primary key autoincrement, "
            + COL_TITLE + " text not null, "
            + COL_URL + " text not null);";

    private static final String DB_SCHEMA = CREATE_TABLE_TUTORIALS;

    public TestDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_SCHEMA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database. Existing contents will be lost. ["
                + oldVersion + "]->[" + newVersion + "]");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TUTORIALS);
        onCreate(db);
    }
}
