package com.example.app_usage_tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static com.example.app_usage_tracker.MyBroadcastReceiver.getAppName;

public class AppList extends AppCompatActivity {

    private static final int REQUEST_READ_PHONE_STATE = 3200;
    private final String TAG = "ahtrap";
    private HashMap<String, AppUsageInfo> appsUsageInfo = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        testThings();

        if(checkForUsagePermission() == false){
            // wait for permission to be granted------------------------------------
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return;
        }
        else{
            initAppsUsageInfo();
            createAppList();
        }
    }

    private void testThings(){

    }

    private void initAppsUsageInfo(){
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        Long dayStartTime = ImportantMethods.getDayStartTime();
        Long dayEndTime = ImportantMethods.getDayEndTime();
        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(dayStartTime, dayEndTime);
        for(UsageStats stat:stats.values()){

            String packageName = stat.getPackageName();
            String appName = getAppName(packageName, this);
            Long usageTime = stat.getTotalTimeInForeground();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                usageTime = stat.getTotalTimeVisible();
            }
            Long lastUsedTime = stat.getLastTimeUsed();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                lastUsedTime = stat.getLastTimeVisible();
            }
            Long installationDate = getAppInstallationDate(packageName);
            boolean isSystem = isSystemPackage(packageName);
            Drawable appIcon = getAppIcon(packageName);

            AppUsageInfo appUsageInfo = new AppUsageInfo(appName, packageName, appIcon, installationDate, usageTime, lastUsedTime, isSystem);
            appsUsageInfo.put(packageName, appUsageInfo);


//            String usageTimeString = ImportantMethods.getTimeFromMillisecond(usageTime);
//            String lastUsedTimeString = ImportantMethods.getTimeInAgoFromMillisecond(lastUsedTime);
//            showLog("\n"+appName, "-->", "Used time:", usageTimeString, "|||| Last used time", "-->", lastUsedTimeString);
        }

        UsageEvents events = usageStatsManager.queryEvents(dayStartTime, dayEndTime);
        UsageEvents.Event currentEvent = new UsageEvents.Event();
        while (events.hasNextEvent()){
            events.getNextEvent(currentEvent);
            String packageName = currentEvent.getPackageName();
            AppUsageInfo appUsageInfo = appsUsageInfo.get(packageName);
            if(appUsageInfo != null){
                if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED)
                    appUsageInfo.incrementLaunchCount();
            }
        }

        return;
    }

    private void createAppList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        ArrayList<AppUsageInfo> appsInfoList = new ArrayList<>(appsUsageInfo.values());
        AppListAdapter adapter = new AppListAdapter(this, appsInfoList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.sortByLastOpened(false);
    }

    private boolean checkForUsagePermission() {
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

    private Drawable getAppIcon(String packageName){
        Drawable appIcon = null;
        try {
            appIcon = getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appIcon;
    }

    private long getAppInstallationDate(String packageName){
        long installed = 0;
        try {
            installed = getPackageManager().getPackageInfo(packageName, 0).firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {

        }
        return installed;
    }

    public boolean isSystemPackage(String packageName) {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName, 0);
            return ((ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);
        }catch (PackageManager.NameNotFoundException e) {
            showLog(getAppName(packageName, this));
            return false;
        }
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

    private void showLog(String message) {
        Log.v(TAG, message);
    }

    private void showLog(int message) {
        showLog(Integer.toString(message));
    }

    private void showLog(int... messages) {
        String fullMessage = "";
        for (int message : messages) {
            fullMessage += message + " ";
        }
        showLog(fullMessage);
    }

    private void showLog(String... messages) {
        String fullMessage = "";
        for (String message : messages) {
            fullMessage += message + " ";
        }
        showLog(fullMessage);
    }
}