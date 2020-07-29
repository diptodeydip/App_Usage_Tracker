package com.example.app_usage_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
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

        Button dailyStat,weeklyStat;
        dailyStat = findViewById(R.id.btn1);
        weeklyStat = findViewById(R.id.btn2);

        setListView("DailyInfo");

        dailyStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setListView("DailyInfo");
            }
        });
        weeklyStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setListView("WeeklyInfo");
            }
        });

    }

    void setListView(String type) {
        JSONObject targetDetails = new JSONObject();
        String jsonString = MyBroadcastReceiver.readJSON("TargetDetails.json", this);

            try {
                targetDetails = new JSONObject(jsonString);
                JSONObject appDetails = targetDetails.getJSONObject(TargetStats.appName);


                try {
                    if(type=="DailyInfo"){
                        status.setText("Daily Target: "+appDetails.get("Daily").toString());
                        if(appDetails.get("Daily").toString().equals("Active")){
                            targetTime.setText("Current Target Time: "+DateUtils.formatElapsedTime(appDetails.getLong("dailyTargetInMilis") / 1000));
                            status.setTextColor(Color.GREEN);
                            targetTime.setVisibility(View.VISIBLE);
                        }
                        else {
                            status.setTextColor(Color.RED);
                            targetTime.setVisibility(View.GONE);
                        }
                    }
                    else{
                        status.setText("Weekly Target: "+appDetails.get("Weekly").toString());
                        if(appDetails.get("Weekly").toString().equals("Active")){
                            targetTime.setText("Current Target Time: "+DateUtils.formatElapsedTime(appDetails.getLong("weeklyTargetInMilis") / 1000));
                            status.setTextColor(Color.GREEN);
                            targetTime.setVisibility(View.VISIBLE);
                        }
                        else{
                            status.setTextColor(Color.RED);
                            targetTime.setVisibility(View.GONE);
                        }
                    }
                }catch (Exception e){
                    status.setText("Not set yet");
                    status.setTextColor(Color.RED);
                    targetTime.setVisibility(View.GONE);
                }




                try {
                    JSONObject appTargetDeatils = appDetails.getJSONObject(type);

                    Iterator<String> keys = appTargetDeatils.keys();
                    listItems.clear();
                    if(!keys.hasNext())listItems.add("No record");

                    while(keys.hasNext()) {
                        String key = keys.next();
                        String value ;
                        JSONObject details = appTargetDeatils.getJSONObject(key);
                        value = "Time Range: "+details.getString("Time_Range").toString()+"\n";
                        if(type=="WeeklyInfo"){
                            value+= "Target at range: "+DateUtils.formatElapsedTime(details.getLong("weeklyTargetInMilis") / 1000)+"\n";
                        }else{
                            value+= "Target at range: "+DateUtils.formatElapsedTime(details.getLong("dailyTargetInMilis") / 1000)+"\n";
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
            }
    }
}