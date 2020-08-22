package com.example.app_usage_tracker.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.app_usage_tracker.R;

public class DailyTargetHistory extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1", TAG = "temp";

    // TODO: Rename and change types of parameters
    private String currentPackage;

    public DailyTargetHistory() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param currentPackage Parameter 1.
     * @return A new instance of fragment weekly_target_history.
     */
    // TODO: Rename and change types and number of parameters
    public static DailyTargetHistory newInstance(String currentPackage) {
        DailyTargetHistory fragment = new DailyTargetHistory();
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
        Log.d(TAG, "onCreate: daily "+currentPackage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weekly_target_history, container, false);
    }
}