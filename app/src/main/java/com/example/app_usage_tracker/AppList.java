package com.example.app_usage_tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static android.app.AppOpsManager.MODE_ALLOWED;

public class AppList extends AppCompatActivity {
    public static final String TAG = "temp";
    private HashMap<String, AppUsageInfo> appsUsageInfo;
    private AppListAsyncTask appListAsyncTask;

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

        sharedPreference = getSharedPreferences(APP_LIST_SHARED_PREFERENCE, MODE_PRIVATE);
        editor = sharedPreference.edit();

        systemAppFilter = sharedPreference.getBoolean(SYSTEM_APP_FILTER, false);
        unusedAppFilter = sharedPreference.getBoolean(UNUSED_APP_FILTER, true);
        sortOrderAscending = sharedPreference.getBoolean(ASCENDING_SORT, false);
        sortBy = sharedPreference.getInt(SORT_BY, R.id.sort_by_usage_time);

        testThings();
        checkIfFirst();
        if(isUsagePermissionEnabled() == true){
            AppsDataController.startAlarm(this, 500);
        }
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

    @Override
    protected void onResume() {
        super.onResume();

        if (isUsagePermissionEnabled() == false) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            finish();
            return;
        } else {
            appListAsyncTask = new AppListAsyncTask(this);
            appListAsyncTask.execute();
//            long startTime = ImportantStuffs.getDayStartingHour(), endTime = ImportantStuffs.getCurrentTime();
//            appsUsageInfo = AppsDataController.getAppsAllInfo(startTime, endTime, this);
//            createAppList();
        }
    }


    private boolean checkIfFirst(){
        String info = ImportantStuffs.getStringFromJsonObjectPath("info.json", this);
        if(info == ""){
            ImportantStuffs.showLog("No checkpoint data found. Creating new checkpoint---");
            Calendar calendar = Calendar.getInstance();
            JSONObject jsonInfo = new JSONObject();

            calendar.set(Calendar.MILLISECOND,0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            calendar.add(Calendar.DAY_OF_WEEK,1);
            calendar.add(Calendar.WEEK_OF_MONTH,-1);
            calendar.set(Calendar.AM_PM, Calendar.AM);

            Long time = calendar.getTimeInMillis();
            try {
                jsonInfo.put("checkpoint", time);
                jsonInfo.put("appsInfo", new JSONObject());
            } catch (JSONException e) {
                e.printStackTrace();
                ImportantStuffs.showErrorLog("Checkpoint can't be initialized");
            }
            if(!ImportantStuffs.saveFileLocally("info.json", jsonInfo.toString(), this))
                ImportantStuffs.showErrorLog("Checkpoint can't be initialized");
            return true;
        }
        return false;
    }

    private void testThings() {
//        try{
//            String path = getExternalFilesDir("").getAbsolutePath();
//            ImportantStuffs.showLog(path);
//            File file = new File(path, "testFile8.json");
//            FileWriter fw = new FileWriter(file.getAbsoluteFile());
//            BufferedWriter bw = new BufferedWriter(fw);
//            bw.write("hello :(");
//            bw.close();
//            fw.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            ImportantStuffs.showLog("Can't save file :(");
//        }
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

    private boolean isUsagePermissionEnabled() {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, appInfo.uid, getPackageName());
            return mode == MODE_ALLOWED;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showToast(int message) {
        showToast(Integer.toString(message));
    }

    private void showToast(String... messages) {
        String fullMessage = "";
        for (String message : messages) {
            fullMessage += message + " ";
        }
        showToast(fullMessage);
    }

    private void showToast(int... messages) {
        String fullMessage = "";
        for (int message : messages) {
            fullMessage += message + " ";
        }
        showToast(fullMessage);
    }


    private class AppListAsyncTask extends AsyncTask<Void, Void, Void>{
        private WeakReference<AppList> activityWeakReference;

        AppListAsyncTask(AppList activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... aVoid) {
            AppList activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }
            long startTime = ImportantStuffs.getDayStartingHour(), endTime = ImportantStuffs.getCurrentTime();
            appsUsageInfo = AppsDataController.getAppsAllInfo(startTime, endTime, activity);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AppList activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.createAppList();
            super.onPostExecute(aVoid);
        }
    }
}