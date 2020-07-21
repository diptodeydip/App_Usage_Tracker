package com.example.app_usage_tracker;

import android.os.Build;

import java.util.Comparator;
import java.util.Map;

import androidx.annotation.RequiresApi;

class AppNameComparator implements Comparator<AppUsageInfo> {
    private Map<String, String> mAppLabelMap;

    AppNameComparator(Map<String, String> appList) {
        mAppLabelMap = appList;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public final int compare(AppUsageInfo a, AppUsageInfo b) {
        String alabel = mAppLabelMap.get(a.packageName);
        String blabel = mAppLabelMap.get(b.packageName);
        return alabel.compareTo(blabel);
    }
}
