package com.example.app_usage_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class TargetStats extends AppCompatActivity {
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    public static String appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_stats);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);

        ListView lv = findViewById(R.id.targetApp);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Toast.makeText(getApplicationContext(), "" + listItems.get(position), Toast.LENGTH_SHORT).show();
                appName = listItems.get(position);
                if(!appName.equals("No Target is set yet"))
                startActivity(new Intent(TargetStats.this,AppwiseTargetStats.class));
            }
        });

        setListView();
    }

    void setListView() {
        JSONObject targetDetails = new JSONObject();
        String jsonString = MyBroadcastReceiver.readJSON("TargetMetaDetails.json", this);

        if (!jsonString.equals("")) {
            try {
                targetDetails = new JSONObject(jsonString);
                Iterator<String> keys = targetDetails.keys();
                while(keys.hasNext()) {
                    String key = keys.next();
                    listItems.add(key);
                   // Toast.makeText(getApplicationContext(), "" + key, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
            }
        }
        else {
            listItems.add("No Target is set yet");
        }
        adapter.notifyDataSetChanged();
    }

}