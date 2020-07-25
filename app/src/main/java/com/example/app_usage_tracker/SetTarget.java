package com.example.app_usage_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SetTarget extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_target);

        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        final String[] appName = new String[1];
        final String[] targetType = new String[1];
        final long[] hour = new long[1];
        final long[] minute = new long[1];

        final EditText hourET,minuteET;
        hourET = findViewById(R.id.hour);
        minuteET = findViewById(R.id.minute);

        Button set = findViewById(R.id.set);


        final JSONObject targetDetails[] = {new JSONObject()};
        String jsonString =  MyBroadcastReceiver.readJSON("TargetDetails.json",this);


        if(jsonString!="") {
            try {
                targetDetails[0] = new JSONObject(jsonString);
            } catch (JSONException e) {}
        }



        ArrayList<String> list = new ArrayList<>();

        for(int i =0; i<packages.size();i++){
            if((packages.get(i).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                list.add(packages.get(i).applicationInfo.loadLabel(pm).toString());
            }
        }
        final String[] arraySpinner = new String[list.size()];
        for(int i =0; i<list.size();i++){
                arraySpinner[i]= list.get(i);
        }


        Spinner s = (Spinner) findViewById(R.id.Applist);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);


        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                appName[0] = arraySpinner[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });



        Spinner s1 = (Spinner) findViewById(R.id.targetType);
        s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position==0){
                    targetType[0] = "Daily";
                }
                else {
                    targetType[0] = "Weekly";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });


       // final JSONObject finalTargetDetails = targetDetails;
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(hourET.getText().toString())){
                    hourET.setError("Hour field is empty");
                }
                else if(TextUtils.isEmpty(minuteET.getText().toString())){
                    minuteET.setError("Minute field is empty");
                }
                else{
                    hour[0] = Integer.parseInt(hourET.getText().toString());
                    minute[0] = Integer.parseInt(minuteET.getText().toString());
                    final JSONObject[] appWiseDetails = {new JSONObject()};

                    try {
                        appWiseDetails[0] = (JSONObject) targetDetails[0].get(appName[0]);
                    } catch (JSONException e) {}

                    try {
                        appWiseDetails[0].put("targetInMilis",hour[0]*minute[0]*60*1000);
                        appWiseDetails[0].put("targetType",targetType[0]);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }

                    try {
                        targetDetails[0].put(appName[0],appWiseDetails[0]);
                        MyBroadcastReceiver.saveToPhone(targetDetails[0].toString(), "TargetDetails.json", getApplicationContext());
                        Toast.makeText(getApplicationContext(), "Target is set",Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
}