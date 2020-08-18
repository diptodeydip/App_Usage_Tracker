package com.example.app_usage_tracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class ImportantStuffs {

    public static final long MILLISECONDS_IN_HOUR = 3600000L, MILLISECONDS_IN_DAY = 24*MILLISECONDS_IN_HOUR;
    public static final String SHARED_PREFERENCE = "AppUsageData", TAG = "ahtrap";
    public static final String LAUNCHER_PACKAGE = "com.example.ui", THIS_APP_PACKAGE = "com.example.app_usage_tracker";
    public static final String SETTINGS_PACKAGE = "com.android.settings", FILE_MANAGER_PACKAGE = "com.amaze.filemanager";
    private static final Random random = new Random();


    public static String getAppName(String packageName, Context context) {
        String appName = packageName;
        PackageManager mPm = context.getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = mPm.getApplicationInfo(packageName, 0);
            appName = appInfo.loadLabel(mPm).toString();
            if (appName == null)
                appName = packageName;
        } catch (Exception e) {

        }

        return appName;
    }

    public static Drawable getAppIcon(String packageName, Context context) {
        Drawable appIcon = null;
        try {
            appIcon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appIcon;
    }

    public static long getAppInstallationDate(String packageName, Context context) {
        long installed = 0;
        try {
            installed = context.getPackageManager().getPackageInfo(packageName, 0).firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {

        }
        return installed;
    }

    public static boolean isSystemPackage(String packageName, Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(packageName, 0);
            return ((ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);
        } catch (PackageManager.NameNotFoundException e) {
//            showLog(getAppName(packageName, context));
            return false;
        }
    }


    public static long getDayStartingHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTimeInMillis();
    }

    public static long getDayEndingHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        return calendar.getTimeInMillis();
    }

    public static long getDayEndTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        return calendar.getTimeInMillis();
    }

    public static long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static long getRecentHourFromTime(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTimeInMillis();
    }

    public static long getRecentWeekFromTime(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.DAY_OF_WEEK, 1);
        return calendar.getTimeInMillis();
    }

    public static long getCurrentHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTimeInMillis();
    }

    public static String getDateAndTimeFromMilliseconds(long milliseconds) {
        String dateFormat = "dd MMM yyyy HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatter.format(calendar.getTime());
    }

    public static String getDateFromMilliseconds(long milliseconds){
        String dateFormat = "dd MMM yyyy";
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatter.format(calendar.getTime());
    }

    public static String getDayAndMonthFromMilliseconds(long milliseconds){
        String dateFormat = "dd MMM";
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatter.format(calendar.getTime());
    }

    public static String getTimeFromMillisecond(long time){
        String timeString = "";
        Long second = time / 1000;
        Long min = second / 60;
        Long hour = min / 60;
        Long min_hour = min % 60;
        if(hour > 0)
            timeString += hour + " hour " + min_hour + " min";
        else
            timeString = min_hour + " min";
        return timeString;
    }

    public static float getMinuteFromTime(Long time){
        float minute = time / (1000 * 60);
        return Math.min(minute, 60);
    }

    public static float getHourFromTime(Long time){
        float minute = time / ImportantStuffs.MILLISECONDS_IN_HOUR;
        return Math.min(minute, 24);
    }

    public static String getTimeInAgoFromMillisecond(long time){
        if(time == 0)
            return "";
        long interval = Calendar.getInstance().getTimeInMillis() - time;
        String agoTime = getTimeFromMillisecond(interval);
        return agoTime + " ago";
    }


    public static String readJSON(String fileName, Context context) {
        try {
            String path = context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath();
            File file = new File(path + "/" + "AppUsageTracker/" + fileName);

            StringBuilder data = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                data.append(line);
                data.append("\n");
            }
            br.close();

            return data.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static void saveToPhone(String json, String fileName, Context context) {
        try {
            String path = context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath();

            File dir = new File(path + "/AppUsageTracker/");
            dir.mkdirs();
            File file = new File(dir, fileName);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(json);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playRandomSound(Context context){
        MediaPlayer mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_ALARM_ALERT_URI);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
    }

    public static int getRandomInt(int maxValue){
        return random.nextInt(maxValue);
    }


    public static void showLog(String message) {
        Log.v(TAG, message);
    }

    public static void showLog(String... messages) {
        String fullMessage = "";
        for (String message : messages) {
            fullMessage += message + " ";
        }
        showLog(fullMessage);
    }

    public static void showLog(int message) {
        showLog(Integer.toString(message));
    }

    public static void showLog(int... messages) {
        String fullMessage = "";
        for (int message : messages) {
            fullMessage += message + " ";
        }
        showLog(fullMessage);
    }

    public static void showLog(long message) {
        showLog(Long.toString(message));
    }

    public static void showLog(long... messages) {
        String fullMessage = "";
        for (long message : messages) {
            fullMessage += message + " ";
        }
        showLog(fullMessage);
    }


//    private HashMap<String, AppUsageInfo> getAllAppsInfo() {
//        PackageManager pm = getPackageManager();
//        HashMap<String, AppUsageInfo> apps = new HashMap<>();
//        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
//
//        for (int i = 0; i < packs.size(); i++) {
//            PackageInfo p = packs.get(i);
//            String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
//            Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
//            String packageName = p.applicationInfo.packageName;
//            long installed = 0;
//            try {
//                installed = pm.getPackageInfo(packageName, 0).firstInstallTime;
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
//            boolean isSystem = isSystemPackage(p);
//            apps.put(packageName, new AppUsageInfo(appName, packageName, icon, installed, isSystem));
//        }
//        return apps;
//    }


//    private boolean hasUsagePermission(){
//        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//        UsageEvents usageEvents = mUsageStatsManager.queryEvents(getDayStartTime(), getDayEndTime());
//        return usageEvents.hasNextEvent();
//    }



//    private void initAppsUsageInfo(){
//        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//        Long dayStartTime = ImportantMethods.getDayStartTime();
//        Long dayEndTime = ImportantMethods.getDayEndTime();
//        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(dayStartTime, dayEndTime);
//        for(UsageStats stat:stats.values()){
//
//            String packageName = stat.getPackageName();
//            String appName = getAppName(packageName, this);
//            Long usageTime = stat.getTotalTimeInForeground();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                usageTime = stat.getTotalTimeVisible();
//            }
//            Long lastUsedTime = stat.getLastTimeUsed();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                lastUsedTime = stat.getLastTimeVisible();
//            }
//            Long installationDate = getAppInstallationDate(packageName);
//            boolean isSystem = isSystemPackage(packageName);
//            Drawable appIcon = getAppIcon(packageName);
//
//            AppUsageInfo appUsageInfo = new AppUsageInfo(appName, packageName, appIcon, installationDate, usageTime, lastUsedTime, isSystem);
//            appsUsageInfo.put(packageName, appUsageInfo);
//
////            String usageTimeString = ImportantMethods.getTimeFromMillisecond(usageTime);
////            String lastUsedTimeString = ImportantMethods.getTimeInAgoFromMillisecond(lastUsedTime);
////            showLog("\n"+appName, "-->", "Used time:", usageTimeString, "|||| Last used time", "-->", lastUsedTimeString);
//        }
//
//        UsageEvents events = usageStatsManager.queryEvents(dayStartTime, dayEndTime);
//        UsageEvents.Event currentEvent = new UsageEvents.Event();
//        while (events.hasNextEvent()){
//            events.getNextEvent(currentEvent);
//            String packageName = currentEvent.getPackageName();
//            AppUsageInfo appUsageInfo = appsUsageInfo.get(packageName);
//            if(appUsageInfo != null){
//                if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED)
//                    appUsageInfo.incrementLaunchCount();
//            }
//        }
//
//        return;
//    }


//    void checkIfFirst(){
//        String jsonString =  ImportantMethods.readJSON("details.json",this);
//
//        if(jsonString=="") {
//            Calendar calendar = Calendar.getInstance();
//            // JSONObject userDetails = new JSONObject();
//
//            calendar.set(Calendar.MILLISECOND,0);
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.HOUR, 0);
//            calendar.add(Calendar.DAY_OF_WEEK,1);
//            calendar.add(Calendar.WEEK_OF_MONTH,-1);
//            calendar.set(Calendar.AM_PM, Calendar.AM);
//
//            long Time = calendar.getTimeInMillis();
//            showLog(ImportantMethods.getDateFromMilliseconds(Time));
//
//            try {
//                JSONObject userDetails = new JSONObject();
//                userDetails.put("InstallationTime" , calendar.getTimeInMillis());
//                userDetails.put("checkPoint" , Time);
//                MyBroadcastReceiver.saveToPhone(userDetails.toString(),"details.json" ,this);
//            } catch (JSONException e) {
//                Toast.makeText(this,"error",Toast.LENGTH_LONG).show();
//            }
//        }
////
////        String jsonString1 =  MyBroadcastReceiver.readJSON("userInfo.json",this);
////        if(jsonString1!="") {
////            startActivity(new Intent(Info.this,MainActivity.class));
////            finish();
////        }
//    }


//    public void initAppsUsageInfo() {
//        appsUsageInfo = new HashMap<>();
//        long startTime = ImportantMethods.getDayStartTime();
//        long endTme = ImportantMethods.getDayEndTime();
//
//        UsageEvents.Event currentEvent;
//        HashMap<String, List<UsageEvents.Event>> sameEvents = new HashMap<>();
//        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//        UsageEvents usageEvents = mUsageStatsManager.queryEvents(startTime, endTme);
//
//        while (usageEvents.hasNextEvent()) {
//            currentEvent = new UsageEvents.Event();
//            usageEvents.getNextEvent(currentEvent);
//
//            final int RESUMED = UsageEvents.Event.ACTIVITY_RESUMED;
//            final int PAUSED = UsageEvents.Event.ACTIVITY_PAUSED;
//            if (currentEvent.getEventType() == RESUMED || currentEvent.getEventType() == PAUSED) {
//                String packageName = currentEvent.getPackageName();
//                String appName = getAppName(packageName, this);
//                Drawable icon = getAppIcon(packageName);
//                Long installationDate = getAppInstallationDate(packageName);
//                boolean isSystemApp = isSystemPackage(packageName);
//
//                if (appsUsageInfo.get(packageName) == null) {
//                    appsUsageInfo.put(packageName, new AppUsageInfo(appName, packageName, icon, installationDate, isSystemApp));
//                    sameEvents.put(packageName, new ArrayList<>());
//                }
//                sameEvents.get(packageName).add(currentEvent);
//            }
//        }
//        addOtherAppsToAppsUsageInfo();
//
//        // Traverse through each app data and count launch, calculate duration
//        for (Map.Entry<String, List<UsageEvents.Event>> entry : sameEvents.entrySet()) {
//            int totalEvents = entry.getValue().size();
//            if (totalEvents > 1) {
//                for (int i = 0; i < totalEvents - 1; i++) {
//                    UsageEvents.Event E0 = entry.getValue().get(i);
//                    UsageEvents.Event E1 = entry.getValue().get(i + 1);
//
//                    if (E1.getEventType() == 1) {
//                        appsUsageInfo.get(E1.getPackageName()).incrementLaunchCount();
//                    }
//                    if (E0.getEventType() == 1) {
//                        appsUsageInfo.get(E1.getPackageName()).incrementLaunchCount();
//                    }
//
//                    if (E0.getEventType() == 1 && E1.getEventType() == 2) {
//                        long diff = E1.getTimeStamp() - E0.getTimeStamp();
//                        appsUsageInfo.get(E0.getPackageName()).addToTimeInForeground(diff);
//                    }
//                }
//            }
//            // shurur event jodi app closing hoy taile start_time and app closing time er difference add korlam
//            if (entry.getValue().get(0).getEventType() == 2) {
//                long diff = entry.getValue().get(0).getTimeStamp() - startTime;
//                appsUsageInfo.get(entry.getValue().get(0).getPackageName()).addToTimeInForeground(diff);
//            }
//            //shesher event jodi app starting hoy  tahole app starting time and end_time er diiferece add korlaam
//            if (entry.getValue().get(totalEvents - 1).getEventType() == 1) {
//                appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).incrementLaunchCount();
//                long diff = endTme - entry.getValue().get(totalEvents - 1).getTimeStamp();
//                appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).addToTimeInForeground(diff);
//            }
//
//            appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).lastTimeUsed = entry.getValue().get(totalEvents - 1).getTimeStamp();
//
//        }
//
//    }

//    private void dunno(){
//        for (Map.Entry<String, List<UsageEvents.Event>> entry : sameEvents.entrySet()) {
//            ImportantStuffs.showLog(packageName + " -> " + totalEvents, " -> " + ImportantStuffs.getTimeFromMillisecond(usageTime));

//            if (totalEvents > 1) {
//                for (int i = 0; i < totalEvents - 1; i++) {
//                    UsageEvents.Event E0 = entry.getValue().get(i);
//                    UsageEvents.Event E1 = entry.getValue().get(i + 1);
//
//                    if (E1.getEventType() == 1) {
//                        appsUsageInfo.get(E1.getPackageName()).incrementLaunchCount();
//                    }
//                    if (E0.getEventType() == 1) {
//                        appsUsageInfo.get(E1.getPackageName()).incrementLaunchCount();
//                    }
//
//                    if (E0.getEventType() == 1 && E1.getEventType() == 2) {
//                        long diff = E1.getTimeStamp() - E0.getTimeStamp();
//                        appsUsageInfo.get(E0.getPackageName()).addToTimeInForeground(diff);
//                    }
//                }
//            }
//
//            // shurur event jodi app closing hoy taile start_time and app closing time er difference add korlam
//            if (entry.getValue().get(0).getEventType() == 2) {
//                long diff = entry.getValue().get(0).getTimeStamp() - startTime;
//                appsUsageInfo.get(entry.getValue().get(0).getPackageName()).addToTimeInForeground(diff);
//            }
//
//            // shesher event jodi app starting hoy  tahole app starting time and end_time er diiferece add korlaam
//            if (entry.getValue().get(totalEvents - 1).getEventType() == 1) {
//                appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).incrementLaunchCount();
//                long diff = endTime - entry.getValue().get(totalEvents - 1).getTimeStamp();
//                appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).addToTimeInForeground(diff);
//            }
//
//            appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).lastTimeUsed = entry.getValue().get(totalEvents - 1).getTimeStamp();
//        }
//    }

//    private BarDataSet getRandomBarDataSet(){
//        ArrayList<BarEntry> dataValues = new ArrayList<>();
//        for (int i = 0; i < 24; i++) {
//            dataValues.add(new BarEntry(i, ImportantStuffs.getRandomInt(60)));
//        }
//        BarDataSet barDataSet = new BarDataSet(dataValues, "");
//        barDataSet.setColor(ContextCompat.getColor(this, R.color.barGraphBarColor1));
//        barDataSet.setDrawValues(false);
//        return barDataSet;
//    }
}
