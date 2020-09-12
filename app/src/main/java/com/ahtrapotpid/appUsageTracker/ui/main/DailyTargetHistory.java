package com.ahtrapotpid.appUsageTracker.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ahtrapotpid.appUsageTracker.AppDetails;
import com.ahtrapotpid.appUsageTracker.AppsDataController;
import com.ahtrapotpid.appUsageTracker.R;
import com.ahtrapotpid.appUsageTracker.TargetInfo;

import java.util.ArrayList;

public class DailyTargetHistory extends Fragment {
    private static final String ARG_PARAM1 = "param1", TAG = "temp";

    private String currentPackage;

    public DailyTargetHistory() {
        // Required empty public constructor
    }

    public static DailyTargetHistory newInstance(String currentPackage) {
        DailyTargetHistory fragment = new DailyTargetHistory();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, currentPackage);
        fragment.setArguments(args);
        Log.d(TAG, "newInstance: ");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentPackage = getArguments().getString(ARG_PARAM1);
        }
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_daily_target_history, container, false);
        Log.d(TAG, "onCreateView: ");
        createHistoryList(currentView);
        return currentView;
    }

    private void createHistoryList(View view){
        ArrayList<TargetInfo> dailyTargetInfo = AppsDataController.getTargetHistory(currentPackage, AppDetails.MODE_DAILY, getContext());
        RecyclerView recyclerView = view.findViewById(R.id.daily_target_list_recycler_view);
        HistoryListAdapter adapter = new HistoryListAdapter(getContext(), dailyTargetInfo, AppDetails.MODE_DAILY);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}