package com.example.app_usage_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class AppwiseTargetStats extends AppCompatActivity {
    TextView tv,status,targetTime;
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    Button dailyStat,weeklyStat,deleteDailyTarget,deleteWeeklyTarget;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appwise_target_stats);

        tv = findViewById(R.id.appName);
        tv.setText(TargetStats.appName);
        status = findViewById(R.id.status);
        targetTime = findViewById(R.id.targetTime);

        adapter = new ArrayAdapter<String>(this,
                R.layout.targethistoryformat,
                listItems);

        ListView lv = findViewById(R.id.targetDetails);
        lv.setAdapter(adapter);


        dailyStat = findViewById(R.id.btn1);
        weeklyStat = findViewById(R.id.btn2);
        deleteDailyTarget = findViewById(R.id.btn3);
        deleteWeeklyTarget = findViewById(R.id.btn4);

        setListView("DailyInfo");
        dailyStat .getBackground()
                .setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);

        dailyStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setListView("DailyInfo");
                dailyStat .getBackground()
                        .setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                weeklyStat.getBackground().clearColorFilter();
            }
        });
        weeklyStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setListView("WeeklyInfo");
                weeklyStat.getBackground()
                        .setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                dailyStat.getBackground().clearColorFilter();
            }
        });

        deleteDailyTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String jsonString = MyBroadcastReceiver.readJSON("TargetMetaDetails.json", getApplicationContext());
                JSONObject targetDetails = new JSONObject();
                try {
                    targetDetails = new JSONObject(jsonString);
                    JSONObject appDetails = targetDetails.getJSONObject(MyBroadcastReceiver.removeDot(TargetStats.appName));
                    appDetails.remove("Daily");
                    appDetails.remove("dailyTargetInMilis");
                    targetDetails.put(TargetStats.appName,appDetails);
                    MyBroadcastReceiver.saveToPhone(targetDetails.toString(), "TargetMetaDetails.json", getApplicationContext());
                    finish();
                }catch (Exception e){}
            }
        });
        deleteWeeklyTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String jsonString = MyBroadcastReceiver.readJSON("TargetMetaDetails.json", getApplicationContext());
                JSONObject targetDetails = new JSONObject();
                try {
                    targetDetails = new JSONObject(jsonString);
                    JSONObject appDetails = targetDetails.getJSONObject(MyBroadcastReceiver.removeDot(TargetStats.appName));
                    appDetails.remove("Weekly");
                    appDetails.remove("weeklyTargetInMilis");
                    targetDetails.put(TargetStats.appName,appDetails);
                    MyBroadcastReceiver.saveToPhone(targetDetails.toString(), "TargetMetaDetails.json", getApplicationContext());
                    finish();
                }catch (Exception e){}
            }
        });

    }

    void setListView(String type) {
        JSONObject targetDetails = new JSONObject();
        String jsonString = MyBroadcastReceiver.readJSON("TargetMetaDetails.json", this);

            try {
                targetDetails = new JSONObject(jsonString);
                JSONObject appDetails = targetDetails.getJSONObject(MyBroadcastReceiver.removeDot(TargetStats.appName));


                try {
                    if(type=="DailyInfo"){
                        status.setText("Daily Target: "+appDetails.get("Daily").toString());
                        if(appDetails.get("Daily").toString().equals("Active")){
                            targetTime.setText("Current Target Time: "+MyBroadcastReceiver.getHourMinuteSec(appDetails.getLong("dailyTargetInMilis")));
                            status.setTextColor(Color.GREEN);
                            targetTime.setVisibility(View.VISIBLE);
                            deleteDailyTarget.setVisibility(View.VISIBLE);
                            deleteWeeklyTarget.setVisibility(View.GONE);
                        }
                    }
                    else{
                        status.setText("Weekly Target: "+appDetails.get("Weekly").toString());
                        if(appDetails.get("Weekly").toString().equals("Active")){
                            targetTime.setText("Current Target Time: "+MyBroadcastReceiver.getHourMinuteSec(appDetails.getLong("weeklyTargetInMilis")));
                            status.setTextColor(Color.GREEN);
                            targetTime.setVisibility(View.VISIBLE);
                            deleteDailyTarget.setVisibility(View.GONE);
                            deleteWeeklyTarget.setVisibility(View.VISIBLE);
                        }
                    }
                }catch (Exception e){
                    status.setText("Inactive");
                    status.setTextColor(Color.RED);
                    targetTime.setVisibility(View.GONE);
                    deleteDailyTarget.setVisibility(View.GONE);
                    deleteWeeklyTarget.setVisibility(View.GONE);
                }




                try {
                    JSONObject appTargetDeatils = appDetails.getJSONObject(type);
                    //Log.w("checkifok","ok");
                    Iterator<String> keys = appTargetDeatils.keys();
                    listItems.clear();
                    if(!keys.hasNext())listItems.add("No record");

                    while(keys.hasNext()) {
                        String key = keys.next();
                        String value ;
                        JSONObject details = appTargetDeatils.getJSONObject(key);
                        value = "Time Range: "+details.getString("Time_Range").toString()+"\n";
                        if(type.equals("WeeklyInfo")){
                            value+= "Target at range: "+MyBroadcastReceiver.getHourMinuteSec(details.getLong("weeklyTargetInMilis"))+"\n";
                        }else{
                            value+= "Target at range: "+MyBroadcastReceiver.getHourMinuteSec(details.getLong("dailyTargetInMilis"))+"\n";
                        }
                        double percentage = details.getDouble("usageInPercentage");
                        value += "Used "+String.format("%.2f", percentage)+"% of target\n";
                        listItems.add(value);
                    }

                }catch (Exception e){
                    listItems.clear();
                    listItems.add("No record");
                }

                adapter.notifyDataSetChanged();

            } catch (Exception e) {
                status.setText("Inactive");
                status.setTextColor(Color.RED);
                targetTime.setVisibility(View.GONE);
                deleteDailyTarget.setVisibility(View.GONE);
                deleteWeeklyTarget.setVisibility(View.GONE);
                listItems.clear();
                listItems.add("No record");
                adapter.notifyDataSetChanged();
            }
    }
}