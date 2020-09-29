package com.ahtrapotpid.appusagetracker.applist;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ahtrapotpid.appusagetracker.AppListTabbed;
import com.ahtrapotpid.appusagetracker.R;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.daily, R.string.weekly};
    private final Context mContext;
    public AppListTabbed appListTabbed;

    public SectionsPagerAdapter(AppListTabbed appListTabbed, FragmentManager fm) {
        super(fm);
        mContext = appListTabbed;
        this.appListTabbed = appListTabbed;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment appList;
        if(position == 0)
            appList = new DailyAppList(appListTabbed);
        else
            appList = new WeeklyAppList(appListTabbed);
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
}