package com.ahtrapotpid.appusagetracker;

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
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.ahtrapotpid.appusagetracker.ImportantStuffs.MILLISECONDS_IN_DAY;
import static com.ahtrapotpid.appusagetracker.ImportantStuffs.MILLISECONDS_IN_HOUR;

public class AppsDataController extends BroadcastReceiver {
    Context context;
    public static final String TAG = "extra";

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
        UsageEvents usageEvents ;

        assert mUsageStatsManager != null;
        usageEvents = mUsageStatsManager.queryEvents(startTime, endTime);

//        int RESUMED = UsageEvents.Event.MOVE_TO_FOREGROUND;
//        int PAUSED = UsageEvents.Event.MOVE_TO_BACKGROUND;
//        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//        {
//            RESUMED = UsageEvents.Event.ACTIVITY_RESUMED;
//            PAUSED = UsageEvents.Event.ACTIVITY_PAUSED;
//        }
        final int RESUMED = 1;
        final  int PAUSED = 2;


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

            if(usageTime > MILLISECONDS_IN_HOUR)
                usageTime = MILLISECONDS_IN_HOUR;

            appUsageInfo.setTimeInForeground(usageTime);
            appUsageInfo.setLaunchCount(launchCount);
            appUsageInfo.setLastTimeUsed(lastUsedTime);
        }

        return appsUsageInfo;
    }

    public static HashMap<String, AppUsageInfo> getAllAppsUsageInfo(long startTime, long endTime, Context context) {
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
    }

    public static HashMap<String, AppUsageInfo> getAppsUsageInfoFromJson(long startTime, long endTime, Context context){
        HashMap<String, AppUsageInfo> usageInfo = new HashMap<>();
        JSONObject historyJson = ImportantStuffs.getJsonObject("history.json", context);
        long checkPoint = getCheckpoint(context);
        for(long time = startTime; time <= endTime && time <= checkPoint; time += MILLISECONDS_IN_HOUR){
            try {
                JSONArray appArray = historyJson.getJSONArray(String.valueOf(time));
                for (int i = 0; i < appArray.length(); i++) {
                    try {
                        JSONObject appHourlyInfo = appArray.getJSONObject(i);
                        String packageName = appHourlyInfo.getString("packageName");
                        AppUsageInfo appUsageInfo = usageInfo.get(packageName);

                        if(appUsageInfo == null){
                            String appName = appHourlyInfo.getString("name");
                            Drawable icon = ImportantStuffs.getAppIcon(packageName, context);
                            long installationDate = ImportantStuffs.getAppInstallationDate(packageName, context);
                            boolean isSystem = ImportantStuffs.isSystemPackage(packageName, context);
                            appUsageInfo = new AppUsageInfo(appName, packageName, icon, installationDate, isSystem);
                            usageInfo.put(packageName, appUsageInfo);
                        }
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
        return usageInfo;
    }


    public static ArrayList<Long> getWeeklyUsageDataInDailyList(JSONObject currentHourChecker ,JSONObject jsonObject, long weekStartTime, String packageName, Context context) {
        ArrayList<Long> usage = new ArrayList<>();

        long startTime = weekStartTime;
        long endTime = startTime + 6 * ImportantStuffs.MILLISECONDS_IN_DAY;

        for (long currentTime = startTime; currentTime <= endTime; currentTime += ImportantStuffs.MILLISECONDS_IN_DAY) {
            long time = getDailyUsageData(currentHourChecker,jsonObject, currentTime, packageName, context);
            usage.add(time);
        }
        return usage;
    }

    public static ArrayList<Long> getDailyUsageDataInHourlyList(JSONObject currentHourChecker ,JSONObject jsonObject, long dayStartTime, String packageName, Context context) {
        ArrayList<Long> usage = new ArrayList<>();

        long startTime = dayStartTime;
        long endTime = dayStartTime + 23 * MILLISECONDS_IN_HOUR;

        for (long currentTime = startTime; currentTime <= endTime; currentTime += MILLISECONDS_IN_HOUR) {
            long time = getHourlyUsageData(currentHourChecker,jsonObject, currentTime, packageName, context);
            usage.add(time);
        }
        return usage;
    }

    public static long getWeeklyUsageData(JSONObject currentHourChecker ,JSONObject jsonObject, long weekStartTime, String packageName, Context context) {
        long usage = 0;

        long startTime = weekStartTime;
        long endTime = startTime + 6 * ImportantStuffs.MILLISECONDS_IN_DAY;

        for (long currentTime = startTime; currentTime <= endTime; currentTime += ImportantStuffs.MILLISECONDS_IN_DAY) {
            long time = getDailyUsageData(currentHourChecker,jsonObject, currentTime, packageName, context);
            usage += time;
        }
        return usage;
    }

    public static long getDailyUsageData(JSONObject currentHourChecker ,JSONObject jsonObject, long dayStartTime, String packageName, Context context) {
        long usage = 0;

        long startTime = dayStartTime;
        long endTime = dayStartTime + 23 * MILLISECONDS_IN_HOUR;

        for (long currentTime = startTime; currentTime <= endTime; currentTime += MILLISECONDS_IN_HOUR) {
            long time = getHourlyUsageData(currentHourChecker,jsonObject, currentTime, packageName, context);
            usage += time;
        }
        return usage;
    }

    public static long getHourlyUsageData(JSONObject currentHourChecker ,JSONObject jsonObject, long hourStartTime, String packageName, Context context) {
        long checkpoint = getCheckpoint(context);
        long currentHour = ImportantStuffs.getCurrentHour();
        long currentTime = ImportantStuffs.getCurrentTime();

        if (checkpoint >= hourStartTime) {

            String key = String.valueOf(hourStartTime);
            try {
                JSONArray appArray = jsonObject.getJSONArray(key);
                return getForegroundTime(appArray, packageName);
            } catch (JSONException e) {
                return 0;
            }
        } else if (hourStartTime == currentHour) {

            long time = 0 , savedTime = 0;
            HashMap<String, AppUsageInfo> map = getAppsUsageInfo(hourStartTime, currentTime, context);
            try {
                time = map.get(packageName).getTimeInForeground();
            } catch (Exception e) {
            }

            try {
                JSONObject ob = currentHourChecker.getJSONObject(packageName);
                savedTime = ob.getLong(hourStartTime+"");

                if(time>savedTime){
                    ob.put(hourStartTime+"", time);
                    currentHourChecker.put(packageName,ob);

                }
                else {
                    time = savedTime;
                }

            }catch (Exception e){
                try{
                    JSONObject ob = new JSONObject();
                    ob.put(hourStartTime+"",time);
                    currentHourChecker.put(packageName,ob);
                }catch (Exception e1){}
            }

            ImportantStuffs.saveFileLocally("notificationErrorChecker.json",currentHourChecker.toString(),context);
            return time;
        }

        return 0;
    }


    private static int getForegroundTime(JSONArray array, String packageName) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject object = array.getJSONObject(i);
                String value = object.getString("packageName");
                if (value.equals(packageName))
                    return object.getInt("foregroundTime");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return 0;
    }

    private static long getCheckpoint(Context context) {
        JSONObject jsonInfo = ImportantStuffs.getJsonObject("info.json", context);
        long checkpoint = 0;
        try {
            checkpoint = jsonInfo.getLong("checkpoint");
        } catch (Exception e) {
        }
        return checkpoint;
    }

    private void saveUsageDataLocally() {
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

        JSONObject usageDetails = ImportantStuffs.getJsonObject("History.json",context);

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
    }

    private JSONObject removeHistoryOlderThan(long time, JSONObject history){
        Iterator<String> keys = history.keys();
        ArrayList<String> keysToRemove = new ArrayList<>();

        while (keys.hasNext()){
            String key = keys.next();
            long value = Long.parseLong(key);
            if(value < time)
                keysToRemove.add(key);
        }
        for(String key:keysToRemove)
            history.remove(key);

        return history;
    }

    public void checkTargetLocally(Context context) {
        JSONObject historyJsonObject = ImportantStuffs.getJsonObject("History.json", context);
        JSONObject currentHourChecker = ImportantStuffs.getJsonObject("notificationErrorChecker.json" , context);
        JSONObject info = new JSONObject();
        String jsonString = ImportantStuffs.getStringFromJsonObjectPath("info.json", context);

        try {
            info = new JSONObject(jsonString);
        } catch (Exception e) {
        }

        try {
            JSONObject appsInfo = info.getJSONObject("appsInfo");

            Iterator<String> keys = appsInfo.keys();

            long dailyTargetStartTime, weeklyTargetStartTime;

            dailyTargetStartTime = ImportantStuffs.getDayStartingHour();

            weeklyTargetStartTime = ImportantStuffs.getWeekStartTimeFromTime(System.currentTimeMillis());


            // HashMap<String, AppUsageInfo> dailyData = getAppsUsageInfo(dailyTargetStartTime, System.currentTimeMillis(),context);
            // HashMap<String, AppUsageInfo> weeklyData = getAppsUsageInfo(weeklyTargetStartTime, System.currentTimeMillis(),context);


            //ImportantStuffs.notificationString = new StringBuilder();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject individualApp = (JSONObject) appsInfo.get(key);

                JSONArray targetTypes = individualApp.getJSONArray("targetTypes");
                int typeCount = targetTypes.length();
                int daily = 1, weekly = 0;

                for (int i = 0; i < typeCount; i++) {
                    int dailyOrWeekly = targetTypes.getInt(i);
                    if (dailyOrWeekly == daily) {
                        JSONArray dailyNotifications = individualApp.getJSONArray("dailyNotifications");
                        individualApp = getSingleHistory("dailyTarget", "DailyInfo",
                                getDailyUsageData(currentHourChecker,historyJsonObject, dailyTargetStartTime, ImportantStuffs.addDot(key), context), key,
                                context, individualApp, dailyTargetStartTime, dailyNotifications);
                    } else {
                        JSONArray weeklyNotifications = individualApp.getJSONArray("weeklyNotifications");
                        individualApp = getSingleHistory("weeklyTarget", "WeeklyInfo",
                                getWeeklyUsageData(currentHourChecker,historyJsonObject, weeklyTargetStartTime, ImportantStuffs.addDot(key), context), key,
                                context, individualApp, weeklyTargetStartTime, weeklyNotifications);
                    }
                }
                if (typeCount == 0) {
                    individualApp = setRecentHistoryToNull("DailyInfo", individualApp, dailyTargetStartTime, ImportantStuffs.addDot(key));
                    individualApp = setRecentHistoryToNull("WeeklyInfo", individualApp, weeklyTargetStartTime, ImportantStuffs.addDot(key));
                } else if (typeCount == 1) {
                    if (targetTypes.getInt(0) == weekly) {
                        individualApp = setRecentHistoryToNull("DailyInfo", individualApp, dailyTargetStartTime, ImportantStuffs.addDot(key));
                    } else {
                        individualApp = setRecentHistoryToNull("WeeklyInfo", individualApp, weeklyTargetStartTime, ImportantStuffs.addDot(key));
                    }
                }

                appsInfo.put(key, individualApp);
            }
            info.put("appsInfo", appsInfo);
            ImportantStuffs.saveFileLocally("info.json", info.toString(), context);

            // if(!ImportantStuffs.notificationString.toString().equals("")){
            //    ImportantStuffs.displayNotification("UsageInfo",ImportantStuffs.notificationString.toString(),context);
            //}

        } catch (JSONException e) {
        }
    }

    public JSONObject getSingleHistory(String targetType, String infoName, Long usedTime, String packageName, Context context,
                                       JSONObject individualApp, Long startTime, JSONArray notifications) throws JSONException {

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
        checkNotification(infoName, (int) percentage, notifications, packageName);
        //

        return individualApp;
    }

    public void checkNotification(String infoName, int percentage, JSONArray notifications, String packageName) {

        boolean flag = false;
        int tempPercentage = 0;

        JSONObject notificationInfo = new JSONObject();
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


        try {
            String jsonString = ImportantStuffs.getStringFromJsonObjectPath("notificationInfo.json", context);
            notificationInfo = new JSONObject(jsonString);
        } catch (Exception e) {
        }


        for (int i = 0; i < notifications.length(); i++) {
            //Log.d("trycatch",notifications.length()+"");
            try {
                if (percentage >= notificationTypes.getInt(notifications.getInt(i)) && notificationTypes.getInt(notifications.getInt(i)) >= tempPercentage) {
                    flag = true;
                    tempPercentage = notificationTypes.getInt(notifications.getInt(i));
                }
            } catch (Exception e) {
            }
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
                    ImportantStuffs.saveFileLocally("notificationInfo.json", notificationInfo.toString(), context);
                    int mode = (infoName.equals("DailyInfo")) ? 1 : 0;

                    try {
                        ImportantStuffs.showLog("Notification incoming...");
                        ImportantStuffs.displayNotification(ImportantStuffs.addDot(packageName), tempPercentage, mode, context);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        ImportantStuffs.showErrorLog("Can't show notification");
                    }

                } catch (Exception e1) {

                }
            }
        }
    }

    public JSONObject setRecentHistoryToNull(String infoName, JSONObject individualApp, Long startTime, String packageName) throws JSONException {

        //set notificationInfo of current day to null
        JSONObject data, appData, notificationInfo;
        notificationInfo = ImportantStuffs.getJsonObject("notificationInfo.json",context);
        data = new JSONObject();
        appData = new JSONObject();
        try {
            appData = notificationInfo.getJSONObject(ImportantStuffs.removeDot(packageName));
            data = appData.getJSONObject(infoName);
            data.put(ImportantStuffs.getDayStartingHour() + "", null);
        }catch (Exception e){
            data.put(ImportantStuffs.getDayStartingHour() + "", null);
            appData.put(infoName, data);
            notificationInfo.put(ImportantStuffs.removeDot(packageName), appData);
        }
        ImportantStuffs.saveFileLocally("notificationInfo.json", notificationInfo.toString(), context);
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
            Log.d("flag", "DataController: started");
            saveUsageDataLocally();
            checkTargetLocally(context);
            ImportantStuffs.saveEverything(context);
            Log.d("flag", "DataController: ended");
            return null;
        }

    }

}
