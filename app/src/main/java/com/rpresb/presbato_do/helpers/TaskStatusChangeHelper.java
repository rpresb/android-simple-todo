package com.rpresb.presbato_do.helpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.rpresb.presbato_do.MainActivity;
import com.rpresb.presbato_do.R;
import com.rpresb.presbato_do.domain.State;
import com.rpresb.presbato_do.events.OnTaskCompleted;
import com.rpresb.presbato_do.providers.TasksProvider;

import java.util.List;
import java.util.Locale;

public class TaskStatusChangeHelper extends AsyncTask<String, String, State> {
    private final Integer taskId;
    private final MainActivity activity;
    private OnTaskCompleted onTaskCompleted;

    public TaskStatusChangeHelper(Integer taskId, MainActivity activity) {
        this.taskId = taskId;
        this.activity = activity;
    }

    protected State doInBackground(String... params) {
        String selection = DBHelper.TASK_ID + "=" + taskId;
        State currentState = null;
        Cursor c = activity.getContentResolver().query(TasksProvider.CONTENT_URI,
                DBHelper.ALL_COLUMNS, selection, null, null);
        while (c.moveToNext()) {
            currentState = State.fromInteger(c.getInt(c.getColumnIndex(DBHelper.TASK_STATE)));
            ContentValues values = cursorToContentValues(c, currentState == State.DONE ? State.PENDING : State.DONE);
            activity.getContentResolver().update(TasksProvider.CONTENT_URI, values,
                    DBHelper.TASK_ID + "=" + c.getInt(c.getColumnIndex(DBHelper.TASK_ID)), null);
        }

        return currentState;
    }

    private ContentValues cursorToContentValues(Cursor c, State newState) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_ID, c.getInt(c.getColumnIndex(DBHelper.TASK_ID)));
        values.put(DBHelper.TASK_TEXT, c.getString(c.getColumnIndex(DBHelper.TASK_TEXT)));
        values.put(DBHelper.TASK_CREATED_AT, c.getString(c.getColumnIndex(DBHelper.TASK_CREATED_AT)));
        values.put(DBHelper.TASK_STATE, newState.getNumericType());
        return values;
    }

    @Override
    protected void onPostExecute(final State undoState) {
        super.onPostExecute(undoState);

        taskCompleted();

        Snackbar
                .make(activity.findViewById(R.id.main_content),
                        undoState == State.DONE ? activity.getString(R.string.task_is_pending) : activity.getString(R.string.task_completed),
                        Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String selection = DBHelper.TASK_ID + "=" + taskId;
                        Cursor c = activity.getContentResolver().query(TasksProvider.CONTENT_URI,
                                DBHelper.ALL_COLUMNS, selection, null, null);
                        while (c.moveToNext()) {
                            ContentValues values = cursorToContentValues(c, undoState);
                            activity.getContentResolver().update(TasksProvider.CONTENT_URI, values,
                                    DBHelper.TASK_ID + "=" + c.getInt(c.getColumnIndex(DBHelper.TASK_ID)), null);
                        }

                        taskCompleted();
                    }
                })
                .setActionTextColor(Color.RED)
                .show();
    }

    private void taskCompleted() {
        if (this.onTaskCompleted != null) {
            this.onTaskCompleted.onTaskCompleted();
        }
    }

    public void setOnTaskCompleted(OnTaskCompleted onTaskCompleted) {
        this.onTaskCompleted = onTaskCompleted;
    }
}