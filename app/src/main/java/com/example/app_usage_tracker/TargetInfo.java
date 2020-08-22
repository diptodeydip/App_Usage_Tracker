package com.example.app_usage_tracker;

public class TargetInfo {
    long date, target, usage;
    int mode;

    public TargetInfo(long date, long target, long usage, int mode) {
        this.date = date;
        this.target = target;
        this.usage = usage;
        this.mode = mode;
    }
}
