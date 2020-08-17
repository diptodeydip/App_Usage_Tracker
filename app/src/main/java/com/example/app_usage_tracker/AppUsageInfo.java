package com.example.app_usage_tracker;

import android.graphics.drawable.Drawable;

import java.text.SimpleDateFormat;
import java.util.Calendar;

class AppUsageInfo {
    Drawable appIcon;
    String appName = "Demo", packageName = "";
    long timeInForeground = 0, lastTimeUsed = 0, installationTime = 0;
    int launchCount = 0;
    boolean isSystemApp = true;

    @Override
    public String toString() {
        String usedTime = getTimeFromMillisecond(timeInForeground);
        String lastOpened = "Not opened today";
        if(lastTimeUsed != 0)
            lastOpened = getTimeInAgoFromMillisecond(lastTimeUsed);
        String result = String.format("Name: %s, Used time: %s, Last opened: %s, Launches: %d", appName, usedTime, lastOpened, launchCount);
        return result;
    }

    AppUsageInfo(String appName, String packageName, Drawable appIcon, long installationDate, boolean isSystemApp) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.isSystemApp = isSystemApp;
        this.installationTime = installationDate;
    }

    AppUsageInfo(String appName, String packageName, Drawable appIcon, long installationDate, long timeInForeground, long lastTimeUsed, boolean isSystemApp) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.isSystemApp = isSystemApp;
        this.installationTime = installationDate;
        this.timeInForeground = timeInForeground;
        this.lastTimeUsed = lastTimeUsed;
    }

    AppUsageInfo(String packageName){
        this.packageName = packageName;
    }


    public void incrementLaunchCount(){
        launchCount++;
    }

    public void addToTimeInForeground(long time){
        timeInForeground += time;
    }


    private String getDateFromMilliseconds(long milliSeconds) {
        String dateFormat = "dd/MM/yyyy hh:mm:ss.SSS a";
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private String getTimeFromMillisecond(long time){
        String timeString = "";
        Long second = time / 1000;
        Long min = second / 60;

        Long hour = min / 60;
        Long min_hour = min % 60;
        if(hour > 0)
            timeString = hour + " hour " + min_hour + " min";
        else
            timeString = min_hour + " min";
        return timeString;
    }

    private String getTimeInAgoFromMillisecond(long time){
        long interval = System.currentTimeMillis() - time;
        String agoTime = getTimeFromMillisecond(interval);
        return agoTime + " ago";
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getTimeInForeground() {
        return timeInForeground;
    }

    public int getLaunchCount() {
        return launchCount;
    }

    public long getLastTimeUsed() {
        return lastTimeUsed;
    }

    public long getInstallationTime() {
        return installationTime;
    }


    public void setTimeInForeground(long timeInForeground) {
        this.timeInForeground = timeInForeground;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setInstallationTime(long installationTime) {
        this.installationTime = installationTime;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }

    public void setLaunchCount(int launchCount) {
        this.launchCount = launchCount;
    }

    public void setLastTimeUsed(long lastTimeUsed) {
        this.lastTimeUsed = lastTimeUsed;
    }
}
