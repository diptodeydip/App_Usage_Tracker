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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.sql.Connection;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
public class UsagePage extends AppCompatActivity  {



    private UsageStatsAdapter mAdapter;
    //private Toolbar tbar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_usage_page);

        //tbar = findViewById(R.id.mytoolbar);
        // setSupportActionBar(tbar);

        Spinner typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position != MainActivity.sortFlag)
                {
                    MainActivity.sortFlag = position;
                    mAdapter.sortList(position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Spinner typeSpinner1 = (Spinner) findViewById(R.id.typeSpinner1);
        typeSpinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position != MainActivity.historyFlag)
                {
                    MainActivity.historyFlag = position;
                    setStartEndTime(position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        ListView listView = (ListView) findViewById(R.id.pkg_list);

        mAdapter = new UsageStatsAdapter(this);
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
                Toast.makeText(UsagePage.this,
                        "Refreshing",
                        Toast.LENGTH_SHORT)
                        .show();
                return true;
            default:
                return true;
        }
    }
    void setStartEndTime(int position){
        if(position == 0){
            setRange(0,0);
        } else if(position == 1){
            setRange(1,1);
        } else if(position == 2){
            setRange(2,2);
        } else if(position == 3){
            setRange(3,3);
        } else if(position == 4){
            setRange(4,4);
        } else if(position == 5){
            setRange(5,5);
        } else if(position == 6){
            setRange(6,6);
        } else if(position == 7){
            setRange(1,0);
        } else if(position == 8){
            setRange(2,0);
        } else if(position == 9){
            setRange(3,0);
        } else if(position == 10){
            setRange(4,0);
        }else if(position == 11){
            setRange(5,0);
        } else if(position == 12){
            setRange(6,0);
        }
    }
    //Kundin theke kundin porjonto set kora
    void setRange(int daysAgoStart, int daysAgoEnd){
        Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            calendar.add(Calendar.DAY_OF_MONTH,(-1)*daysAgoStart);
            calendar.set(Calendar.AM_PM, Calendar.AM);
            MainActivity.start_time = calendar.getTimeInMillis();
        Calendar calendar1 = Calendar.getInstance();

        calendar1.set(Calendar.HOUR, 11);
        calendar1.set(Calendar.MINUTE, 59);
        calendar1.set(Calendar.SECOND, 59);
        calendar1.add(Calendar.DAY_OF_MONTH,(-1)*daysAgoEnd);
        calendar1.set(Calendar.AM_PM, Calendar.PM);
        MainActivity.end_time = calendar1.getTimeInMillis();
        Toast.makeText(this,calendar.getTime()+"",Toast.LENGTH_LONG).show();
            recreate();
          //  mAdapter.notifyDataSetChanged();

    }
}

