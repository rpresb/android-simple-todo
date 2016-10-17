package com.rpresb.presbato_do.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rpresb.presbato_do.R;
import com.rpresb.presbato_do.events.OnChangeState;
import com.rpresb.presbato_do.events.OnSelectModeChanged;
import com.rpresb.presbato_do.helpers.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class TasksRVAdapter extends RecyclerView.Adapter<TasksRVAdapter.ViewHolder> {

    private Cursor cursor = null;
    private static List<Integer> selectedTasks = new ArrayList<>();
    private static boolean selectMode = false;

    private OnSelectModeChanged onSelectModeChanged;
    private OnChangeState onChangeState;

    public void swapCursor(Cursor c) {
        this.cursor = c;
    }

    @Override
    public TasksRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_item, parent, false);

        return new TasksRVAdapter.ViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(TasksRVAdapter.ViewHolder holder, int position) {
        if (cursor == null) {
            return;
        }

        cursor.moveToPosition(position);

        String taskText = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_TEXT));
        Integer taskId = cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_ID));

        holder.setTag(taskId);
        holder.setText(taskText);
        holder.setSelectMode(this.isSelectMode());

        if (this.isSelectMode()) {
            holder.setSelected(TasksRVAdapter.selectedTasks.contains(taskId));
        }
    }

    @Override
    public int getItemCount() {
        if (cursor == null) {
            return 0;
        }

        return cursor.getCount();
    }

    public void setOnSelectModeChanged(OnSelectModeChanged onSelectModeChanged) {
        this.onSelectModeChanged = onSelectModeChanged;
    }

    public void setOnChangeState(OnChangeState onChangeState) {
        this.onChangeState = onChangeState;
    }

    private void selectTask(Integer taskId) {
        if (selectedTasks.contains(taskId)) {
            selectedTasks.remove(taskId);
        } else {
            selectedTasks.add(taskId);
        }

        this.notifyDataSetChanged();
    }

    private void moveTask(Integer taskId) {
        if (this.onChangeState != null) {
            this.onChangeState.onChangeState(taskId);
        }
    }

    public void setSelectMode(boolean isSelectMode) {
        if (!isSelectMode) {
            selectedTasks.clear();
        }

        selectMode = isSelectMode;
        notifyDataSetChanged();

        if (this.onSelectModeChanged != null) {
            this.onSelectModeChanged.onSelectModeChanged(isSelectMode);
        }
    }

    public List<Integer> getSelectedTasks() {
        return selectedTasks;
    }

    public boolean isSelectMode() {
        return selectMode;
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnLongClickListener, View.OnClickListener {
        private View view;
        private final TasksRVAdapter adapter;

        ViewHolder(View v, TasksRVAdapter adapter) {
            super(v);
            v.setOnLongClickListener(this);
            v.setOnClickListener(this);

            this.adapter = adapter;
            this.view = v;
        }

        void setText(String text) {
            TextView tv = (TextView) this.view.findViewById(R.id.taskTextView);
            tv.setText(text);
        }

        void setTag(int tag) {
            this.view.setTag(tag);
        }

        void setSelectMode(boolean isSelectMode) {
            view.findViewById(R.id.imageDocIcon).setVisibility(isSelectMode ? View.GONE : View.VISIBLE);
            view.findViewById(R.id.checkboxTask).setVisibility(isSelectMode ? View.VISIBLE : View.GONE);
        }

        void setSelected(boolean selected) {
            ImageView checkboxTask = (ImageView) view.findViewById(R.id.checkboxTask);
            checkboxTask.setImageResource(selected ? R.drawable.checkbox_marked_outline : R.drawable.checkbox_blank_outline);
        }

        @Override
        public void onClick(View v) {
            if (adapter.isSelectMode()) {
                adapter.selectTask((Integer) v.getTag());
            } else {
                adapter.moveTask((Integer) v.getTag());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            adapter.setSelectMode(true);
            adapter.selectTask((Integer) v.getTag());
            return true;
        }


    }
}
