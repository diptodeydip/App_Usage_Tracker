package com.example.app_usage_tracker;


import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private UsageStatsManager mUsageStatsManager;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {


        long hour_in_mil = 1000*60*60*1; // In Milliseconds
        long end_time = System.currentTimeMillis();
        long start_time = end_time - hour_in_mil;

        HashMap<String, AppUsageInfo> map = getUsageStatistics(start_time,end_time,context);

        ArrayList<AppUsageInfo> smallInfoList = new ArrayList<>(map.values());

        String usageTime =     "";
        try {
            usageTime = DateUtils.formatElapsedTime(map.get("com.facebook.katana").timeInForeground / 1000);
        }catch (Exception e){}

        JSONObject usageDetails = readJSON("History.json");
        if(usageDetails==null)usageDetails = new JSONObject();


        try {
            usageDetails.put("Facebook",usageTime);
        } catch (JSONException e) {
//            Toast.makeText(context,x,Toast.LENGTH_LONG).show();
        }

        saveToPhone(usageDetails,"History.json");

        //to save in firebase database
        //startWork(usageDetails.toString());
        saveToFirebase(usageDetails.toString());

        displayNotification("Facebook Usage", usageTime,context);

        startAlarm(context);
    }

    void startWork(String jsonString){
        Data data = new Data.Builder()
                .putString("jsonString",jsonString)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        final OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(StoreData.class)
                .setInputData(data)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance().enqueue(request);
    }

    void saveToFirebase(String jsonString){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("User1");

        Map<String, Object> userMap = new Gson().fromJson(jsonString, new TypeToken<HashMap<String, Object>>() {}.getType());
        myRef.setValue(userMap);

    }

    public static String getCurrentTimeStamp(long x) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date(x);
        String strDate = sdf.format(now);
        return strDate;
    }


    public static void saveToPhone(JSONObject ob , String fileName){
        try {
            File path = Environment.getExternalStorageDirectory();
            File dir = new File(path + "/AppUsageTracker/");
            dir.mkdirs();
            File file = new File(dir, fileName);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(ob.toString());
            bw.close();
        } catch (Exception e) {

        }
    }

    public static JSONObject readJSON(String fileName){
        JSONObject ob;

        try
        {
            File file = new File( Environment.getExternalStorageDirectory() + "/" + "AppUsageTracker/"+fileName);

            StringBuilder data = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    data.append(line);
                    data.append("\n");
                }
                br.close();

            ob = new JSONObject(data.toString());
            return ob;
        } catch (Exception e) {
            return null;
        }
    }


    private void displayNotification(String task, String desc , Context context) {

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                1, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("dip", "Dipto", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "dip")
                .setContentTitle(task)
                .setContentText(desc)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent);

        manager.notify(1, builder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private  HashMap<String, AppUsageInfo> getUsageStatistics(long start_time, long end_time, Context context) {

        UsageEvents.Event currentEvent;
      //  List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, AppUsageInfo> map = new HashMap<>();
        HashMap<String, List<UsageEvents.Event>> sameEvents = new HashMap<>();

        UsageStatsManager mUsageStatsManager = (UsageStatsManager)
                context.getSystemService(Context.USAGE_STATS_SERVICE);

        if (mUsageStatsManager != null) {
            // Get all apps data from starting time to end time
            UsageEvents usageEvents = mUsageStatsManager.queryEvents(start_time, end_time);

            // Put these data into the map
            while (usageEvents.hasNextEvent()) {
                currentEvent = new UsageEvents.Event();
                usageEvents.getNextEvent(currentEvent);
                if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED ||
                        currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) {
                  //  allEvents.add(currentEvent);
                    String key = currentEvent.getPackageName();
                    if (map.get(key) == null) {
                        map.put(key, new AppUsageInfo(key));
                        sameEvents.put(key,new ArrayList<UsageEvents.Event>());
                    }
                    sameEvents.get(key).add(currentEvent);
                }
            }

            // Traverse through each app data and count launch, calculate duration
            for (Map.Entry<String,List<UsageEvents.Event>> entry : sameEvents.entrySet()) {
                int totalEvents = entry.getValue().size();
                if (totalEvents > 1) {
                    for (int i = 0; i < totalEvents - 1; i++) {
                        UsageEvents.Event E0 = entry.getValue().get(i);
                        UsageEvents.Event E1 = entry.getValue().get(i + 1);

                        if (E1.getEventType() == 1 || E0.getEventType() == 1) {
                            map.get(E1.getPackageName()).launchCount++;
                        }

                        if (E0.getEventType() == 1 && E1.getEventType() == 2) {
                            long diff = E1.getTimeStamp() - E0.getTimeStamp();
                            map.get(E0.getPackageName()).timeInForeground += diff;
                        }
                    }
                }
                // shurur event jodi app closing hoy taile start_time and app closing time er difference add korlam
                if (entry.getValue().get(0).getEventType() == 2) {
                    long diff = entry.getValue().get(0).getTimeStamp() - start_time;
                    map.get(entry.getValue().get(0).getPackageName()).timeInForeground += diff;
                }
                //shesher event jodi app starting hoy  tahole app starting time and end_time er diiferece add korlaam
                if (entry.getValue().get(totalEvents - 1).getEventType() == 1) {
                    long diff = end_time - entry.getValue().get(totalEvents - 1).getTimeStamp();
                    map.get(entry.getValue().get(totalEvents - 1).getPackageName()).timeInForeground += diff;
                }
            }
            return map;
        } else {
            Toast.makeText(context, "Sorry...", Toast.LENGTH_SHORT).show();
        }
         return null;
    }

    private void startAlarm(Context context){
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MyBroadcastReceiver.class);

        Calendar calendar = Calendar.getInstance();
        //  calendar.add(Calendar.DAY_OF_YEAR, -1);
        calendar.set(Calendar.SECOND, 0);

        long alarmtime = calendar.getTimeInMillis() + 1000 * 5;

//        PendingIntent alarmUp = PendingIntent.getBroadcast(context, 0,
//                intent,
//                PendingIntent.FLAG_NO_CREATE);
//
//        //Flag_No_Create dile jodi Intent already created na thake tokhon null return kore
//
//        if (alarmUp == null)
//        {
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmtime, alarmIntent);
            //Toast.makeText(context, "Alarm is Set",Toast.LENGTH_LONG).show();
//        }
//        else{
//            Toast.makeText(context, "Alarm Already Active",Toast.LENGTH_LONG).show();
//        }
    }
}