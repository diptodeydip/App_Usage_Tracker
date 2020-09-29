package com.ahtrapotpid.appusagetracker.targethistory;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ahtrapotpid.appusagetracker.AppDetails;
import com.ahtrapotpid.appusagetracker.AppsDataController;
import com.ahtrapotpid.appusagetracker.R;
import com.ahtrapotpid.appusagetracker.TargetInfo;

import java.util.ArrayList;


public class WeeklyTargetHistory extends Fragment {
    private static final String ARG_PARAM1 = "param1", TAG = "temp";
    private String currentPackage;

    public WeeklyTargetHistory() {

    }

    public static WeeklyTargetHistory newInstance(String currentPackage) {
        WeeklyTargetHistory fragment = new WeeklyTargetHistory();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, currentPackage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentPackage = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View currentView = inflater.inflate(R.layout.fragment_weekly_target_history, container, false);
        createHistoryList(currentView);

//        Log.d(TAG, "onCreateView: " + ImportantStuffs.getTimeFromMillisecond(usage));
        return currentView;
    }

    private void createHistoryList(View view){
        ArrayList<TargetInfo> weeklyTargetInfo = AppsDataController.getTargetHistory(currentPackage, AppDetails.MODE_WEEKLY, getContext());
        RecyclerView recyclerView = view.findViewById(R.id.weekly_target_list_recycler_view);
        HistoryListAdapter adapter = new HistoryListAdapter(getContext(), weeklyTargetInfo, AppDetails.MODE_WEEKLY);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}