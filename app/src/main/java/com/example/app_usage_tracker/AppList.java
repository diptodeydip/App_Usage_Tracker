package com.example.app_usage_tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static android.app.AppOpsManager.MODE_ALLOWED;

public class AppList extends AppCompatActivity {
    private final String TAG = "ahtrap";
    private HashMap<String, AppUsageInfo> appsUsageInfo;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        sharedPreferences = getSharedPreferences(ImportantMethods.SHARED_PREFERENCE, MODE_PRIVATE);
        checkIfFirst();
        if(isUsagePermissionEnabled() == true)
            startAlarm();
        testThings();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isUsagePermissionEnabled() == false) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            finish();
            return;
        } else {
            appsUsageInfo = AppUsageDataController.getAppsUsageInfo(ImportantMethods.getDayStartingHour(), ImportantMethods.getDayEndTime(), this);
            addOtherAppsToAppsUsageInfo();
            createAppList();
        }
    }

    private void checkIfFirst(){
        SharedPreferences sharedPreferences = getSharedPreferences("AppUsageData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Long time = sharedPreferences.getLong("checkpoint", 0);
        if(time == 0){
            ImportantMethods.showLog("no checkpoint data");
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.MILLISECOND,0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            calendar.add(Calendar.DAY_OF_WEEK,1);
            calendar.add(Calendar.WEEK_OF_MONTH,-1);
            calendar.set(Calendar.AM_PM, Calendar.AM);

            time = calendar.getTimeInMillis();
            editor.putLong("checkpoint", time);
            editor.commit();
        }
    }

    private void startAlarm() {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AppUsageDataController.class);

        Calendar calendar = Calendar.getInstance();

        long alarmTime = calendar.getTimeInMillis() + 1000 * 5;

        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
    }

    private void testThings() {
        String packageName = "com.android.launcher3";
        long dayStartTime = ImportantMethods.getDayStartingHour();
        ArrayList<Integer>usageData = ImportantMethods.getDailyAppUsageData(dayStartTime, packageName, this);
        Integer totalUsageTime = 0;
        for(Integer hourlyUsage:usageData){
            totalUsageTime += hourlyUsage;
            ImportantMethods.showLog(ImportantMethods.getTimeFromMillisecond(hourlyUsage));
        }
        ImportantMethods.showLog(ImportantMethods.getTimeFromMillisecond(totalUsageTime));
    }

    public void addOtherAppsToAppsUsageInfo(){
        PackageManager pm = getPackageManager();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);

        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            String packageName = p.applicationInfo.packageName;
            if(appsUsageInfo.get(packageName) != null)
                continue;

            String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
            Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
            long installed = 0;
            try {
                installed = pm.getPackageInfo(packageName, 0).firstInstallTime;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            boolean isSystem = ImportantMethods.isSystemPackage(packageName, this);

            appsUsageInfo.put(packageName, new AppUsageInfo(appName, packageName, icon, installed, isSystem));
        }
    }

    private void createAppList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        ArrayList<AppUsageInfo> appsInfoList = new ArrayList<>(appsUsageInfo.values());
        AppListAdapter adapter = new AppListAdapter(this, appsInfoList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.sortByLastOpened(false);
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
}