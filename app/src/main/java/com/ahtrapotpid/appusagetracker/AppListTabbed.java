package com.ahtrapotpid.appusagetracker;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;


import com.ahtrapotpid.appusagetracker.applist.SectionsPagerAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class AppListTabbed extends AppCompatActivity {
    public static final String TAG = "temp";
    public HashMap<String, AppUsageInfo> appsListInfo;

    SectionsPagerAdapter sectionsPagerAdapter;

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;

    public int sortBy;
    public boolean systemAppFilter, unusedAppFilter, sortOrderAscending;
    public static final String SYSTEM_APP_FILTER = "systemAppFilter", UNUSED_APP_FILTER = "unusedAppFilter";
    public static final String APP_LIST_SHARED_PREFERENCE = "AppListSharedPreference";
    public static final String ASCENDING_SORT = "sortOrderAscending", SORT_BY = "sortBy";


    private ViewPager viewPager;
    private AppBarLayout appBarLayout;

    private Animation topToBottomAnim, bottomToTopAnim;
    public ProgressDialog progressDialog;
    private ConstraintLayout splashScreenLayout;

    private ImageView logo;
    private TextView titleText, subText;
    public boolean appListLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list_tabbed);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        setTitle(R.string.app_name);

        splashScreenLayout = findViewById(R.id.splash_screen);
        logo = findViewById(R.id.app_icon);
        titleText = findViewById(R.id.title_text);
        subText = findViewById(R.id.sub_text);

        topToBottomAnim = AnimationUtils.loadAnimation(this, R.anim.top_to_bottom);
        bottomToTopAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);

        showSplashScreen();
        new Handler().postDelayed( ()-> hideSplashScreen(), 5000);

        progressDialog = new ProgressDialog(AppListTabbed.this, R.style.DialogTheme);
        progressDialog.setTitle("Loading apps usage info...");
        progressDialog.setMessage("Wait a few moments");
        progressDialog.setCanceledOnTouchOutside(false);

        viewPager = findViewById(R.id.view_pager);
        appBarLayout = findViewById(R.id.app_bar_layout);

        sharedPreference = getSharedPreferences(APP_LIST_SHARED_PREFERENCE, MODE_PRIVATE);
        editor = sharedPreference.edit();
        systemAppFilter = sharedPreference.getBoolean(SYSTEM_APP_FILTER, false);
        unusedAppFilter = sharedPreference.getBoolean(UNUSED_APP_FILTER, true);
        sortOrderAscending = sharedPreference.getBoolean(ASCENDING_SORT, false);
        sortBy = sharedPreference.getInt(SORT_BY, R.id.sort_by_usage_time);

        new AppListAsyncTask().execute();

//        appsListInfo = AppsDataController.getAppList(this);

//        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
//        ViewPager viewPager = findViewById(R.id.view_pager);
//        viewPager.setAdapter(sectionsPagerAdapter);
//        TabLayout tabs = findViewById(R.id.tabs);
//        tabs.setupWithViewPager(viewPager);
    }

    public void startFragments(){
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
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

                sectionsPagerAdapter.filter(systemAppFilter, unusedAppFilter);
//                adapter.filterApps(systemAppFilter, unusedAppFilter);
                editor.putBoolean(SYSTEM_APP_FILTER, systemAppFilter);
                break;

            case R.id.unused_app_filter:
                unusedAppFilter = !item.isChecked();
                item.setChecked(unusedAppFilter);

                sectionsPagerAdapter.filter(systemAppFilter, unusedAppFilter);
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

        sectionsPagerAdapter.sort(sortBy, sortOrderAscending);
//        adapter.sort(sortBy, sortOrderAscending);
        editor.commit();
        return true;
    }


    private void showSplashScreen(){
        setFullScreenMode(true);
        logo.setAnimation(topToBottomAnim);
        titleText.setAnimation(bottomToTopAnim);
        subText.setAnimation(bottomToTopAnim);
    }

    private void hideSplashScreen(){
        setFullScreenMode(false);
        splashScreenLayout.setVisibility(View.GONE);
        viewPager.setAlpha(1);
        appBarLayout.setAlpha(1);
        if(!appListLoaded){
            progressDialog.show();
        }
    }

    private void setFullScreenMode(boolean mode){
        View decorView = getWindow().getDecorView();
        if(mode){
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            getSupportActionBar().hide();
        } else{
            decorView.setSystemUiVisibility(0);
            getSupportActionBar().show();
        }
    }


    private class AppListAsyncTask extends AsyncTask<Void, Void, Void> {
        AppListAsyncTask() {

        }

        @Override
        protected void onPreExecute() {
//            progressDialog = new ProgressDialog(AppListTabbed.this, R.style.DialogTheme);
//            progressDialog.setTitle("Loading apps usage info...");
//            progressDialog.setMessage("Wait a moment");
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("flag", "AppListAsyncTask: started");
            appsListInfo = AppsDataController.getAppList(AppListTabbed.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("flag", "AppListAsyncTask: ended");
            appListLoaded = true;
            startFragments();
            progressDialog.cancel();
        }
    }
}