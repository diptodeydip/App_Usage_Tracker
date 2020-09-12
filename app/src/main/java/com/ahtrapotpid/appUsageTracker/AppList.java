package com.ahtrapotpid.appUsageTracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class AppList extends AppCompatActivity {
    private HashMap<String, AppUsageInfo> appsUsageInfo;

    private SwipeRefreshLayout refreshLayout;

    AppListAdapter adapter;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    int sortBy;
    boolean systemAppFilter, unusedAppFilter, sortOrderAscending;
    public static final String SYSTEM_APP_FILTER = "systemAppFilter", UNUSED_APP_FILTER = "unusedAppFilter";
    public static final String APP_LIST_SHARED_PREFERENCE = "AppListSharedPreference";
    public static final String ASCENDING_SORT = "sortOrderAscending", SORT_BY = "sortBy";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        testThings();
        sharedPreference = getSharedPreferences(APP_LIST_SHARED_PREFERENCE, MODE_PRIVATE);
        editor = sharedPreference.edit();

        systemAppFilter = sharedPreference.getBoolean(SYSTEM_APP_FILTER, false);
        unusedAppFilter = sharedPreference.getBoolean(UNUSED_APP_FILTER, true);
        sortOrderAscending = sharedPreference.getBoolean(ASCENDING_SORT, false);
        sortBy = sharedPreference.getInt(SORT_BY, R.id.sort_by_usage_time);


        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            new AppListAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        });

        new AppListAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_list_menu, menu);

        try {
            menu.findItem(R.id.system_app_filter).setChecked(systemAppFilter);
            menu.findItem(R.id.unused_app_filter).setChecked(unusedAppFilter);
            menu.findItem(R.id.sort_ascending).setChecked(sortOrderAscending);
            menu.findItem(sortBy).setChecked(true);
        } catch (Exception e){
            e.printStackTrace();
            ImportantStuffs.showErrorLog("Menu item check failed");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        systemAppFilter = sharedPreference.getBoolean(SYSTEM_APP_FILTER, false);
        unusedAppFilter = sharedPreference.getBoolean(UNUSED_APP_FILTER, true);
        sortOrderAscending = sharedPreference.getBoolean(ASCENDING_SORT, false);
        sortBy = sharedPreference.getInt(SORT_BY, R.id.sort_by_usage_time);


        switch (item.getItemId()){
            case R.id.system_app_filter:
                systemAppFilter = !item.isChecked();
                item.setChecked(systemAppFilter);

                adapter.filterApps(systemAppFilter, unusedAppFilter);
                editor.putBoolean(SYSTEM_APP_FILTER, systemAppFilter);
                break;

            case R.id.unused_app_filter:
                unusedAppFilter = !item.isChecked();
                item.setChecked(unusedAppFilter);

                adapter.filterApps(systemAppFilter, unusedAppFilter);
                editor.putBoolean(UNUSED_APP_FILTER, unusedAppFilter);
                break;


            case R.id.sort_by_name:
                sortBy = R.id.sort_by_name;
                item.setChecked(true);

                editor.putInt(SORT_BY, sortBy);
                break;

            case R.id.sort_by_last_installed:
                sortBy = R.id.sort_by_last_installed;
                item.setChecked(true);

                editor.putInt(SORT_BY, sortBy);
                break;

            case R.id.sort_by_last_used:
                sortBy = R.id.sort_by_last_used;
                item.setChecked(true);

                editor.putInt(SORT_BY, sortBy);
                break;

            case R.id.sort_by_usage_time:
                sortBy = R.id.sort_by_usage_time;
                item.setChecked(true);

                editor.putInt(SORT_BY, sortBy);
                break;

            case R.id.sort_ascending:
                sortOrderAscending = !item.isChecked();
                item.setChecked(sortOrderAscending);
                editor.putBoolean(ASCENDING_SORT, sortOrderAscending);
                break;
        }

        adapter.sort(sortBy, sortOrderAscending);
        editor.commit();
        return true;
    }

    private void testThings() {
//        Toast.makeText(this, "App list started", Toast.LENGTH_SHORT).show();
//        refreshLayout.getpro
    }

    public void createAppList() {
        RecyclerView recyclerView = findViewById(R.id.app_list_recycler_view);
        ArrayList<AppUsageInfo> appsInfoList = new ArrayList<>(appsUsageInfo.values());
        adapter = new AppListAdapter(this, appsInfoList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.filterApps(systemAppFilter, unusedAppFilter);
        adapter.sort(sortBy, sortOrderAscending);
    }

    private class AppListAsyncTask extends AsyncTask<Void, Void, Void>{
        private WeakReference<AppList> activityWeakReference;
        ProgressDialog progressDialog;

        AppListAsyncTask(AppList activity) {
            activityWeakReference = new WeakReference<>(activity);
            progressDialog = new ProgressDialog(activity, R.style.DialogTheme);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Loading apps usage info...");
            progressDialog.setMessage("Wait a moment");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("flag", "AppListAsyncTask: started");
            AppList activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }
            long startTime = ImportantStuffs.getDayStartingHour(), endTime = ImportantStuffs.getCurrentTime();
            {
                // temporary block
                String startDate = ImportantStuffs.getDateAndTimeFromMilliseconds(startTime);
                String endDate = ImportantStuffs.getDateAndTimeFromMilliseconds(endTime);
                Log.d("temp", startDate + " " + endDate);
            }
            activity.appsUsageInfo = AppsDataController.getAppsUsageInfo(startTime, endTime, activity);
            for(AppUsageInfo info:activity.appsUsageInfo.values()){
                Log.d("extra", info.toString());
            }

            activity.appsUsageInfo = AppsDataController.addOtherAppsInfo(activity.appsUsageInfo, activity);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AppList activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.createAppList();
            progressDialog.cancel();
            Log.d("flag", "AppListAsyncTask: ended");
        }
    }
}