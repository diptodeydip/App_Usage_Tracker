package com.example.app_usage_tracker.ui.main;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.app_usage_tracker.AppDetails;
import com.example.app_usage_tracker.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.weekly, R.string.daily};
    private final Context mContext;
    private String currentPackage;

    public SectionsPagerAdapter(Context context, FragmentManager fm, String currentPackage) {
        super(fm);
        mContext = context;
        this.currentPackage = currentPackage;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        // return PlaceholderFragment.newInstance(position + 1);
        Fragment targetHistory;
        if(position == AppDetails.MODE_DAILY)
            targetHistory = DailyTargetHistory.newInstance(currentPackage);
        else
            targetHistory = WeeklyTargetHistory.newInstance(currentPackage);
        return targetHistory;
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