package com.ahtrapotpid.appusagetracker.applist;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ahtrapotpid.appusagetracker.AppListTabbed;
import com.ahtrapotpid.appusagetracker.R;

import java.util.ArrayList;
import java.util.List;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.daily, R.string.weekly};
    private final Context mContext;
    public AppListTabbed appListTabbed;

    public ArrayList<AppList> appLists = new ArrayList<>();

    public SectionsPagerAdapter(AppListTabbed appListTabbed, FragmentManager fm) {
        super(fm);
        mContext = appListTabbed;
        this.appListTabbed = appListTabbed;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment appList;
        if(position == 0)
            appList = new DailyAppList(appListTabbed, this);
        else
            appList = new WeeklyAppList(appListTabbed, this);
        appLists.add((AppList) appList);
        return appList;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    public void sort(int sortBy, boolean sortOrderAscending){
        for(AppList appList:appLists){
            appList.sort(sortBy, sortOrderAscending);
        }
    }

    public void filter(boolean systemAppFilter, boolean unusedAppFilter){
        for(AppList appList:appLists){
            appList.filter(systemAppFilter, unusedAppFilter);
        }
    }

    public void startWeeklyAppList(){
        appLists.get(1).startAppListAsync();
    }

}