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
     public  final int _DISPLAY_ORDER_USAGE_TIME = 0;
     public  final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
     public  final int _DISPLAY_ORDER_APP_NAME = 2;
     public  final String TAG = "UsageStatsActivity";
     public  final boolean localLOGV = false;

     public int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
     public LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
     public UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
     public AppNameComparator mAppLabelComparator;
     public final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
     public static ArrayList<AppUsageInfo> mPackageStats = new ArrayList<>();
     LayoutInflater mInflater;
     PackageManager mPm;
    public static Context context;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    UsageStatsAdapter(Context contextOfUsagePage) {
        doTask(contextOfUsagePage);
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
        String label = null;
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
            if(label!=null)
                holder.pkgName.setText(label);
            else
                holder.pkgName.setText(pkgStats.packageName);
            holder.lastTimeUsed.setText(DateUtils.formatSameDayTime(pkgStats.lastTimeUsed,
                    System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
            //  holder.lastTimeUsed.setText(MyBroadcastReceiver.getCurrentTimeStamp(pkgStats.lastTimeUsed));
//            holder.usageTime.setText(
//                    DateUtils.formatElapsedTime(pkgStats.timeInForeground / 1000));
            holder.usageTime.setText(MyBroadcastReceiver.getHourMinuteSec(pkgStats.timeInForeground));
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
//        if (mDisplayOrder == sortOrder) {
//            // do nothing
//            return;
//        }
        mDisplayOrder= sortOrder;
        sortList();
    }
    private  void sortList() {
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public  void doTask(Context contextOfUsagePage){

        context = contextOfUsagePage;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = context.getPackageManager();

        Map<String, AppUsageInfo> lUsageStatsMap = MyBroadcastReceiver.getUsageStatistics(MainActivity.start_time,MainActivity.end_time,context);
        if (lUsageStatsMap.isEmpty() ) {
            return;
        }
        getLabel(lUsageStatsMap);
        mPackageStats.addAll(lUsageStatsMap.values());
        // Sort list
        sortList();
    }

    void getLabel(Map<String, AppUsageInfo> lUsageStatsMap){
        for (Map.Entry<String,AppUsageInfo> entry : lUsageStatsMap.entrySet()){
            final AppUsageInfo pkgStats = entry.getValue();

            // load application labels for each application
            try {
                ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.packageName, 0);
                String label = appInfo.loadLabel(mPm).toString();
                mAppLabelMap.put(pkgStats.packageName, label);
            } catch (PackageManager.NameNotFoundException e) {
                // This package may be gone.
            }
        }
        mAppLabelComparator = new AppNameComparator(mAppLabelMap);
    }
}
