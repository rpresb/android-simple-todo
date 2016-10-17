package com.rpresb.presbato_do.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rpresb.presbato_do.R;

public class DBHelper extends SQLiteOpenHelper {

    //Constants for db name and version
    private static final String DATABASE_NAME = "presbatodo.db";
    private static final int DATABASE_VERSION = 1;

    //Constants for identifying table and columns
    public static final String TABLE_TASKS = "tasks";
    public static final String TASK_ID = "_id";
    public static final String TASK_TEXT = "taskText";
    public static final String TASK_STATE = "state";
    public static final String TASK_CREATED_AT = "createdAt";

    public static final String[] ALL_COLUMNS =
            {TASK_ID, TASK_TEXT, TASK_STATE, TASK_CREATED_AT};

    //SQL to create table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_TASKS + " (" +
                    TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TASK_TEXT + " TEXT, " +
                    TASK_STATE + " INT, " +
                    TASK_CREATED_AT + " TEXT default CURRENT_TIMESTAMP" +
                    ")";

    private final Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }
}
