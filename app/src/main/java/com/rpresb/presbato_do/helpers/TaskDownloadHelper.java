package com.rpresb.presbato_do.helpers;

import android.content.ContentValues;
import android.os.AsyncTask;

import com.rpresb.presbato_do.MainActivity;
import com.rpresb.presbato_do.domain.State;
import com.rpresb.presbato_do.domain.Task;
import com.rpresb.presbato_do.events.OnTaskCompleted;
import com.rpresb.presbato_do.providers.TasksProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class TaskDownloadHelper extends AsyncTask<String, String, List<Task>> {
    private final String url;
    private List<Task> tasks = new ArrayList<>();
    private MainActivity activity;
    private OnTaskCompleted onTaskCompleted;

    public TaskDownloadHelper(String url, MainActivity activity) {
        this.url = url;
        this.activity = activity;
    }

    protected List<Task> doInBackground(String... params) {
        URL url = null;
        HttpsURLConnection conn = null;
        String response = "";
        Scanner inStream = null;

        try {
            url = new URL(this.url);
            conn = (HttpsURLConnection) url.openConnection();
            inStream = new Scanner(conn.getInputStream());
            while (inStream.hasNextLine()) {
                response += (inStream.nextLine());
            }

            JSONObject jsonResult = new JSONObject(response);
            JSONArray jArray = jsonResult.getJSONArray("data");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                this.tasks.add(new Task(jObject.getInt("id"),
                        jObject.getString("name"),
                        State.fromInteger(jObject.getInt("state"))));
            }

            for (Task task : tasks) {
                ContentValues values = new ContentValues();
                values.put(DBHelper.TASK_ID, task.getId());
                values.put(DBHelper.TASK_TEXT, task.getName());
                values.put(DBHelper.TASK_STATE, task.getState().getNumericType());

                activity.getContentResolver().delete(TasksProvider.CONTENT_URI, DBHelper.TASK_ID + "=" + task.getId(), null);
                activity.getContentResolver().insert(TasksProvider.CONTENT_URI, values);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return this.tasks;
    }

    @Override
    protected void onPostExecute(List<Task> tasks) {
        super.onPostExecute(tasks);
        taskCompleted();
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