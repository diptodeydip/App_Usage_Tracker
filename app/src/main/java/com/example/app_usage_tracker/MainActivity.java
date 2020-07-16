package com.example.app_usage_tracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

/**
 * Activity to display package usage statistics.
 */
public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {
    private static final String TAG = "UsageStatsActivity";
    private static final boolean localLOGV = false;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CODE =  100;
    private UsageStatsManager mUsageStatsManager;
    private LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    private PackageManager mPm;

    //private Toolbar tbar;

    public static class AppNameComparator implements Comparator<UsageStats> {
        private Map<String, String> mAppLabelMap;

        AppNameComparator(Map<String, String> appList) {
            mAppLabelMap = appList;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            String alabel = mAppLabelMap.get(a.getPackageName());
            String blabel = mAppLabelMap.get(b.getPackageName());
            return alabel.compareTo(blabel);
        }
    }

    public static class LastTimeUsedComparator implements Comparator<UsageStats> {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            // return by descending order
            return (int)(b.getLastTimeUsed() - a.getLastTimeUsed());
        }
    }

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int)(b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
        }
    }

    // View Holder used when displaying views
    static class AppViewHolder {
        TextView pkgName;
        TextView lastTimeUsed;
        TextView usageTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    class UsageStatsAdapter extends BaseAdapter {
        // Constants defining order for display order
        private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
        private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
        private static final int _DISPLAY_ORDER_APP_NAME = 2;

        private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
        private LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
        private UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
        private AppNameComparator mAppLabelComparator;
        private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
        private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        UsageStatsAdapter() {
            Calendar calendar = Calendar.getInstance();
          //  calendar.add(Calendar.DAY_OF_YEAR, -1);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.HOUR, 11);
            calendar.set(Calendar.AM_PM, Calendar.PM);
            long endTime = calendar.getTimeInMillis();

            calendar.set(Calendar.SECOND, 1);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.AM_PM, Calendar.AM);
            long startTime = calendar.getTimeInMillis();


            ArrayMap<String, UsageStats> map = new ArrayMap<>();



            // comment kora portion die usagestats anle vul result dey. tai queryAndAggregateUsageStats use kore result ana hoise.

//            Map<String, UsageStats> lUsageStatsMap = mUsageStatsManager.queryAndAggregateUsageStats(startTime,endTime);
//            if (lUsageStatsMap.isEmpty() ) {
//                return;
//            }
//            for (Map.Entry<String,UsageStats> entry : lUsageStatsMap.entrySet()){
//                final android.app.usage.UsageStats pkgStats = entry.getValue();


                final List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                        startTime, endTime);
                if (stats == null) {
                    return;
                }
                final int statCount = stats.size();
                for (int i = 0; i < statCount; i++) {
                    final android.app.usage.UsageStats pkgStats = stats.get(i);

                // load application labels for each application
                try {
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    String label = appInfo.loadLabel(mPm).toString();
                    mAppLabelMap.put(pkgStats.getPackageName(), label);

                    UsageStats existingStats =
                            map.get(pkgStats.getPackageName());
                    if (existingStats == null) {
                        map.put(pkgStats.getPackageName(), pkgStats);
                    }
//                    else {
//                        existingStats.add(pkgStats);
//                    }

                } catch (NameNotFoundException e) {
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
            final UsageStats pkgStats = mPackageStats.get(position);
            if (pkgStats != null) {
                label = mAppLabelMap.get(pkgStats.getPackageName());
                holder.pkgName.setText(label);
                holder.lastTimeUsed.setText(DateUtils.formatSameDayTime(pkgStats.getLastTimeUsed(),
                        System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
                holder.usageTime.setText(
                        DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
            } else {
                Log.w(TAG, "No usage stats info for package:" + position);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this,
                            mAppLabelMap.get(pkgStats.getPackageName())+" "+getCurrentTimeStamp(
                                    pkgStats.getFirstTimeStamp())+" "
                            +getCurrentTimeStamp(pkgStats.getLastTimeStamp()),
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

    public static String getCurrentTimeStamp(long x) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date(x);
        String strDate = sdf.format(now);
        return strDate;
    }

    /** Called w
     * hen
     * the
     * activity
     * is
     * first
     * created. */

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        //tbar = findViewById(R.id.mytoolbar);
        // setSupportActionBar(tbar);

        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        requestPermissionsManually();
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = getPackageManager();


        Spinner typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        typeSpinner.setOnItemSelectedListener(this);

        ListView listView = (ListView) findViewById(R.id.pkg_list);
        mAdapter = new UsageStatsAdapter();
        listView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                recreate();
                Toast.makeText(MainActivity.this,
                        "Refreshing",
                        Toast.LENGTH_SHORT)
                        .show();
                return true;
            default:
                return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void requestPermissionsManually() {
        List<UsageStats> stats = mUsageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis());
        boolean isEmpty = stats.isEmpty();

        //AutoStartHelper.getInstance().getAutoStartPermission(this);

//        try
//        {
//            //Open the specific App Info page:
//            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//            intent.setData(Uri.parse("package:" + this.getPackageName()));
//            this.startActivity(intent);
//        }
//        catch ( ActivityNotFoundException e )
//        {
//            //Open the generic Apps page:
//            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
//            this.startActivity(intent);
//        }

        if (isEmpty) {
            Toast.makeText(MainActivity.this,
                    "Allow App Usage Access",
                    Toast.LENGTH_SHORT)
                    .show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
        else{
            //startWorkManager();
            startAlarm();
        }
    }

    private void startAlarm(){
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MyBroadcastReceiver.class);

//        PendingIntent alarmUp = PendingIntent.getBroadcast(this, 0,
//                intent,
//                PendingIntent.FLAG_NO_CREATE);

        //Flag_No_Create dile jodi Intent already created na thake tokhon null return kore
//
//        if (alarmUp == null)
//        {
            alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000, alarmIntent);
            Toast.makeText(this, "Alarm is Set",Toast.LENGTH_LONG).show();
//        }
//        else{
//            Toast.makeText(this, "Alarm Already Active",Toast.LENGTH_LONG).show();
//        }
    }

//    private void startWorkManager(){
//        //Work Manager test
//        Data data = new Data.Builder()
//                .putString("input_data", "Data for Testing")
//                .build();
//
//        Constraints constraints = new Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build();
//
////        final OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(StoreData.class)
////                .setInputData(data)
////                .setConstraints(constraints)
////                .build();
//
//        //Minimum interval time is 15 minute....
//        final PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(StoreData.class,15, TimeUnit.MINUTES,15, TimeUnit.MINUTES)
//                .setInputData(data)
//                .setConstraints(constraints)
//                .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
//                .addTag("tagname")
//                .build();
//
//
//        WorkManager.getInstance().enqueueUniquePeriodicWork("Test", ExistingPeriodicWorkPolicy.KEEP,request);
//        WorkManager.getInstance().getWorkInfoByIdLiveData(request.getId())
//                .observe(this, new Observer<WorkInfo>() {
//                    @Override
//                    public void onChanged(@Nullable WorkInfo workInfo) {
//                        if (workInfo != null) {
//                            if (workInfo.getState().isFinished()) {
//                                Data data1 = workInfo.getOutputData();
//                                String output = data1.getString("output_data");
//                                Toast.makeText(MainActivity.this,
//                                        output,
//                                        Toast.LENGTH_SHORT)
//                                        .show();
//                            }
//                            String status = workInfo.getState().name();
//                            Toast.makeText(MainActivity.this,
//                                    status,
//                                    Toast.LENGTH_SHORT)
//                                    .show();
//                        }
//                    }
//                });
//        //Work manager test end
//    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.sortList(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }
}

