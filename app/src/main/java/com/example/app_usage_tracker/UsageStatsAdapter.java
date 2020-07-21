package com.example.app_usage_tracker;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import androidx.annotation.RequiresApi;


@RequiresApi(api = Build.VERSION_CODES.KITKAT)
class UsageStatsAdapter extends BaseAdapter {
    // Constants defining order for display order
    private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
    private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
    private static final int _DISPLAY_ORDER_APP_NAME = 2;
    private static final String TAG = "UsageStatsActivity";
    private static final boolean localLOGV = false;

    private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
    private LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
    private UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
    private AppNameComparator mAppLabelComparator;
    private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
    private final ArrayList<AppUsageInfo> mPackageStats = new ArrayList<>();
    private LayoutInflater mInflater;
    private PackageManager mPm;
    Context context;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    UsageStatsAdapter(Context contextofUsagePage) {

        context = contextofUsagePage;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = context.getPackageManager();
        ArrayMap<String, AppUsageInfo> map = new ArrayMap<>();


        Map<String, AppUsageInfo> lUsageStatsMap = MyBroadcastReceiver.getUsageStatistics(MainActivity.start_time,MainActivity.end_time,context);
        if (lUsageStatsMap.isEmpty() ) {
            return;
        }
        for (Map.Entry<String,AppUsageInfo> entry : lUsageStatsMap.entrySet()){
            final AppUsageInfo pkgStats = entry.getValue();

            // load application labels for each application
            try {
                ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.packageName, 0);
                String label = appInfo.loadLabel(mPm).toString();
                mAppLabelMap.put(pkgStats.packageName, label);

                AppUsageInfo existingStats =
                        map.get(pkgStats.packageName);
                if (existingStats == null) {
                    map.put(pkgStats.packageName, pkgStats);
                }
            } catch (PackageManager.NameNotFoundException e) {
                // This package may be gone.
            }
        }
        mPackageStats.addAll(map.values());

        // Sort list
        mAppLabelComparator = new AppNameComparator(mAppLabelMap);
        sortList();
    }

    @Override
    public int getCount() {
        return mPackageStats.size();
    }

    @Override
    public Object getItem(int position) {
        return mPackageStats.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        AppViewHolder holder;
        String label;
        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.usage_stats_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new AppViewHolder();
            holder.pkgName = (TextView) convertView.findViewById(R.id.package_name);
            holder.lastTimeUsed = (TextView) convertView.findViewById(R.id.last_time_used);
            holder.usageTime = (TextView) convertView.findViewById(R.id.usage_time);
            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (AppViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder
        final AppUsageInfo pkgStats = mPackageStats.get(position);
        if (pkgStats != null) {
            label = mAppLabelMap.get(pkgStats.packageName);
            holder.pkgName.setText(label);
            holder.lastTimeUsed.setText(DateUtils.formatSameDayTime(pkgStats.lastTimeUsed,
                    System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
            //  holder.lastTimeUsed.setText(MyBroadcastReceiver.getCurrentTimeStamp(pkgStats.lastTimeUsed));
            holder.usageTime.setText(
                    DateUtils.formatElapsedTime(pkgStats.timeInForeground / 1000));
        } else {
            Log.w(TAG, "No usage stats info for package:" + position);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,
                        mAppLabelMap.get(pkgStats.packageName)+"",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
        return convertView;
    }

    void sortList(int sortOrder) {
        if (mDisplayOrder == sortOrder) {
            // do nothing
            return;
        }
        mDisplayOrder= sortOrder;
        sortList();
    }
    private void sortList() {
        if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
            if (localLOGV) Log.i(TAG, "Sorting by usage time");
            Collections.sort(mPackageStats, mUsageTimeComparator);
        } else if (mDisplayOrder == _DISPLAY_ORDER_LAST_TIME_USED) {
            if (localLOGV) Log.i(TAG, "Sorting by last time used");
            Collections.sort(mPackageStats, mLastTimeUsedComparator);
        } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
            if (localLOGV) Log.i(TAG, "Sorting by application name");
            Collections.sort(mPackageStats, mAppLabelComparator);
        }
        notifyDataSetChanged();
    }
}
