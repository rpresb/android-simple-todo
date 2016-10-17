package com.rpresb.presbato_do;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.rpresb.presbato_do.adapters.TasksRVAdapter;
import com.rpresb.presbato_do.events.OnChangeState;
import com.rpresb.presbato_do.events.OnSelectModeChanged;
import com.rpresb.presbato_do.events.OnTaskCompleted;
import com.rpresb.presbato_do.helpers.DBHelper;
import com.rpresb.presbato_do.helpers.TaskDeleteHelper;
import com.rpresb.presbato_do.helpers.TaskDownloadHelper;
import com.rpresb.presbato_do.helpers.TaskStatusChangeHelper;
import com.rpresb.presbato_do.providers.TasksProvider;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int EDITOR_REQUEST_CODE = 1000;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private FloatingActionButton buttonAddNewTask;
    private FloatingActionButton buttonDeleteTask;
    private PlaceholderFragment fragment;
    private boolean selectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        buttonAddNewTask = (FloatingActionButton) findViewById(R.id.fab);
        buttonAddNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditor(view);
            }
        });

        buttonDeleteTask = (FloatingActionButton) findViewById(R.id.delete_tasks);
        buttonDeleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTasks(getSelectedTasks());
            }
        });
    }

    private void deleteTasks(final List<Integer> tasksId) {
        TaskDeleteHelper taskDeleteHelper = new TaskDeleteHelper(getSelectedTasks(), this);
        taskDeleteHelper.execute();
        taskDeleteHelper.setOnTaskCompleted(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted() {
                reloadData();
            }
        });
    }

    private void openEditor(View view) {
        stopSelectMode();
        Intent intent = new Intent(this, EditorActivity.class);
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    private List<Integer> getSelectedTasks() {
        return fragment.getSelectedTasks();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            getFragmentManager().executePendingTransactions();
            reloadData();
        }
    }

    public void reloadData() {
        fragment.restartLoader();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (this.selectMode) {
            buttonAddNewTask.setVisibility(View.GONE);
            buttonDeleteTask.setVisibility(View.VISIBLE);
            getMenuInflater().inflate(R.menu.menu_main_select_mode, menu);
        } else {
            buttonDeleteTask.setVisibility(View.GONE);
            buttonAddNewTask.setVisibility(View.GONE);

            if (mViewPager.getCurrentItem() == 0) {
                getMenuInflater().inflate(R.menu.menu_main, menu);
                buttonAddNewTask.setVisibility(View.VISIBLE);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_task:
                openEditor(this.buttonAddNewTask);
                break;
            case R.id.action_remove_tasks:
                deleteTasks(getSelectedTasks());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (this.selectMode) {
            stopSelectMode();
        } else {
            super.onBackPressed();
        }
    }

    private void changeTaskState(Integer taskId) {
        TaskStatusChangeHelper taskStatusChangeHelper = new TaskStatusChangeHelper(taskId, this);
        taskStatusChangeHelper.execute();
        taskStatusChangeHelper.setOnTaskCompleted(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted() {
                reloadData();
            }
        });
    }

    private void setSelectMode(boolean isSelectMode) {
        this.selectMode = isSelectMode;
    }

    private void stopSelectMode() {
        this.setSelectMode(false);
        if (fragment != null) {
            fragment.stopSelectMode();
        }
    }

    public void setCurrentFragment(PlaceholderFragment fragment, boolean overwrite) {
        if (this.fragment == null || overwrite) {
            this.stopSelectMode();
            this.fragment = fragment;
        }
    }

    public static class PlaceholderFragment
            extends Fragment
            implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private TasksRVAdapter tasksRVAdapter;
        private RecyclerView recyclerView;
        private SwipeRefreshLayout swipeRefreshLayout;

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber, Activity mainActivity) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            ((MainActivity) mainActivity).setCurrentFragment(fragment, false);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setOnRefreshListener(this);

            tasksRVAdapter = new TasksRVAdapter();
            tasksRVAdapter.setOnSelectModeChanged(new OnSelectModeChanged() {
                @Override
                public void onSelectModeChanged(boolean isSelectMode) {
                    ((MainActivity) getActivity()).setSelectMode(isSelectMode);
                    getActivity().invalidateOptionsMenu();
                }
            });
            tasksRVAdapter.setOnChangeState(new OnChangeState() {
                @Override
                public void onChangeState(Integer taskId) {
                    ((MainActivity) getActivity()).changeTaskState(taskId);
                }
            });

            recyclerView = (RecyclerView) rootView.findViewById(R.id.tasksRecyclerView);
            recyclerView.setAdapter(tasksRVAdapter);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());
            recyclerView.setLayoutManager(mLayoutManager);

            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                this.onRefresh();
            }

            return rootView;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);

            if (isVisibleToUser && getActivity() != null) {
                getActivity().invalidateOptionsMenu();
                ((MainActivity) getActivity()).setCurrentFragment(this, true);

                restartLoader();
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            swipeRefreshLayout.setRefreshing(true);

            String selection = DBHelper.TASK_STATE + "=" + (getArguments().getInt(ARG_SECTION_NUMBER) - 1);

            return new CursorLoader(this.getContext(), TasksProvider.CONTENT_URI, null, selection, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            tasksRVAdapter.swapCursor(data);
            recyclerView.setAdapter(tasksRVAdapter);
            tasksRVAdapter.notifyDataSetChanged();

            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            tasksRVAdapter.swapCursor(null);
            tasksRVAdapter.notifyDataSetChanged();
        }

        private void restartLoader() {
            getLoaderManager().restartLoader(0, null, this);
        }

        private void stopSelectMode() {
            tasksRVAdapter.setSelectMode(false);
        }

        private List<Integer> getSelectedTasks() {
            return tasksRVAdapter.getSelectedTasks();
        }

        @Override
        public void onRefresh() {
            swipeRefreshLayout.setRefreshing(true);

            // I have decided to download data from web every time the swipe to refresh is performed
            // in order to show how I handle this approach when working with data remote and locally
            // simultaneously.
            TaskDownloadHelper taskDownloadHelper = new TaskDownloadHelper(getResources().getString(R.string.data_url), (MainActivity) this.getActivity());
            taskDownloadHelper.execute();
            taskDownloadHelper.setOnTaskCompleted(new OnTaskCompleted() {
                @Override
                public void onTaskCompleted() {
                    restartLoader();
                    swipeRefreshLayout.setRefreshing(false);

                }
            });
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Activity mainActivity;

        SectionsPagerAdapter(FragmentManager fm, Activity activity) {
            super(fm);
            this.mainActivity = activity;
        }

        private String[] tabs = {"Pending", "Done"};

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1, this.mainActivity);
        }

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }
    }
}
