package com.example.app_usage_tracker;

import android.os.Bundle;
import android.widget.TextView;


import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app_usage_tracker.ui.main.SectionsPagerAdapter;

public class TargetHistory extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_history);

        String currentPackage = getIntent().getStringExtra("packageName");
        String appName = ImportantStuffs.getAppName(currentPackage, this);
        TextView title = findViewById(R.id.title);
        title.setText(appName);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), currentPackage);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }
}