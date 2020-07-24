package com.example.app_usage_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

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



        ArrayList<String> list = new ArrayList<>();

        for(int i =0; i<packages.size();i++){
            if((packages.get(i).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                list.add(packages.get(i).applicationInfo.loadLabel(pm).toString());
            }
        }
        String[] arraySpinner = new String[list.size()];
        for(int i =0; i<list.size();i++){
                arraySpinner[i]= list.get(i);
        }


        Spinner s = (Spinner) findViewById(R.id.Applist);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        Spinner s1 = (Spinner) findViewById(R.id.targetType);




    }
}