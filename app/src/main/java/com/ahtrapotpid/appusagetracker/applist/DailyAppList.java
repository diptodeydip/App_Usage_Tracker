package com.ahtrapotpid.appusagetracker.applist;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ahtrapotpid.appusagetracker.AppListAdapter;
import com.ahtrapotpid.appusagetracker.AppListTabbed;
import com.ahtrapotpid.appusagetracker.AppUsageInfo;
import com.ahtrapotpid.appusagetracker.AppsDataController;
import com.ahtrapotpid.appusagetracker.ImportantStuffs;
import com.ahtrapotpid.appusagetracker.R;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class DailyAppList extends Fragment {
    public static final String TAG = "temp";
    public AppListTabbed appListTabbed;
    public HashMap<String, AppUsageInfo> appsUsageInfo;
    AppListAdapter adapter;

    public DailyAppList() {
        // Required empty public constructor
    }

    public DailyAppList(AppListTabbed appListTabbed){
        this.appListTabbed = appListTabbed;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_daily_app_list, container, false);
        Log.d(TAG, "onCreateView: dailyAppList");
        createHistoryList(currentView);
        return currentView;
    }

    private void createHistoryList(View view){
        appsUsageInfo = ImportantStuffs.copyUsageMap(appListTabbed.appsListInfo);

        long startTime = ImportantStuffs.getDayStartingHour(), endTime = ImportantStuffs.getCurrentTime();
        appsUsageInfo = AppsDataController.getAppsUsageInfoFromJson(appsUsageInfo, startTime, endTime, getContext());

        RecyclerView recyclerView = view.findViewById(R.id.weekly_app_list_recycler_view);
        ArrayList<AppUsageInfo> appsInfoList = new ArrayList<>(appsUsageInfo.values());
        adapter = new AppListAdapter(getContext(), appsInfoList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter.filterApps(false, true);
        adapter.sort(-2, false);
    }
}