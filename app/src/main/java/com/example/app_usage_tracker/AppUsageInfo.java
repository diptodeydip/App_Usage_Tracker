package com.example.app_usage_tracker;

import android.graphics.drawable.Drawable;

class AppUsageInfo {
    Drawable appIcon;
    String appName = "Demo", packageName = "";
    long timeInForeground = 0, lastTimeUsed = 0, installationTime = 0, usageTarget = 0;
    int launchCount = 0;
    boolean isSystemApp = true;
    String targetType = "None";
    String notifications[];

    @Override
    public String toString() {
        String usedTime = ImportantStuffs.getTimeFromMillisecond(timeInForeground);
        String lastOpened = "Not opened today";
        if(lastTimeUsed != 0)
            lastOpened = ImportantStuffs.getTimeInAgoFromMillisecond(lastTimeUsed);
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

    AppUsageInfo(String targetType, long usageTarget, String[] notifications) {
        this.usageTarget = usageTarget;
        this.targetType = targetType;
        this.notifications = notifications;
    }

    AppUsageInfo(String packageName){
        this.packageName = packageName;
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

    public long getUsageTarget() {
        return usageTarget;
    }

    public String getTargetType() {
        return targetType;
    }

    public String[] getNotifications() {
        return notifications;
    }


    public void setUsageTarget(long usageTarget) {
        this.usageTarget = usageTarget;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public void setNotifications(String[] notifications) {
        this.notifications = notifications;
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
