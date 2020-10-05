package com.ahtrapotpid.appusagetracker;

import android.graphics.drawable.Drawable;

public class AppUsageInfo {
    Drawable appIcon;
    String appName = "", packageName = "";
    long timeInForeground = 0, lastTimeUsed = 0, installationTime = 0;
    int launchCount = 0;
    boolean isSystemApp = false;

    @Override
    public String toString() {
        String usedTime = ImportantStuffs.getTimeFromMillisecond(timeInForeground);
        String lastOpened = "Not opened today";
        if (lastTimeUsed != 0)
            lastOpened = ImportantStuffs.getTimeInAgoFromMillisecond(lastTimeUsed);
        String installationDate = ImportantStuffs.getDateFromMilliseconds(installationTime);
        String result = String.format("Name: %s, Installation date: %s, Used time: %s, Last opened: %s, Launches: %d", appName, installationDate, usedTime, lastOpened, launchCount);
        return result;
    }

    AppUsageInfo(String appName, String packageName, Drawable appIcon, long installationDate, boolean isSystemApp) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.isSystemApp = isSystemApp;
        this.installationTime = installationDate;
    }

    AppUsageInfo(String packageName) {
        this.packageName = packageName;
    }

    public AppUsageInfo getClone() {
        AppUsageInfo clone = new AppUsageInfo(appName, packageName, appIcon, installationTime, isSystemApp);
        clone.setTimeInForeground(timeInForeground);
        clone.setLastTimeUsed(lastTimeUsed);
        clone.setLaunchCount(launchCount);
        return clone;
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

    public void addToTimeInForeground(long time) {
        this.timeInForeground += time;
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

    public void addToLaunchCount(int count) {
        this.launchCount += count;
    }

    public void setLastTimeUsed(long lastTimeUsed) {
        this.lastTimeUsed = lastTimeUsed;
    }
}
