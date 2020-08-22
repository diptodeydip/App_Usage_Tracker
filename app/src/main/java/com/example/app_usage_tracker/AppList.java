package com.example.app_usage_tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static android.app.AppOpsManager.MODE_ALLOWED;

public class AppList extends AppCompatActivity {
    private HashMap<String, AppUsageInfo> appsUsageInfo;
    AppListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        testThings();
        checkIfFirst();
        if(isUsagePermissionEnabled() == true){
            AppsDataController.startAlarm(this, 500);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isUsagePermissionEnabled() == false) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            finish();
            return;
        } else {
            long startTime = ImportantStuffs.getDayStartingHour(), endTime = ImportantStuffs.getCurrentTime();
            appsUsageInfo = AppsDataController.getAppsAllInfo(startTime, endTime, this);
            createAppList();
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

//    private void startAlarm() {
//        AlarmManager alarmMgr;
//        PendingIntent alarmIntent;
//
//        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
//        Intent intent = new Intent(this, AppsDataController.class);
//
//        Calendar calendar = Calendar.getInstance();
//
//        long alarmTime = calendar.getTimeInMillis() + 500;
//
//        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
//        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
//    }

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

//    public void addOtherAppsToAppsUsageInfo(){
//        PackageManager pm = getPackageManager();
//        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
//
//        for (int i = 0; i < packs.size(); i++) {
//            PackageInfo p = packs.get(i);
//            String packageName = p.applicationInfo.packageName;
//            if(appsUsageInfo.get(packageName) != null)
//                continue;
//
//            String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
//            Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
//            long installed = 0;
//            try {
//                installed = pm.getPackageInfo(packageName, 0).firstInstallTime;
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
//            boolean isSystem = ImportantStuffs.isSystemPackage(packageName, this);
//
//            appsUsageInfo.put(packageName, new AppUsageInfo(appName, packageName, icon, installed, isSystem));
//        }
//    }

    private void createAppList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        ArrayList<AppUsageInfo> appsInfoList = new ArrayList<>(appsUsageInfo.values());
        adapter = new AppListAdapter(this, appsInfoList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        adapter.sortByUsageTime(false);
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