package com.ahtrapotpid.appusagetracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.ahtrapotpid.appusagetracker.ImportantStuffs.MILLISECONDS_IN_DAY;
import static com.ahtrapotpid.appusagetracker.ImportantStuffs.MILLISECONDS_IN_HOUR;
import static com.ahtrapotpid.appusagetracker.ImportantStuffs.MILLISECONDS_IN_MINUTE;

public class AppsDataController extends BroadcastReceiver {
    Context context;
    public static final String TAG = "temp";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            ImportantStuffs.showLog("Device has been rebooted!");
        }

        startAlarm(context, 20 * ImportantStuffs.MILLISECONDS_IN_MINUTE);

        new DataController().execute();
    }

    public static void startAlarm(Context context, long delayInMillisecond) {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AppsDataController.class);

        Calendar calendar = Calendar.getInstance();

        long alarmTime = calendar.getTimeInMillis() + delayInMillisecond;

        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
    }


    public static HashMap<String, AppUsageInfo> getAppsUsageInfo(long startTime, long endTime, Context context) {
        HashMap<String, AppUsageInfo> appsUsageInfo = new HashMap<>();

        long currentTime = ImportantStuffs.getCurrentTime();
        if (endTime > currentTime)
            endTime = currentTime;
        if (startTime >= currentTime)
            return appsUsageInfo;

        UsageEvents.Event currentEvent;
        HashMap<String, List<UsageEvents.Event>> sameEvents = new HashMap<>();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents;

        assert mUsageStatsManager != null;
        usageEvents = mUsageStatsManager.queryEvents(startTime, endTime);

        final int RESUMED = 1;
        final int PAUSED = 2;


        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);

            if (currentEvent.getEventType() == RESUMED || currentEvent.getEventType() == PAUSED) {
                String packageName = currentEvent.getPackageName();
                String appName = ImportantStuffs.getAppName(packageName, context);

                if (appsUsageInfo.get(packageName) == null) {
                    appsUsageInfo.put(packageName, new AppUsageInfo(appName, packageName));
                    sameEvents.put(packageName, new ArrayList<>());
                }
                sameEvents.get(packageName).add(currentEvent);
            }
        }

        JSONObject lastUsedTimeJson = ImportantStuffs.getJsonObject("lastUsedTime.json", context);
        for (Map.Entry<String, List<UsageEvents.Event>> entry : sameEvents.entrySet()) {
            String packageName = entry.getKey();
            AppUsageInfo appUsageInfo = appsUsageInfo.get(packageName);
            int launchCount = 0, recentEventType = RESUMED;
            long recentResumedTime = startTime, usageTime = 0, lastUsedTime = 0;

            for (UsageEvents.Event event : entry.getValue()) {
                switch (event.getEventType()) {
                    case RESUMED:
                        recentResumedTime = event.getTimeStamp();
                        launchCount++;
                        recentEventType = RESUMED;
                        lastUsedTime = event.getTimeStamp();
                        break;
                    case PAUSED:
                        usageTime += (event.getTimeStamp() - recentResumedTime);
                        recentEventType = PAUSED;
                        lastUsedTime = event.getTimeStamp();
                        break;
                }
            }
            if (recentEventType == RESUMED)
                usageTime += endTime - recentResumedTime;

            if (usageTime > MILLISECONDS_IN_HOUR)
                usageTime = MILLISECONDS_IN_HOUR;

            appUsageInfo.setTimeInForeground(usageTime);
            appUsageInfo.setLaunchCount(launchCount);
            checkAndChangeLastUsedTime(lastUsedTimeJson, packageName, lastUsedTime, context);
        }
        ImportantStuffs.saveFileLocally("lastUsedTime.json", lastUsedTimeJson.toString(), context);

        return appsUsageInfo;
    }

/*    public static HashMap<String, AppUsageInfo> getAllAppsUsageInfo(long startTime, long endTime, Context context) {
        HashMap<String, AppUsageInfo> appsUsageInfo = getAppsUsageInfo(startTime, endTime, context);
        appsUsageInfo = addOtherAppsInfo(appsUsageInfo, context);
        return appsUsageInfo;
    }

    public static HashMap<String, AppUsageInfo> addOtherAppsInfo(HashMap<String, AppUsageInfo> appsInfo, Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(0);

        for (int i = 0; i < installedPackages.size(); i++) {
            PackageInfo packageInfo = installedPackages.get(i);
            String packageName = packageInfo.applicationInfo.packageName;
            if (appsInfo.get(packageName) != null)
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
    }*/

    public static HashMap<String, AppUsageInfo> getAppList(Context context) {
        saveUsageDataLocally(context);

        HashMap<String, AppUsageInfo> appsInfo = new HashMap<>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);

        for (int i = 0; i < installedPackages.size(); i++) {
            PackageInfo packageInfo = installedPackages.get(i);
            String packageName = packageInfo.applicationInfo.packageName;

            String appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            Drawable icon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
            long installed = 0;
            try {
                installed = packageManager.getPackageInfo(packageName, 0).firstInstallTime;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            boolean isSystem = ImportantStuffs.isSystemPackage(packageName, context);

            AppUsageInfo appUsageInfo = new AppUsageInfo(appName, packageName, icon, installed, isSystem);

            appsInfo.put(packageName, appUsageInfo);
        }
        return appsInfo;
    }

    public static HashMap<String, AppUsageInfo> getAppsUsageAndLastOpenedInfoFromJson(HashMap<String, AppUsageInfo> usageInfo, long startTime, long endTime, Context context) {
        Log.d("flag", "getting usage info from json");
        JSONObject historyJson = ImportantStuffs.getJsonObject("History.json", context);
        long checkPoint = getCheckpoint(context);
        for (long time = startTime; time < endTime && time <= checkPoint; time += MILLISECONDS_IN_HOUR) {
            try {
                JSONArray appArray = historyJson.getJSONArray(String.valueOf(time));
                for (int i = 0; i < appArray.length(); i++) {
                    try {
                        JSONObject appHourlyInfo = appArray.getJSONObject(i);
                        String packageName = appHourlyInfo.getString("packageName");
                        if (!usageInfo.containsKey(packageName))
                            continue;
                        AppUsageInfo appUsageInfo = usageInfo.get(packageName);

                        long foregroundTime = appHourlyInfo.getLong("foregroundTime");
                        appUsageInfo.addToTimeInForeground(foregroundTime);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("flag", "usage info from json got");


        Log.d("flag", "getting live data");
        HashMap<String, AppUsageInfo> liveUsageInfo = getAppsUsageInfo(checkPoint + MILLISECONDS_IN_HOUR, endTime, context);
        for (Map.Entry<String, AppUsageInfo> entry : liveUsageInfo.entrySet()) {
            String packageName = entry.getKey();
            if (!usageInfo.containsKey(packageName))
                continue;
            usageInfo.get(packageName).addToTimeInForeground(entry.getValue().timeInForeground);
        }
        Log.d("flag", "live data got");

        Log.d("flag", "getting last used time");
        JSONObject lastUsedTimeJson = ImportantStuffs.getJsonObject("lastUsedTime.json", context);
        for (Map.Entry<String, AppUsageInfo> entry : usageInfo.entrySet()) {
            long lastUsedTime = 0;
            try {
                lastUsedTime = lastUsedTimeJson.getLong(entry.getKey());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            entry.getValue().setLastTimeUsed(lastUsedTime);
        }
        Log.d("flag", "last usage time got");
        return usageInfo;
    }

    public static HashMap<String, AppUsageInfo> getOnlyAppsUsageInfoFromJson(JSONObject historyJson, long startTime, long endTime, Context context) {

        long checkPoint = getCheckpoint(context);
        long currentTime = ImportantStuffs.getCurrentTime();
        HashMap<String, AppUsageInfo> usageInfo = new HashMap<>();

        if (endTime > currentTime)
            endTime = currentTime;
        if (startTime >= currentTime)
            return usageInfo;

        Log.d("flag", "getting usage info from json");
//        JSONObject historyJson = ImportantStuffs.getJsonObject("History.json", context);
        for (long time = startTime; time < endTime && time <= checkPoint; time += MILLISECONDS_IN_HOUR) {
            try {
                JSONArray appArray = historyJson.getJSONArray(String.valueOf(time));
                for (int i = 0; i < appArray.length(); i++) {
                    try {
                        JSONObject appHourlyInfo = appArray.getJSONObject(i);
                        String packageName = appHourlyInfo.getString("packageName");
                        if (!usageInfo.containsKey(packageName))
                            usageInfo.put(packageName, new AppUsageInfo(packageName));

                        AppUsageInfo appUsageInfo = usageInfo.get(packageName);

                        long foregroundTime = appHourlyInfo.getLong("foregroundTime");
                        appUsageInfo.addToTimeInForeground(foregroundTime);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("flag", "usage info from json got");

        if (checkPoint + MILLISECONDS_IN_HOUR >= endTime)
            return usageInfo;

        Log.d("flag", "getting live data");
        String a = ImportantStuffs.getDateAndTimeFromMilliseconds(startTime), b = ImportantStuffs.getDateAndTimeFromMilliseconds(endTime);
        String c = ImportantStuffs.getDateAndTimeFromMilliseconds(checkPoint);
        Log.d(TAG, a + "-----" + b + "-----" + c);
        HashMap<String, AppUsageInfo> liveUsageInfo = getAppsUsageInfo(checkPoint + MILLISECONDS_IN_HOUR, endTime, context);
        for (Map.Entry<String, AppUsageInfo> entry : liveUsageInfo.entrySet()) {
            String packageName = entry.getKey();
            if (!usageInfo.containsKey(packageName))
                usageInfo.put(packageName, new AppUsageInfo(packageName));
            usageInfo.get(packageName).addToTimeInForeground(entry.getValue().timeInForeground);
        }
        Log.d("flag", "live data got");
        return usageInfo;
    }


    public static ArrayList<Long> getWeeklyUsageDataInDailyList(long weekStartTime, String packageName, Context context) {
        JSONObject historyJson = ImportantStuffs.getJsonObject("History.json", context);
        ArrayList<Long> usage = new ArrayList<>();

        long startTime = weekStartTime;
        long endTime = startTime + 6 * ImportantStuffs.MILLISECONDS_IN_DAY;

        for (long currentTime = startTime; currentTime <= endTime; currentTime += ImportantStuffs.MILLISECONDS_IN_DAY) {
            long time = getDailyUsageData(historyJson, currentTime, packageName, context);
            usage.add(time);
        }
        return usage;
    }

    public static ArrayList<Long> getDailyUsageDataInHourlyList(long dayStartTime, String packageName, Context context) {
        JSONObject historyJson = ImportantStuffs.getJsonObject("History.json", context);
        ArrayList<Long> usage = new ArrayList<>();

        long startTime = dayStartTime;
        long endTime = dayStartTime + 23 * MILLISECONDS_IN_HOUR;
        String a = ImportantStuffs.getDateAndTimeFromMilliseconds(dayStartTime);

        for (long currentTime = startTime; currentTime <= endTime; currentTime += MILLISECONDS_IN_HOUR) {
            long time = getHourlyUsageData(historyJson, currentTime, packageName, context);
            usage.add(time);
        }
        return usage;
    }

    public static long getWeeklyUsageData(long weekStartTime, String packageName, Context context) {
        long endTime = weekStartTime + 7 * ImportantStuffs.MILLISECONDS_IN_DAY;

        HashMap<String, AppUsageInfo> usageInfoHashMap = new HashMap<>();
        usageInfoHashMap = getAppsUsageAndLastOpenedInfoFromJson(usageInfoHashMap, weekStartTime, endTime, context);
        if (!usageInfoHashMap.containsKey(packageName))
            return 0;
        return usageInfoHashMap.get(packageName).getTimeInForeground();
    }

    public static long getDailyUsageData(JSONObject historyJson, long dayStartTime, String packageName, Context context) {
        long endTime = dayStartTime + MILLISECONDS_IN_DAY;
        HashMap<String, AppUsageInfo> usageInfoHashMap;
        usageInfoHashMap = getOnlyAppsUsageInfoFromJson(historyJson, dayStartTime, endTime, context);
//        Log.d(TAG, "getDailyUsageData: "+usageInfoHashMap.toString());
        if (!usageInfoHashMap.containsKey(packageName))
            return 0;
        return usageInfoHashMap.get(packageName).getTimeInForeground();
    }

    public static long getHourlyUsageData(JSONObject historyJson, long hourStartTime, String packageName, Context context) {
        long usage, endTime = hourStartTime + MILLISECONDS_IN_HOUR;
        HashMap<String, AppUsageInfo> usageInfoHashMap;
        usageInfoHashMap = getOnlyAppsUsageInfoFromJson(historyJson, hourStartTime, endTime, context);
        if (usageInfoHashMap.containsKey(packageName))
            usage = usageInfoHashMap.get(packageName).getTimeInForeground();
        else
            usage = 0;
        return usage;
    }

    private static long getCheckpoint(Context context) {
        JSONObject jsonInfo = ImportantStuffs.getJsonObject("info.json", context);
        long checkpoint = 0;
        try {
            checkpoint = jsonInfo.getLong("checkpoint");
        } catch (Exception e) {
            Log.e(TAG, "Can't get checkpoint");
            ImportantStuffs.showErrorLog("Can't get checkpoint");
        }
        return checkpoint;
    }


    public static void checkVersionUpdate(Context context) {
        //FirebaseDatabase.getInstance().getReference().child("UpdateApp").child("CheckerNode").child("version").setValue("1.3");
        String installedVersion = ImportantStuffs.getAppVersion(context);

        Query q = FirebaseDatabase.getInstance().getReference("UpdateApp")
                .orderByChild("version").equalTo(installedVersion);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    ImportantStuffs.displayUpdateNotification(context);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void checkCurrentWeek(Context context, long timeDifference) {
        Log.d("flag", "checking current week");
        long currentHour = ImportantStuffs.getCurrentHour();
        JSONObject infoJson = ImportantStuffs.getJsonObject("info.json", context);
        SharedPreferences sharedPreference = context.getSharedPreferences(MainActivity.SHARED_PREFERENCE, MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreference.edit();
        int weekNumber = 0;
        long weekTime = currentHour;
        try {
            weekNumber = infoJson.getInt("weekNumber");
            weekTime = infoJson.getLong("weekTime");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (weekNumber >= 4)
            return;
//        Log.d(TAG, "current week = " + weekNumber + " time = " + ImportantStuffs.getDateAndTimeFromMilliseconds(weekTime));
        if (weekNumber == 0) {
            long currentTime = ImportantStuffs.getCurrentTime();
            long currentWeekTime = ImportantStuffs.getWeekStartTimeFromTime(currentTime);
            if (currentTime - currentWeekTime < MILLISECONDS_IN_DAY) {
                editor.putLong("WeekOneStartTime", currentWeekTime);
                editor.putLong("weekTime", currentWeekTime);
                editor.putInt("weekNumber", 1);
                try {
                    infoJson.put("weekNumber", 1);
                    infoJson.put("weekTime", currentWeekTime);
                    ImportantStuffs.saveFileLocally("info.json", infoJson.toString(), context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        if (currentHour - weekTime >= timeDifference) {
            try {
                weekNumber++;
                ImportantStuffs.showLog("week changed to " + weekNumber);
                infoJson.put("weekNumber", weekNumber);
                infoJson.put("weekTime", currentHour);
                if (weekNumber == 2) {
                    sharedPreference.edit().putLong("weekTwoStartTime", currentHour).apply();
                    setAutoTargetForAllApps(weekTime, currentHour, infoJson, context);
                    ImportantStuffs.displayNotification(context, context.getResources().getString(R.string.week_2_notice));
                }
                if (weekNumber == 3) {
                    sharedPreference.edit().putLong("weekThreeStartTime", currentHour).apply();
                    resetAutoTargetForAllApps(infoJson);
                    ImportantStuffs.displayNotification(context, context.getResources().getString(R.string.week_3_notice));
                } else if (weekNumber == 4){
                    sharedPreference.edit().putLong("weekFourTwoStartTime", currentHour).apply();
                    ImportantStuffs.displayNotification(context, context.getResources().getString(R.string.week_4_notice));
                }


                ImportantStuffs.saveFileLocally("info.json", infoJson.toString(), context);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        Log.d("flag", "current week checked");
    }

    private static void setAutoTargetForAllApps(long startTime, long endTime, JSONObject infoJson, Context context) {
        final long DAILY_TARGET_LOW = 60 * MILLISECONDS_IN_MINUTE, DAILY_TARGET_HIGH = 4 * MILLISECONDS_IN_HOUR;
//        final long WEEKLY_TARGET_LOW = 7*DAILY_TARGET_LOW, WEEKLY_TARGET_HIGH = 7*DAILY_TARGET_HIGH;

        HashMap<String, AppUsageInfo> usageInfoHashMap = new HashMap<>();
        List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < installedPackages.size(); i++) {
            PackageInfo packageInfo = installedPackages.get(i);
            String packageName = packageInfo.applicationInfo.packageName;
            usageInfoHashMap.put(packageName, new AppUsageInfo(packageName));
        }
        usageInfoHashMap = getAppsUsageAndLastOpenedInfoFromJson(usageInfoHashMap, startTime, endTime, context);

        JSONObject appsInfo = new JSONObject();
        for (Map.Entry<String, AppUsageInfo> entry : usageInfoHashMap.entrySet()) {
            String packageName = entry.getKey();
            AppUsageInfo usageInfo = entry.getValue();
            long usageTime = usageInfo.getTimeInForeground();

            double numOfDays = (double) (endTime - startTime) / (double) MILLISECONDS_IN_DAY;
            double avgDailyUsage = usageTime / numOfDays;
            long dailyTarget = (long) (avgDailyUsage * .7);
            if (dailyTarget < DAILY_TARGET_LOW)
                dailyTarget = MILLISECONDS_IN_HOUR;
            else if (dailyTarget > DAILY_TARGET_HIGH)
                dailyTarget = DAILY_TARGET_HIGH;

            long weeklyTarget = dailyTarget * 7;

//            Log.d(TAG, "num of days: " + numOfDays);
            try {
                JSONObject thisAppInfoJson = new JSONObject();
                thisAppInfoJson.put("targetTypes", new JSONArray("[0, 1]"));
                thisAppInfoJson.put("weeklyTarget", weeklyTarget);
                thisAppInfoJson.put("weeklyNotifications", new JSONArray("[0, 1, 2, 3, 4, 5, 6]"));
                thisAppInfoJson.put("dailyTarget", dailyTarget);
                thisAppInfoJson.put("dailyNotifications", new JSONArray("[0, 1, 2, 3, 4, 5, 6]"));
                appsInfo.put(ImportantStuffs.removeDot(packageName), thisAppInfoJson);
            } catch (JSONException e) {
                e.printStackTrace();
                ImportantStuffs.showErrorLog("Can't put app to appInfo");
            }
        }
        try {
            infoJson.put("appsInfo", appsInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void resetAutoTargetForAllApps(JSONObject infoJson) {
        try {
            infoJson.put("appsInfo", new JSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void testSetAutoTargetForAllApps(long startTime, long endTime, JSONObject infoJson, Context context) {
        setAutoTargetForAllApps(startTime, endTime, infoJson, context);
        ImportantStuffs.saveFileLocally("info.json", infoJson.toString(), context);
    }

    public static void testResetAutoTargetForAllApps(JSONObject infoJson, Context context) {
        resetAutoTargetForAllApps(infoJson);
        ImportantStuffs.saveFileLocally("info.json", infoJson.toString(), context);
    }


    private static void checkAndChangeLastUsedTime(JSONObject lastUsedTimeJson, String packageName, long time, Context context) {
        long lastUsedTime = 0;
        try {
            lastUsedTime = lastUsedTimeJson.getLong(packageName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (time > lastUsedTime) {
            try {
                lastUsedTimeJson.put(packageName, time);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveInstallationInfo() {
        Log.d("flag", "Saving installation info");
        JSONObject infoJson = ImportantStuffs.getJsonObject("info.json", context);
        JSONObject appsInstallationInfoJson;
        try {
            appsInstallationInfoJson = infoJson.getJSONObject("appsInstallationInfo");
        } catch (JSONException e) {
            e.printStackTrace();
            ImportantStuffs.showErrorLog("Can't find appsInstallationInfo");
            return;
        }

        HashMap<String, AppUsageInfo> allApps = new HashMap<>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
        for (int i = 0; i < installedPackages.size(); i++) {
            PackageInfo packageInfo = installedPackages.get(i);
            String packageName = packageInfo.applicationInfo.packageName;
            String appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            long installed = 0;
            try {
                installed = packageManager.getPackageInfo(packageName, 0).firstInstallTime;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            AppUsageInfo appUsageInfo = new AppUsageInfo(appName, packageName, installed);
            allApps.put(packageName, appUsageInfo);
        }

        for (HashMap.Entry entry : allApps.entrySet()) {
            String key = ImportantStuffs.removeDot((String) entry.getKey());
            AppUsageInfo appInfo = (AppUsageInfo) entry.getValue();
            String value = "";
            try {
                value = appsInstallationInfoJson.getJSONObject(key).toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!value.equals(""))
                continue;
            JSONObject jsonValue = new JSONObject();
            try {
                jsonValue.put("installationTime", appInfo.getInstallationTime());
                jsonValue.put("appName", appInfo.getAppName());
                appsInstallationInfoJson.put(key, jsonValue);
            } catch (JSONException e) {
                e.printStackTrace();
                ImportantStuffs.showErrorLog("Can't save installation info for ", appInfo.getAppName());
            }

        }
        try {
            infoJson.put("appsInstallationInfo", appsInstallationInfoJson);
            ImportantStuffs.saveFileLocally("info.json", infoJson.toString(), context);
        } catch (JSONException e) {
            e.printStackTrace();
            ImportantStuffs.showErrorLog("Can't save installation info");
            return;
        }
        Log.d("flag", "Installation info saved.");
    }

    private static void saveUsageDataLocally(Context context) {
        ImportantStuffs.showLog("Checking local data------");
        JSONObject jsonInfo = ImportantStuffs.getJsonObject("info.json", context);
        long checkpoint = getCheckpoint(context);
        long goalPoint = ImportantStuffs.getCurrentHour() - MILLISECONDS_IN_HOUR;

        if (goalPoint == checkpoint) {
            ImportantStuffs.showLog("No new data to store");
            return;
        }
        if (checkpoint == 0) {
            ImportantStuffs.showErrorLog("No valid checkpoint data found");
            return;
        }

        Log.d("flag", "Saving usage info");

        JSONObject usageDetails = ImportantStuffs.getJsonObject("History.json", context);

        for (long startTime = checkpoint + MILLISECONDS_IN_HOUR; startTime <= goalPoint; startTime += MILLISECONDS_IN_HOUR) {
            HashMap<String, AppUsageInfo> appsUsageInfo = getAppsUsageInfo(startTime, startTime + MILLISECONDS_IN_HOUR, context);
            JSONArray jsonAppInfo = new JSONArray();
            for (AppUsageInfo appInfo : appsUsageInfo.values()) {
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

        String startPoint = ImportantStuffs.getDateAndTimeFromMilliseconds(checkpoint + MILLISECONDS_IN_HOUR);
        String endPoint = ImportantStuffs.getDateAndTimeFromMilliseconds(goalPoint + MILLISECONDS_IN_HOUR);

        try {
            jsonInfo.put("checkpoint", goalPoint);
        } catch (JSONException e) {
            e.printStackTrace();
            ImportantStuffs.showLog("checkpoint not saving -_-");
        }

        long oldestTime = ImportantStuffs.getDayStartingHour() - 30 * MILLISECONDS_IN_DAY;
        usageDetails = removeHistoryOlderThan(oldestTime, usageDetails);

        ImportantStuffs.saveFileLocally("History.json", usageDetails.toString(), context);
        ImportantStuffs.saveFileLocally("info.json", jsonInfo.toString(), context);
        ImportantStuffs.showLog("Data saved from", startPoint, "--", endPoint);
        Log.d("flag", "usage info saved");
    }

    public void checkAndSaveTargetLocally(Context context) {
        Log.d("flag", "saving target info");
        JSONObject info = ImportantStuffs.getJsonObject("info.json",context);
        JSONObject notificationInfo = ImportantStuffs.getJsonObject("notificationInfo.json",context);


        try {
            JSONObject appsInfo = info.getJSONObject("appsInfo");

            Iterator<String> keys = appsInfo.keys();

            long dailyTargetStartTime, weeklyTargetStartTime;

            dailyTargetStartTime = ImportantStuffs.getDayStartingHour();

            weeklyTargetStartTime = ImportantStuffs.getWeekStartTimeFromTime(System.currentTimeMillis());

            ArrayList<String> applist = new ArrayList<>();
            HashMap<String, AppUsageInfo> usageInfoDaily = new HashMap<>();
            HashMap<String, AppUsageInfo> usageInfoWeekly;
            while (keys.hasNext()) {
                String key = keys.next();
                applist.add(key);
                usageInfoDaily.put(ImportantStuffs.addDot(key), new AppUsageInfo(ImportantStuffs.addDot(key)));
            }
            usageInfoWeekly = ImportantStuffs.copyUsageMap(usageInfoDaily);

            usageInfoDaily = getAppsUsageAndLastOpenedInfoFromJson(usageInfoDaily, dailyTargetStartTime, System.currentTimeMillis(), context);
            usageInfoWeekly = getAppsUsageAndLastOpenedInfoFromJson(usageInfoWeekly, weeklyTargetStartTime, System.currentTimeMillis(), context);

            for (int j = 0; j < applist.size(); j++) {
                String packageName = applist.get(j);
                JSONObject individualApp = (JSONObject) appsInfo.get(packageName);

                JSONArray targetTypes = individualApp.getJSONArray("targetTypes");
                int typeCount = targetTypes.length();
                int daily = 1, weekly = 0;

                for (int i = 0; i < typeCount; i++) {
                    int dailyOrWeekly = targetTypes.getInt(i);
                    long usedTime = 0;
                    if (dailyOrWeekly == daily) {
                        try{
                            usedTime = Objects.requireNonNull(usageInfoDaily.get(ImportantStuffs.addDot(packageName))).getTimeInForeground();
                            JSONArray dailyNotifications = individualApp.getJSONArray("dailyNotifications");
                            individualApp = getSingleHistory("dailyTarget", "DailyInfo",
                                    usedTime, packageName, individualApp, dailyTargetStartTime, dailyNotifications,notificationInfo);
                        }catch (Exception e){Log.d("try1","reached");}
                    } else {
                        try{
                            usedTime = Objects.requireNonNull(usageInfoWeekly.get(ImportantStuffs.addDot(packageName))).getTimeInForeground();
                            JSONArray weeklyNotifications = individualApp.getJSONArray("weeklyNotifications");
                            individualApp = getSingleHistory("weeklyTarget", "WeeklyInfo",
                                    usedTime, packageName, individualApp, weeklyTargetStartTime, weeklyNotifications,notificationInfo);
                        }catch (Exception e){Log.d("try2","reached");}

                    }
                }
                if (typeCount == 0) {
                    individualApp = setRecentHistoryToNull("DailyInfo", individualApp, dailyTargetStartTime, ImportantStuffs.addDot(packageName)
                            ,notificationInfo);
                    individualApp = setRecentHistoryToNull("WeeklyInfo", individualApp, weeklyTargetStartTime, ImportantStuffs.addDot(packageName)
                    ,notificationInfo);
                } else if (typeCount == 1) {
                    if (targetTypes.getInt(0) == weekly) {
                        individualApp = setRecentHistoryToNull("DailyInfo", individualApp, dailyTargetStartTime, ImportantStuffs.addDot(packageName)
                        ,notificationInfo);
                    } else {
                        individualApp = setRecentHistoryToNull("WeeklyInfo", individualApp, weeklyTargetStartTime, ImportantStuffs.addDot(packageName)
                        ,notificationInfo);
                    }
                }

                appsInfo.put(packageName, individualApp);
            }
            info.put("appsInfo", appsInfo);
            ImportantStuffs.saveFileLocally("info.json", info.toString(), context);
            ImportantStuffs.saveFileLocally("notificationInfo.json", notificationInfo.toString(), context);
            Log.d("flag", "target info saved");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject removeHistoryOlderThan(long time, JSONObject history) {
        Iterator<String> keys = history.keys();
        ArrayList<String> keysToRemove = new ArrayList<>();

        while (keys.hasNext()) {
            String key = keys.next();
            long value = Long.parseLong(key);
            if (value < time)
                keysToRemove.add(key);
            else
                break;
        }
        for (String key : keysToRemove)
            history.remove(key);

        return history;
    }

    public JSONObject getSingleHistory(String targetType, String infoName, Long usedTime, String packageName, JSONObject individualApp,
                                       Long startTime, JSONArray notifications, JSONObject notificationInfo) throws JSONException {

        JSONObject dateInfo = new JSONObject();
        dateInfo.put("target", individualApp.getLong(targetType));
        double percentage = 0.0;
        if (usedTime != 0) {
            percentage = ((double) usedTime / (double) individualApp.getLong(targetType)) * 100.0;
            dateInfo.put("used", usedTime);

        } else dateInfo.put("used", 0);

        dateInfo.put("date", startTime);
        JSONObject date = new JSONObject();
        try {
            date = individualApp.getJSONObject(infoName);
        } catch (Exception e) {
        }

        date.put(startTime + "", dateInfo);
        individualApp.put(infoName, date);

        //
        checkNotification(infoName, (int) percentage, usedTime , notifications, packageName, notificationInfo);
        //

        return individualApp;
    }

    public void checkNotification(String infoName, int percentage, Long UsedTime, JSONArray notifications, String packageName, JSONObject notificationInfo) {

        boolean flag = false, repeatAlarm = false;
        int tempPercentage = 0;

        JSONArray notificationTypes = new JSONArray();
        try {
            notificationTypes.put(0, 100);
            notificationTypes.put(1, 90);
            notificationTypes.put(2, 80);
            notificationTypes.put(3, 70);
            notificationTypes.put(4, 60);
            notificationTypes.put(5, 50);
        } catch (Exception e) {
        }


        for (int i = 0; i < notifications.length(); i++) {
            //Log.d("trycatch",notifications.length()+"");
            try {
                if (notifications.getInt(i) == 6) {
                    repeatAlarm = true;
                } else if (percentage >= notificationTypes.getInt(notifications.getInt(i)) && notificationTypes.getInt(notifications.getInt(i)) >= tempPercentage) {
                    flag = true;
                    tempPercentage = notificationTypes.getInt(notifications.getInt(i));
                }
            } catch (Exception e) {
            }
        }
        if (repeatAlarm && tempPercentage == 100) {
            tempPercentage = getPercentageRoundedToMultipleOfThirty(percentage);
        }

        if (flag) {

            JSONObject data, today, appData;
            data = new JSONObject();
            today = new JSONObject();
            appData = new JSONObject();
            try {
                appData = notificationInfo.getJSONObject(ImportantStuffs.removeDot(packageName));
                data = appData.getJSONObject(infoName);

                today = data.getJSONObject(ImportantStuffs.getDayStartingHour() + "");
                int p = today.getInt("percentage");
                if (p != tempPercentage) throw new Exception();
            } catch (Exception e) {
                try {
                    today.put("percentage", tempPercentage);
                    today.put("Time", System.currentTimeMillis());
                    data.put(ImportantStuffs.getDayStartingHour() + "", today);
                    appData.put(infoName, data);
                    notificationInfo.put(ImportantStuffs.removeDot(packageName), appData);
                    //ImportantStuffs.saveFileLocally("notificationInfo.json", notificationInfo.toString(), context);
                    int mode = (infoName.equals("DailyInfo")) ? 1 : 0;
                    tempPercentage = Math.min(tempPercentage, 100);
                    try {
                        ImportantStuffs.showLog("Notification incoming...");
                        ImportantStuffs.displayUsageNotification(ImportantStuffs.addDot(packageName), tempPercentage, UsedTime, mode, context);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        ImportantStuffs.showErrorLog("Can't show notification");
                    }

                } catch (Exception e1) {

                }
            }
        }
    }

    public int getPercentageRoundedToMultipleOfThirty(int percentage) {

        int temp = percentage - 100;
        int dividend = temp / 30;

        return (dividend * 30) + 100;
    }

    public JSONObject setRecentHistoryToNull(String infoName, JSONObject individualApp, Long startTime, String packageName, JSONObject notificationInfo) throws JSONException {

        //set notificationInfo of current day to null
        JSONObject data, appData;
        data = new JSONObject();
        appData = new JSONObject();
        try {
            appData = notificationInfo.getJSONObject(ImportantStuffs.removeDot(packageName));
            data = appData.getJSONObject(infoName);
            data.put(ImportantStuffs.getDayStartingHour() + "", null);
        } catch (Exception e) {
            data.put(ImportantStuffs.getDayStartingHour() + "", null);
            appData.put(infoName, data);
            notificationInfo.put(ImportantStuffs.removeDot(packageName), appData);
        }
        //

        JSONObject date = new JSONObject();
        try {
            date = individualApp.getJSONObject(infoName);
        } catch (Exception e) {
        }
        date.put(startTime + "", null);
        individualApp.put(infoName, date);

        return individualApp;
    }

    public static ArrayList<TargetInfo> getTargetHistory(String packageName, int mode, Context context) {

        String type;
        if (mode == 0) type = "WeeklyInfo";
        else type = "DailyInfo";

        ArrayList<TargetInfo> targetInfos = new ArrayList<>();

        JSONObject info = new JSONObject();
        String jsonString = ImportantStuffs.getStringFromJsonObjectPath("info.json", context);
        try {
            info = new JSONObject(jsonString);
        } catch (Exception e) {
        }

        JSONObject appsInfo, targetHistory, individualApp;
        try {
            appsInfo = info.getJSONObject("appsInfo");
            individualApp = appsInfo.getJSONObject(ImportantStuffs.removeDot(packageName));
            targetHistory = individualApp.getJSONObject(type);
            Iterator<String> keys = targetHistory.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject ob = (JSONObject) targetHistory.get(key);
                TargetInfo ti = new TargetInfo(ob.getLong("date"), ob.getLong("target"), ob.getLong("used"), mode);
                targetInfos.add(ti);
            }
        } catch (Exception e) {
        }
        return targetInfos;
    }


    private class DataController extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("a_flag", "DataController: started");
            saveInstallationInfo();
//            Log.d(TAG, "saveInstallationInfo done");
            saveUsageDataLocally(context);
//            Log.d(TAG, "saveUsageDataLocally done");
            checkAndSaveTargetLocally(context);
//            Log.d(TAG, "checkAndSaveTargetLocally done");
            ImportantStuffs.saveEverything(context);
//            Log.d(TAG, "ImportantStuffs.saveEverything done");
            checkVersionUpdate(context);
//            Log.d(TAG, "checkVersionUpdate done");
            checkCurrentWeek(context, MILLISECONDS_IN_DAY);
//            Log.d(TAG, "checkCurrentWeek done");
            Log.d("a_flag", "DataController: ended");
            return null;
        }

    }

}
