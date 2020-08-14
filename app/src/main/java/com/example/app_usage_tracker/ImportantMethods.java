package com.example.app_usage_tracker;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ImportantMethods {

    public static long getDayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 001);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        return calendar.getTimeInMillis();
    }

    public static long getDayEndTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 11);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        calendar.set(Calendar.AM_PM, Calendar.PM);
        return calendar.getTimeInMillis();
    }

    public static String getDateFromMilliseconds(long milliSeconds) {
        String dateFormat = "dd/MM/yyyy hh:mm:ss.SSS a";
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
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

    public static String getTimeInAgoFromMillisecond(long time){
        if(time == 0)
            return "";
        long interval = Calendar.getInstance().getTimeInMillis() - time;
        String agoTime = getTimeFromMillisecond(interval);
        return agoTime + " ago";
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
}
