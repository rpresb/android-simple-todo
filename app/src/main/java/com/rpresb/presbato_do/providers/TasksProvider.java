package com.rpresb.presbato_do.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.rpresb.presbato_do.helpers.DBHelper;

public class TasksProvider extends ContentProvider {

    private static final String AUTHORITY = "com.rpresb.presbato_do.tasksprovider";
    private static final String BASE_PATH = "tasks";
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    // Constant to identify the requested operation
    private static final int TASKS = 1;
    private static final int TASKS_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "Task";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, TASKS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TASKS_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DBHelper helper = new DBHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if (uriMatcher.match(uri) == TASKS_ID) {
            selection = DBHelper.TASK_ID + "=" + uri.getLastPathSegment();
        }

        return database.query(DBHelper.TABLE_TASKS, DBHelper.ALL_COLUMNS,
                selection, null, null, null,
                DBHelper.TASK_CREATED_AT + " DESC");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = database.insert(DBHelper.TABLE_TASKS,
                null, values);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(DBHelper.TABLE_TASKS, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(DBHelper.TABLE_TASKS,
                values, selection, selectionArgs);
    }
}
