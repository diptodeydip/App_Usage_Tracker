package com.example.app_usage_tracker;

import android.graphics.drawable.Drawable;

class AppUsageInfo {
    Drawable appIcon; // You may add get this usage data also, if you wish.
    String appName, packageName;
    long timeInForeground = 0;
    int launchCount=0;
    long lastTimeUsed = 0;

    AppUsageInfo(String pName) {
        this.packageName=pName;
    }
}
