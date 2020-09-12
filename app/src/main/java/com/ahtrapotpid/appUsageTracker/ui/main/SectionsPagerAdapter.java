package com.ahtrapotpid.appUsageTracker.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ahtrapotpid.appUsageTracker.R;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.daily, R.string.weekly};
    private final Context mContext;
    private String currentPackage;

    public SectionsPagerAdapter(Context context, FragmentManager fm, String currentPackage) {
        super(fm);
        mContext = context;
        this.currentPackage = currentPackage;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment targetHistory;
        if(position == 0)
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