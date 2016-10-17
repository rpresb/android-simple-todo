package com.rpresb.presbato_do;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.rpresb.presbato_do.domain.State;
import com.rpresb.presbato_do.helpers.DBHelper;
import com.rpresb.presbato_do.providers.TasksProvider;

public class EditorActivity extends AppCompatActivity {

    private EditText editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getString(R.string.new_task));

        editor = (EditText) findViewById(R.id.editText);
    }

    @Override
    public void onBackPressed() {
        saveTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
            case R.id.action_save:
                saveTask();
                break;
        }

        return true;
    }

    private void saveTask() {
        String newText = editor.getText().toString().trim();

        if (newText.length() == 0) {
            setResult(RESULT_CANCELED);
        } else {
            insertTask(newText);
        }

        finish();
    }

    private void insertTask(String newText) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_TEXT, newText);
        values.put(DBHelper.TASK_STATE, State.PENDING.getNumericType());

        getContentResolver().insert(TasksProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }

}
