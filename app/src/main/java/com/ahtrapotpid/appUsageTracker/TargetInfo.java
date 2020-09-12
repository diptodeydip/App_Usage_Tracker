package com.ahtrapotpid.appUsageTracker;

import androidx.annotation.NonNull;

public class TargetInfo {
    long date, target, usage;
    int mode;

    public TargetInfo(long date, long target, long usage, int mode) {
        this.date = date;
        this.target = target;
        this.usage = usage;
        this.mode = mode;
    }

    @NonNull
    @Override
    public String toString() {
        String formattedDate = getStringDate();
        String targetTime = getStringTarget();
        String usageTime = getStringUsage();
        String targetMode = (mode == AppDetails.MODE_WEEKLY) ? "Weekly" : "Daily";
        String info = String.format("%s -- %s used: %s -- %s target: %s", formattedDate, targetMode, usageTime, targetMode, targetTime);
        return info;
    }


    public String getStringDate() {
        String dateString = "";
        dateString += ImportantStuffs.getDateFromMilliseconds(date);
        if(mode == AppDetails.MODE_WEEKLY){
            String endDate = ImportantStuffs.getWeekEndDateFromTime(date);
            dateString += " - " + endDate;
        }
        return dateString;
    }

    public String getStringTarget() {
        return ImportantStuffs.getTimeFromMillisecond(target);
    }

    public String getStringUsage() {
        return ImportantStuffs.getTimeFromMillisecond(usage);
    }

    public String getStringPercentage() {
        double percentage = ( (double) usage/ (double) target ) * 100;
        return String.format("%.2f", percentage)+"%";
    }
}
