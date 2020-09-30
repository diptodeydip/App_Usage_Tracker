package com.ahtrapotpid.appusagetracker.applist;

public interface AppList {
    public void sort(int sortBy, boolean sortOrderAscending);
    public void filter(boolean systemAppFilter, boolean unusedAppFilter);
    public void startAppListAsync();
    public void createAppList();
}
