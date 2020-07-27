package com.example.app_usage_tracker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity{

    //start_time and end_time is for showing custom usage data in UsagePage.class
    public static long start_time,end_time;
    private UsageStatsManager mUsageStatsManager;
    private static final int EXTERNAL_STORAGE_CODE = 1;
    Button target,history;
    public static int sortFlag = 0 , historyFlag = 0;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        Calendar calendar = Calendar.getInstance();
        //  calendar.add(Calendar.DAY_OF_YEAR, -1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        start_time = calendar.getTimeInMillis();
        //Toast.makeText(this,calendar.getTime()+"",Toast.LENGTH_LONG).show();
        calendar.set(Calendar.HOUR, 11);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.AM_PM, Calendar.PM);

        end_time = calendar.getTimeInMillis();

        target  = findViewById(R.id.target);
        history = findViewById(R.id.history);
        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        requestReadWrite();



        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,UsagePage.class));
            }
        });
        target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,SetTarget.class));
            }
        });

        //tbar = findViewById(R.id.mytoolbar);
        // setSupportActionBar(tbar);



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
                Toast.makeText(MainActivity.this,
                        "Refreshing",
                        Toast.LENGTH_SHORT)
                        .show();
                return true;
            default:
                return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void requestReadWrite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, EXTERNAL_STORAGE_CODE);
            }
            else{
                requestAppUsage();
            }
        }
        else {
            requestAppUsage();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_CODE: {
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    requestAppUsage();
                } else {
                    Toast.makeText(this, "Please allow these permissions", Toast.LENGTH_SHORT).show();
                    requestReadWrite();
                }
                break;
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void requestAppUsage(){

        List<UsageStats> stats = mUsageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis());
        boolean isEmpty = stats.isEmpty();
        if (isEmpty) {
            Toast.makeText(MainActivity.this,
                    "Allow App Usage Access",
                    Toast.LENGTH_SHORT)
                    .show();
            showAlert(this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    dialog.dismiss();
                }
            });

        }else{
            requestAutoStart();
            startAlarm();
            target.setVisibility(View.VISIBLE);
            history.setVisibility(View.VISIBLE);
        }
    }

    void requestAutoStart(){
        AutoStartHelper.getInstance().getAutoStartPermission(this);
    }

    private void showAlert(Context context, DialogInterface.OnClickListener onClickListener) {

        new AlertDialog.Builder(context).setTitle("Allow App Usage Access")
                .setMessage("Please enable App Usage Access in settings.")
                .setPositiveButton("Go to Permission Page", onClickListener)
                .show().setCancelable(false);
    }

    private void startAlarm(){
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MyBroadcastReceiver.class);

        Calendar calendar = Calendar.getInstance();
        //  calendar.add(Calendar.DAY_OF_YEAR, -1);

        long alarmtime = calendar.getTimeInMillis() + 1000 * 5;

        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmtime, alarmIntent);
        Toast.makeText(this,"ALARM SET",Toast.LENGTH_LONG).show();
    }





}
