package com.example.app_usage_tracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AppsDataController extends BroadcastReceiver {
    Context context;
    public static final String TAG = "ahtrap";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            ImportantStuffs.showLog("Device has been rebooted!");
        }

        startAlarm(context, 20*ImportantStuffs.MILLISECONDS_IN_MINUTE);

        new Thread(() -> {
            AsyncUsageTask runner = new AsyncUsageTask();
            runner.execute();
        }).start();
    }

    private class AsyncUsageTask extends AsyncTask<Context, String, String> {

        @Override
        protected void onPostExecute(String result) {
//            Log.d(TAG, "onPostExecute: ");
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected String doInBackground(Context... contexts) {
//            Log.d(TAG, "doInBackground: ");
            saveUsageDataLocally();
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPreExecute() {
//            Log.d(TAG, "onPreExecute: ");
//            checkTargetLocally(context);
        }

        @Override
        protected void onProgressUpdate(String... text) {
//            Log.d(TAG, "onProgressUpdate: ");
        }
    }

    public static void startAlarm(Context context, long delayInMillisecond) {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AppsDataController.class);

        Calendar calendar = Calendar.getInstance();

        long alarmTime = calendar.getTimeInMillis() + delayInMillisecond;

        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
    }


    public static HashMap<String, AppUsageInfo> getAppsUsageInfo(long startTime, long endTime, Context context) {
        HashMap<String, AppUsageInfo> appsUsageInfo = new HashMap<>();

        long currentTime = ImportantStuffs.getCurrentTime();
        if(endTime > currentTime)
            endTime = currentTime;
        if(startTime >= currentTime)
            return appsUsageInfo;

        UsageEvents.Event currentEvent;
        HashMap<String, List<UsageEvents.Event>> sameEvents = new HashMap<>();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(startTime, endTime);

        final int RESUMED = UsageEvents.Event.ACTIVITY_RESUMED;
        final int PAUSED = UsageEvents.Event.ACTIVITY_PAUSED;

        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);

            if (currentEvent.getEventType() == RESUMED || currentEvent.getEventType() == PAUSED) {
                String packageName = currentEvent.getPackageName();
                String appName = ImportantStuffs.getAppName(packageName, context);
                Drawable icon = ImportantStuffs.getAppIcon(packageName, context);
                Long installationDate = ImportantStuffs.getAppInstallationDate(packageName, context);
                boolean isSystemApp = ImportantStuffs.isSystemPackage(packageName, context);

                if (appsUsageInfo.get(packageName) == null) {
                    appsUsageInfo.put(packageName, new AppUsageInfo(appName, packageName, icon, installationDate, isSystemApp));
                    sameEvents.put(packageName, new ArrayList<>());
                }
                sameEvents.get(packageName).add(currentEvent);
            }
        }

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

        for (Map.Entry<String, List<UsageEvents.Event>> entry : sameEvents.entrySet()) {
            String packageName = entry.getKey();
            AppUsageInfo appUsageInfo = appsUsageInfo.get(packageName);
            int launchCount = 0, recentEventType = RESUMED;
            long recentResumedTime = startTime, usageTime = 0, lastUsedTime = 0;

            for(UsageEvents.Event event:entry.getValue()){
                switch (event.getEventType()){
                    case RESUMED:
                        recentResumedTime = event.getTimeStamp();
                        launchCount++;
                        recentEventType = RESUMED;
                        lastUsedTime = event.getTimeStamp();
                        break;
                    case PAUSED:
                        usageTime += (event.getTimeStamp() - recentResumedTime);
                        recentEventType = PAUSED;
                        break;
                }
            }
            if(recentEventType == RESUMED)
                usageTime += endTime - recentResumedTime;

            appUsageInfo.setTimeInForeground(usageTime);
            appUsageInfo.setLaunchCount(launchCount);
            appUsageInfo.setLastTimeUsed(lastUsedTime);
        }

        return appsUsageInfo;
    }

    public static HashMap<String, AppUsageInfo> getAppsAllInfo(long startTime, long endTime, Context context){
        HashMap<String, AppUsageInfo> appsUsageInfo = getAppsUsageInfo(startTime, endTime, context);
        appsUsageInfo = addOtherAppsInfo(appsUsageInfo, context);
        return appsUsageInfo;
    }

    private static HashMap<String, AppUsageInfo> addOtherAppsInfo(HashMap<String, AppUsageInfo> appsInfo, Context context){
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(0);

        for (int i = 0; i < installedPackages.size(); i++) {
            PackageInfo packageInfo = installedPackages.get(i);
            String packageName = packageInfo.applicationInfo.packageName;
            if(appsInfo.get(packageName) != null)
                continue;

            String appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            Drawable icon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
            long installed = 0;
            try {
                installed = packageManager.getPackageInfo(packageName, 0).firstInstallTime;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            boolean isSystem = ImportantStuffs.isSystemPackage(packageName, context);

            appsInfo.put(packageName, new AppUsageInfo(appName, packageName, icon, installed, isSystem));
        }
        return appsInfo;
    }


    public static ArrayList<Long> getWeeklyUsageDataInDailyList(long weekStartTime, String packageName, Context context){
        ArrayList<Long> usage = new ArrayList<>();

        long startTime = weekStartTime;
        long endTime = startTime + 6*ImportantStuffs.MILLISECONDS_IN_DAY;

        for(long currentTime=startTime; currentTime<=endTime; currentTime+=ImportantStuffs.MILLISECONDS_IN_DAY){
            long time = getDailyUsageData(currentTime, packageName, context);
            usage.add(time);
        }
        return usage;
    }

    public static ArrayList<Long> getDailyUsageDataInHourlyList(long dayStartTime, String packageName, Context context) {
        ArrayList<Long> usage = new ArrayList<>();

        long startTime = dayStartTime;
        long endTime = dayStartTime + 23*ImportantStuffs.MILLISECONDS_IN_HOUR;

        for(long currentTime=startTime; currentTime<=endTime; currentTime+=ImportantStuffs.MILLISECONDS_IN_HOUR){
            long time = getHourlyUsageData(currentTime, packageName, context);
            usage.add(time);
        }
        return usage;
    }

    public static long getDailyUsageData(long dayStartTime, String packageName, Context context){
        long usage = 0;

        long startTime = dayStartTime;
        long endTime = dayStartTime + 23*ImportantStuffs.MILLISECONDS_IN_HOUR;

        for(long currentTime=startTime; currentTime<=endTime; currentTime+=ImportantStuffs.MILLISECONDS_IN_HOUR){
            long time = getHourlyUsageData(currentTime, packageName, context);
            usage += time;
        }
        return usage;
    }

    public static long getHourlyUsageData(long hourStartTime, String packageName, Context context){
        long checkpoint = getCheckpoint(context);
        long currentHour = ImportantStuffs.getCurrentHour();
        long currentTime = ImportantStuffs.getCurrentTime();

        if(checkpoint >= hourStartTime){
            String history = ImportantStuffs.getStringFromJsonObjectPath("History.json", context);
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(history);
            } catch (JSONException e) {
                return 0;
            }

            String key = String.valueOf(hourStartTime);
            try {
                JSONArray appArray = jsonObject.getJSONArray(key);
                return getForegroundTime(appArray, packageName);
            } catch (JSONException e) {
                return 0;
            }
        }
        else if(hourStartTime == currentHour){
            HashMap<String, AppUsageInfo> map = getAppsUsageInfo(hourStartTime, currentTime, context);
            try {
                return map.get(packageName).getTimeInForeground();
            } catch (Exception e){
                return 0;
            }
        }
        return 0;
    }


    private static int getForegroundTime(JSONArray array, String packageName) {
        for(int i=0; i<array.length(); i++){
            try {
                JSONObject object = array.getJSONObject(i);
                String value = object.getString("packageName");
                if(value.equals(packageName))
                    return object.getInt("foregroundTime");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return 0;
    }

    private static long getCheckpoint(Context context){
        JSONObject jsonInfo = ImportantStuffs.getJsonObject("info.json", context);
        long checkpoint = 0;
        try {
            checkpoint = jsonInfo.getLong("checkpoint");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return checkpoint;
    }


    private void saveUsageDataLocally() {
        ImportantStuffs.showLog("Checking local data------");
        JSONObject jsonInfo = ImportantStuffs.getJsonObject("info.json", context);
        long checkpoint = getCheckpoint(context);
        long goalPoint = ImportantStuffs.getCurrentHour()-ImportantStuffs.MILLISECONDS_IN_HOUR;
        if(goalPoint == checkpoint){
            ImportantStuffs.showLog("No new data to store");
            return;
        }
        if(checkpoint == 0){
            ImportantStuffs.showErrorLog("No valid checkpoint data found");
            return;
        }

        String jsonString = ImportantStuffs.getStringFromJsonObjectPath("History.json", context);
        JSONObject usageDetails = new JSONObject();
        if (jsonString != "") {
            try {
                usageDetails = new JSONObject(jsonString);
            } catch (Exception e) {
                return;
            }
        }

        for (long startTime = checkpoint + ImportantStuffs.MILLISECONDS_IN_HOUR ; startTime <= goalPoint; startTime += ImportantStuffs.MILLISECONDS_IN_HOUR) {
            HashMap<String, AppUsageInfo> appsUsageInfo = getAppsUsageInfo(startTime, startTime + ImportantStuffs.MILLISECONDS_IN_HOUR, context);
            JSONArray jsonAppInfo = new JSONArray();
            for (AppUsageInfo appInfo:appsUsageInfo.values()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("name", appInfo.getAppName());
                    jsonObject.put("packageName", appInfo.getPackageName());
                    jsonObject.put("foregroundTime", appInfo.getTimeInForeground());
                    jsonObject.put("launchCount", appInfo.getLaunchCount());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonAppInfo.put(jsonObject);
            }

            try {
                usageDetails.put(String.valueOf(startTime), jsonAppInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String startPoint = ImportantStuffs.getDateAndTimeFromMilliseconds(checkpoint + ImportantStuffs.MILLISECONDS_IN_HOUR);
        String endPoint = ImportantStuffs.getDateAndTimeFromMilliseconds(goalPoint + ImportantStuffs.MILLISECONDS_IN_HOUR);
        ImportantStuffs.showLog("Data saved from", startPoint, "--", endPoint);

        try {
            jsonInfo.put("checkpoint", goalPoint);
        } catch (JSONException e) {
            e.printStackTrace();
            ImportantStuffs.showLog("checkpoint not saving -_-");
        }

        ImportantStuffs.saveFileLocally("History.json", usageDetails.toString(), context);
        ImportantStuffs.saveFileLocally( "info.json", jsonInfo.toString(), context);
    }

    public  void checkTargetLocally(Context context){
        JSONObject targetMetaDetails = new JSONObject();
        String jsonString =  ImportantStuffs.getStringFromJsonObjectPath("targetMetaDetails.json",context);

        if(!jsonString.equals("")) {
            try {
                targetMetaDetails = new JSONObject(jsonString);
                Iterator<String> keys = targetMetaDetails.keys();
                long dailyTargetStartTime,dailyTargetEndTime,weeklyTargetStartTime,weeklyTargetEndTime;

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MILLISECOND,0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE,0);
                calendar.set(Calendar.HOUR,0);
                calendar.set(Calendar.AM_PM,Calendar.AM);
                dailyTargetStartTime = calendar.getTimeInMillis();

                calendar.set(Calendar.MILLISECOND,999);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MINUTE,59);
                calendar.set(Calendar.HOUR,11);
                calendar.set(Calendar.AM_PM,Calendar.PM);
                dailyTargetEndTime = calendar.getTimeInMillis();

                calendar.set(Calendar.MILLISECOND,0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE,0);
                calendar.set(Calendar.HOUR,0);
                calendar.set(Calendar.AM_PM,Calendar.AM);
                calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
                weeklyTargetStartTime = calendar.getTimeInMillis();

                calendar.set(Calendar.MILLISECOND,999);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MINUTE,59);
                calendar.set(Calendar.HOUR,11);
                calendar.set(Calendar.AM_PM,Calendar.PM);
                calendar.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
                weeklyTargetEndTime = calendar.getTimeInMillis();

                HashMap<String, AppUsageInfo> dailyData = getAppsUsageInfo(dailyTargetStartTime, System.currentTimeMillis(),context);
                HashMap<String, AppUsageInfo> weeklyData = getAppsUsageInfo(weeklyTargetStartTime, System.currentTimeMillis(),context);


                StringBuilder noti = new StringBuilder();

                while(keys.hasNext()) {
                    String key = keys.next();
                    JSONObject ob = (JSONObject) targetMetaDetails.get(key);


                    boolean checkDaily = false;
                    try {
                        checkDaily = ob.get("Daily").toString().equals("Active");
                    }catch (Exception e){}

                    if(checkDaily){
                        AppUsageInfo temp;
                        assert dailyData != null;
                        temp = dailyData.get(key);

                        JSONObject dateInfo = new JSONObject();
                        dateInfo.put("dailyTargetInMilis",  ob.getLong("dailyTargetInMilis"));
                        double percentage;
                        if (temp != null)
                            percentage = ((double) temp.timeInForeground / (double) ob.getLong("dailyTargetInMilis")) * 100.0;
                        else percentage = 0.00;
                        dateInfo.put("usageInPercentage", percentage);
                        dateInfo.put("Time_Range", dailyTargetStartTime + "--" + dailyTargetEndTime);
                        JSONObject date = new JSONObject();
                        try {
                            date = ob.getJSONObject("DailyInfo");
                        } catch (Exception e) {
                        }

                        date.put(dailyTargetStartTime+"-"+dailyTargetEndTime, dateInfo);
                        ob.put("DailyInfo", date);
                        String formattedPercentage = String.format("%.2f", percentage);
                        noti.append(formattedPercentage).append("% of daily target for ").append(key).append(" is used\n");
                    }else {
                        JSONObject date = new JSONObject();
                        try {
                            date = ob.getJSONObject("DailyInfo");
                        } catch (Exception e) {
                        }
                        date.put(dailyTargetStartTime + "-"+dailyTargetEndTime, null);
                        ob.put("DailyInfo", date);
                    }

                    boolean checkWeekly = false;
                    try {
                        checkWeekly = ob.get("Weekly").toString().equals("Active");
                    }catch (Exception e){}

                    if(checkWeekly){
                        AppUsageInfo temp;
                        assert weeklyData != null;
                        temp = weeklyData.get(key);

                        JSONObject dateInfo = new JSONObject();
                        dateInfo.put("weeklyTargetInMilis", ob.getLong("weeklyTargetInMilis"));
                        double percentage;
                        if (temp != null)
                            percentage = ((double) temp.timeInForeground / (double) ob.getLong("weeklyTargetInMilis")) * 100.0;
                        else percentage = 0.00;
                        dateInfo.put("usageInPercentage", percentage);
                        dateInfo.put("Time_Range", weeklyTargetStartTime + "--" + weeklyTargetEndTime);
                        JSONObject date = new JSONObject();
                        try {
                            date = ob.getJSONObject("WeeklyInfo");
                        } catch (Exception e) {
                        }
                        date.put(weeklyTargetStartTime + "-"+weeklyTargetEndTime, dateInfo);
                        ob.put("WeeklyInfo", date);
                        String formattedPercentage = String.format("%.2f", percentage);
                        noti.append(formattedPercentage).append("% of weekly target for ").append(key).append(" is used\n");

                    }else {
                        JSONObject date = new JSONObject();
                        try {
                            date = ob.getJSONObject("WeeklyInfo");
                        } catch (Exception e) {
                        }
                        date.put(weeklyTargetStartTime + "-"+weeklyTargetEndTime, null);
                        ob.put("WeeklyInfo", date);
                    }
                    targetMetaDetails.put(key, ob);
                }
                ImportantStuffs.saveFileLocally("targetMetaDetails.json", targetMetaDetails.toString(), context);


            } catch (JSONException e) {}
        }
    }
}
