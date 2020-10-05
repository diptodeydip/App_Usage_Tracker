package com.ahtrapotpid.appusagetracker.applist;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ahtrapotpid.appusagetracker.AppListAdapter;
import com.ahtrapotpid.appusagetracker.AppListTabbed;
import com.ahtrapotpid.appusagetracker.AppUsageInfo;
import com.ahtrapotpid.appusagetracker.AppsDataController;
import com.ahtrapotpid.appusagetracker.ImportantStuffs;
import com.ahtrapotpid.appusagetracker.R;

import java.util.ArrayList;
import java.util.HashMap;

public class DailyAppList extends Fragment implements AppList{
    public static final String TAG = "temp";
    public AppListTabbed appListTabbed;
    public HashMap<String, AppUsageInfo> appsUsageInfo;
    AppListAdapter adapter;
    View currentView;
    SectionsPagerAdapter sectionsPagerAdapter;
    SwipeRefreshLayout refreshLayout;
    RecyclerView recyclerView;

    public DailyAppList() {
        // Required empty public constructor
    }

    public DailyAppList(AppListTabbed appListTabbed, SectionsPagerAdapter sectionsPagerAdapter){
        this.appListTabbed = appListTabbed;
        this.sectionsPagerAdapter = sectionsPagerAdapter;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_daily_app_list, container, false);
        refreshLayout = currentView.findViewById(R.id.refresh_layout);
        recyclerView = currentView.findViewById(R.id.weekly_app_list_recycler_view);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            startAppListAsync();
        });
        startAppListAsync();
        return currentView;
    }


    @Override
    public void startAppListAsync(){
        new DailyAppListAsyncTask().execute();
    }

    @Override
    public void createAppList(){
        ArrayList<AppUsageInfo> appsInfoList = new ArrayList<>(appsUsageInfo.values());
        adapter = new AppListAdapter(getContext(), appsInfoList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // always filter first then sort
        filter(appListTabbed.systemAppFilter, appListTabbed.unusedAppFilter);
        sort(appListTabbed.sortBy, appListTabbed.sortOrderAscending);
    }

    @Override
    public void sort(int sortBy, boolean sortOrderAscending) {
        adapter.sort(sortBy, sortOrderAscending);
    }

    @Override
    public void filter(boolean systemAppFilter, boolean unusedAppFilter) {
        adapter.filterApps(systemAppFilter, unusedAppFilter);
    }


    private class DailyAppListAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            refreshLayout.setRefreshing(true);
            recyclerView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("a_flag", "DailyAppListAsyncTask: started");
            appsUsageInfo = ImportantStuffs.copyUsageMap(appListTabbed.appsListInfo);
            long startTime = ImportantStuffs.getDayStartingHour(), endTime = ImportantStuffs.getCurrentTime();
            appsUsageInfo = AppsDataController.getAppsUsageInfoFromJson(appsUsageInfo, startTime, endTime, getContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            createAppList();
            refreshLayout.setRefreshing(false);
            recyclerView.setVisibility(View.VISIBLE);
            Log.d("a_flag", "DailyAppListAsyncTask: ended");
            sectionsPagerAdapter.startWeeklyAppList();
        }
    }
}