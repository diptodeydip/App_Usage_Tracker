package com.ahtrapotpid.appusagetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;


import com.ahtrapotpid.appusagetracker.applist.SectionsPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.HashMap;

public class AppListTabbed extends AppCompatActivity {
    public static final String TAG = "temp";
    public HashMap<String, AppUsageInfo> appsListInfo;

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    int sortBy;
    boolean systemAppFilter, unusedAppFilter, sortOrderAscending, listLoaded = false;
    public static final String SYSTEM_APP_FILTER = "systemAppFilter", UNUSED_APP_FILTER = "unusedAppFilter";
    public static final String APP_LIST_SHARED_PREFERENCE = "AppListSharedPreference";
    public static final String ASCENDING_SORT = "sortOrderAscending", SORT_BY = "sortBy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list_tabbed);

        appsListInfo = AppsDataController.getAppList(this);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        setTitle(R.string.app_name);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);

//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_list_menu, menu);

        try {
            menu.findItem(R.id.system_app_filter).setChecked(systemAppFilter);
            menu.findItem(R.id.unused_app_filter).setChecked(unusedAppFilter);
            menu.findItem(R.id.sort_ascending).setChecked(sortOrderAscending);
            menu.findItem(sortBy).setChecked(true);
        } catch (Exception e){
            e.printStackTrace();
            ImportantStuffs.showErrorLog("Menu item check failed");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        systemAppFilter = sharedPreference.getBoolean(SYSTEM_APP_FILTER, false);
        unusedAppFilter = sharedPreference.getBoolean(UNUSED_APP_FILTER, true);
        sortOrderAscending = sharedPreference.getBoolean(ASCENDING_SORT, false);
        sortBy = sharedPreference.getInt(SORT_BY, R.id.sort_by_usage_time);


        switch (item.getItemId()){
            case R.id.system_app_filter:
                systemAppFilter = !item.isChecked();
                item.setChecked(systemAppFilter);

//                adapter.filterApps(systemAppFilter, unusedAppFilter);
                editor.putBoolean(SYSTEM_APP_FILTER, systemAppFilter);
                break;

            case R.id.unused_app_filter:
                unusedAppFilter = !item.isChecked();
                item.setChecked(unusedAppFilter);

//                adapter.filterApps(systemAppFilter, unusedAppFilter);
                editor.putBoolean(UNUSED_APP_FILTER, unusedAppFilter);
                break;


            case R.id.sort_by_name:
                sortBy = R.id.sort_by_name;
                item.setChecked(true);

                editor.putInt(SORT_BY, sortBy);
                break;

            case R.id.sort_by_last_installed:
                sortBy = R.id.sort_by_last_installed;
                item.setChecked(true);

                editor.putInt(SORT_BY, sortBy);
                break;

            case R.id.sort_by_last_used:
                sortBy = R.id.sort_by_last_used;
                item.setChecked(true);

                editor.putInt(SORT_BY, sortBy);
                break;

            case R.id.sort_by_usage_time:
                sortBy = R.id.sort_by_usage_time;
                item.setChecked(true);

                editor.putInt(SORT_BY, sortBy);
                break;

            case R.id.sort_ascending:
                sortOrderAscending = !item.isChecked();
                item.setChecked(sortOrderAscending);
                editor.putBoolean(ASCENDING_SORT, sortOrderAscending);
                break;
        }

//        adapter.sort(sortBy, sortOrderAscending);
        editor.commit();
        return true;
    }
}