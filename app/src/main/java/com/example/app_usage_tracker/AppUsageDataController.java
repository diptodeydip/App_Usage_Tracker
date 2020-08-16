package com.example.app_usage_tracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppUsageDataController extends BroadcastReceiver {
    Context context;
    public static final String TAG = "ahtrap";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final long HOUR_IN_MILLIS = 60 * 1000 * 60;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
//        ImportantMethods.playRandomSound(context);
//        Log.d(TAG, "onReceive: ");

        sharedPreferences = context.getSharedPreferences(ImportantMethods.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        startAlarm(context);

        new Thread(() -> {
            AsyncUsageTask runner = new AsyncUsageTask();
            runner.execute();
        }).start();
    }

    private void startAlarm(Context context) {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AppUsageDataController.class);

        Calendar calendar = Calendar.getInstance();

        long alarmTime = calendar.getTimeInMillis() + 1000 * 60 * 20;

        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
    }

    public static HashMap<String, AppUsageInfo> getAppsUsageInfo(long startTime, long endTime, Context context) {
        HashMap<String, AppUsageInfo> appsUsageInfo = new HashMap<>();

        UsageEvents.Event currentEvent;
        HashMap<String, List<UsageEvents.Event>> sameEvents = new HashMap<>();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(startTime, endTime);

        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);

            final int RESUMED = UsageEvents.Event.ACTIVITY_RESUMED;
            final int PAUSED = UsageEvents.Event.ACTIVITY_PAUSED;
            if (currentEvent.getEventType() == RESUMED || currentEvent.getEventType() == PAUSED) {
                String packageName = currentEvent.getPackageName();
                String appName = ImportantMethods.getAppName(packageName, context);
                Drawable icon = ImportantMethods.getAppIcon(packageName, context);
                Long installationDate = ImportantMethods.getAppInstallationDate(packageName, context);
                boolean isSystemApp = ImportantMethods.isSystemPackage(packageName, context);

                if (appsUsageInfo.get(packageName) == null) {
                    appsUsageInfo.put(packageName, new AppUsageInfo(appName, packageName, icon, installationDate, isSystemApp));
                    sameEvents.put(packageName, new ArrayList<>());
                }
                sameEvents.get(packageName).add(currentEvent);
            }
        }

        // Traverse through each app data and count launch, calculate duration
        for (Map.Entry<String, List<UsageEvents.Event>> entry : sameEvents.entrySet()) {
            int totalEvents = entry.getValue().size();
            if (totalEvents > 1) {
                for (int i = 0; i < totalEvents - 1; i++) {
                    UsageEvents.Event E0 = entry.getValue().get(i);
                    UsageEvents.Event E1 = entry.getValue().get(i + 1);

                    if (E1.getEventType() == 1) {
                        appsUsageInfo.get(E1.getPackageName()).incrementLaunchCount();
                    }
                    if (E0.getEventType() == 1) {
                        appsUsageInfo.get(E1.getPackageName()).incrementLaunchCount();
                    }

                    if (E0.getEventType() == 1 && E1.getEventType() == 2) {
                        long diff = E1.getTimeStamp() - E0.getTimeStamp();
                        appsUsageInfo.get(E0.getPackageName()).addToTimeInForeground(diff);
                    }
                }
            }
            // shurur event jodi app closing hoy taile start_time and app closing time er difference add korlam
            if (entry.getValue().get(0).getEventType() == 2) {
                long diff = entry.getValue().get(0).getTimeStamp() - startTime;
                appsUsageInfo.get(entry.getValue().get(0).getPackageName()).addToTimeInForeground(diff);
            }
            //shesher event jodi app starting hoy  tahole app starting time and end_time er diiferece add korlaam
            if (entry.getValue().get(totalEvents - 1).getEventType() == 1) {
                appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).incrementLaunchCount();
                long diff = endTime - entry.getValue().get(totalEvents - 1).getTimeStamp();
                appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).addToTimeInForeground(diff);
            }

            appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).lastTimeUsed = entry.getValue().get(totalEvents - 1).getTimeStamp();

        }

        return appsUsageInfo;
    }

    private void saveUsageDataLocally() {
        Long checkpoint = sharedPreferences.getLong("checkpoint", 0);
        Long goalPoint = ImportantMethods.getMostRecentHourTime();
//        ImportantMethods.showLog("Checkpoint------GoalPoint");
//        ImportantMethods.showLog(checkpoint, goalPoint);
        if(goalPoint == checkpoint || checkpoint == 0)
            return;

        String jsonString = ImportantMethods.readJSON("History.json", context);
        JSONObject usageDetails = new JSONObject();
        if (jsonString != "") {
            try {
                usageDetails = new JSONObject(jsonString);
            } catch (Exception e) {
                return;
            }
        }

        for (long startTime = checkpoint ; startTime + HOUR_IN_MILLIS <= goalPoint; startTime += HOUR_IN_MILLIS) {
            ImportantMethods.showLog("getting new data!", ImportantMethods.getDateFromMilliseconds(startTime));
            HashMap<String, AppUsageInfo> appsUsageInfo = getAppsUsageInfo(startTime, startTime + HOUR_IN_MILLIS, context);

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
//        ImportantMethods.showLog(usageDetails.toString());

        editor.putLong("checkpoint", goalPoint);
        editor.commit();

        ImportantMethods.saveToPhone(usageDetails.toString(), "History.json", context);
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
        }

        @Override
        protected void onProgressUpdate(String... text) {
//            Log.d(TAG, "onProgressUpdate: ");
        }
    }
}
