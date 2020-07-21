package com.example.app_usage_tracker;

import android.os.Build;

import java.util.Comparator;

import androidx.annotation.RequiresApi;

class LastTimeUsedComparator implements Comparator<AppUsageInfo> {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public final int compare(AppUsageInfo a, AppUsageInfo b) {
        // return by descending order
        return (int)(b.lastTimeUsed - a.lastTimeUsed);
    }
}