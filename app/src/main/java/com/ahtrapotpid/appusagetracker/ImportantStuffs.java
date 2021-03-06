package com.ahtrapotpid.appusagetracker;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static com.ahtrapotpid.appusagetracker.AppDetails.MODE_DAILY;

public class ImportantStuffs {
    public static final long MILLISECONDS_IN_MINUTE = 60000L, MILLISECONDS_IN_HOUR = 3600000L, MILLISECONDS_IN_DAY = 86400000L;
    public static final String TAG = "ahtrap";
    public static final String LAUNCHER_PACKAGE = "com.example.ui", THIS_APP_PACKAGE = "com.ahtrapotpid.appusagetracker";
    public static final String SETTINGS_PACKAGE = "com.android.settings", FILE_MANAGER_PACKAGE = "com.amaze.filemanager";
    public static final String USAGE_CHANNEL_ID = "1", UPDATE_CHANNEL_ID = "2", GENERAL_CHANNEL_ID = "3";
    private static final Random random = new Random();


    public static String getAppName(String packageName, Context context) {
        String appName = packageName;
        PackageManager mPm = context.getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = mPm.getApplicationInfo(packageName, 0);
            appName = appInfo.loadLabel(mPm).toString();
            if (appName == null)
                appName = packageName;
        } catch (Exception e) {

        }

        return appName;
    }

    public static Drawable getAppIcon(String packageName, Context context) {
        Drawable appIcon = null;
        try {
            appIcon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appIcon;
    }

    public static long getAppInstallationDate(String packageName, Context context) {
        long installed = 0;
        try {
            installed = context.getPackageManager().getPackageInfo(packageName, 0).firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {

        }
        return installed;
    }

    public static boolean isSystemPackage(String packageName, Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(packageName, 0);
            return ((ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);
        } catch (PackageManager.NameNotFoundException e) {
//            showLog(getAppName(packageName, context));
            return false;
        }
    }

    public static boolean isUsagePermissionEnabled(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, appInfo.uid, context.getPackageName());
            return mode == MODE_ALLOWED;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;

//        UsageStatsManager mUsageStatsManager;
//        mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//        ;
//            List<UsageStats> stats = mUsageStatsManager
//                    .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis());
//            boolean isEmpty = stats.isEmpty();
//            if (isEmpty) return false;
//            else return true;
    }


    public static long getDayStartingHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTimeInMillis();
    }

    public static long getDayEndingHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        return calendar.getTimeInMillis();
    }

    public static long getDayEndTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        return calendar.getTimeInMillis();
    }

    public static long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static long getRecentHourFromTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTimeInMillis();
    }

    public static long getWeekStartTimeFromTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return calendar.getTimeInMillis();
    }

    public static long getCurrentHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTimeInMillis();
    }

    public static String getWeekEndDateFromTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.DAY_OF_WEEK, 7);
        return getDateFromMilliseconds(calendar.getTimeInMillis());
    }

    public static String getDateAndTimeFromMilliseconds(long milliseconds) {
        String dateFormat = "dd MMM yyyy HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatter.format(calendar.getTime());
    }

    public static String getDateFromMilliseconds(long milliseconds) {
        String dateFormat = "dd MMM yyyy";
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatter.format(calendar.getTime());
    }

    public static String getDayAndMonthFromMilliseconds(long milliseconds) {
        String dateFormat = "dd MMM";
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatter.format(calendar.getTime());
    }

    public static String getTimeFromMillisecond(long time) {
        String timeString = "";
        Long second = time / 1000;
        Long min = second / 60;
        Long hour = min / 60;
        Long min_hour = min % 60;
        if (hour > 0)
            timeString += hour + " hour " + min_hour + " min";
        else
            timeString = min_hour + " min";
        return timeString;
    }

    public static float getMinuteFromTime(long time) {
        float minute = time / (1000 * 60);
        return minute;
    }

    public static float getHourFromTime(long time) {
        float minute = (float) time / (float) MILLISECONDS_IN_HOUR;
        return minute;
    }

    public static int getRemainingMinuteFromTime(long time) {
        long remainingTime = time % MILLISECONDS_IN_HOUR;
        return (int) (remainingTime / MILLISECONDS_IN_MINUTE);
    }

    public static String getTimeInAgoFromMillisecond(long time) {
        if (time == 0)
            return "";
        long interval = Calendar.getInstance().getTimeInMillis() - time;
        String agoTime = getTimeFromMillisecond(interval);
        return agoTime + " ago";
    }


    public static synchronized String getStringFromJsonObjectPath(String jsonFilePath, Context context) {
        try {
            String path = context.getExternalFilesDir("").getAbsolutePath();
            File file = new File(path + "/" + jsonFilePath);

            StringBuilder data = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                data.append(line);
                data.append("\n");
            }
            br.close();

            return data.toString();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorLog("Can't read string from", jsonFilePath);
            return "";
        }
    }

    public static synchronized JSONObject getJsonObject(String jsonFilePath, Context context) {
        String jsonString = getStringFromJsonObjectPath(jsonFilePath, context);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            showErrorLog("Can't read", jsonFilePath);
        }
        return jsonObject;
    }

    public static synchronized boolean saveFileLocally(String fileName, String fileContent, Context context) {
        try {
            String path = context.getExternalFilesDir("").getAbsolutePath();
            File file = new File(path, fileName);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fileContent);
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void playRandomSound(Context context) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_ALARM_ALERT_URI);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
    }

    public static int getRandomInt(int maxValue) {
        return random.nextInt(maxValue);
    }

    public static String removeDot(String x) {
        x = x.replaceAll("[.]", "_dot_");
        return x;
    }

    public static String addDot(String x) {
        x = x.replaceAll("_dot_", ".");
        return x;
    }


    public static int getCurrentWeekNumber(Context context) {
        JSONObject infoJson = getJsonObject("info.json", context);
        int weekNumber = 0;
        try {
            weekNumber = infoJson.getInt("weekNumber");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weekNumber;
    }


    public static void displayUsageNotification(String packageName, int percentage, long usageTime, int mode, Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(USAGE_CHANNEL_ID, "Target", NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            assert manager != null;
            manager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(context, AppDetails.class);
        notificationIntent.putExtra("packageName", packageName);
        notificationIntent.putExtra("mode", mode);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, 0);

        String usageTimeString = getTimeFromMillisecond(usageTime);
        String appName = getAppName(packageName, context);
        Drawable appIcon = getAppIcon(packageName, context);
        Bitmap icon = getBitmapFromDrawable(appIcon);
        String modeString = (mode == MODE_DAILY) ? "daily" : "weekly";
        String title = appName + "(" + usageTimeString + ")";

        int weekNumber = getCurrentWeekNumber(context);
        String description = "";
        if (weekNumber != 2)
            description = "Over " + percentage + "% of " + modeString + " target is used.";
        else {
            modeString = (mode == MODE_DAILY) ? "today." : "this week.";
            description = usageTimeString + " is used " + modeString;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, USAGE_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(title)
                .setContentText(description)
//                .setStyle(new NotificationCompat.BigTextStyle()
//                        .bigText(description))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setLargeIcon(icon)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setSmallIcon(R.drawable.aut_notification_icon)
                .setColor(ContextCompat.getColor(context, R.color.notificationColor));

        assert manager != null;

        manager.notify((int) (System.currentTimeMillis()) % 2000, builder.build());
    }

    public static void displayUpdateNotification(Context context){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.cancel(Integer.valueOf(UPDATE_CHANNEL_ID));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(UPDATE_CHANNEL_ID, "Update", NotificationManager.IMPORTANCE_LOW);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            assert manager != null;
            manager.createNotificationChannel(channel);
        }

        final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.update);
        String description = context.getResources().getString(R.string.update_description);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("Click to update")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(description))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(false)
                .setLargeIcon(icon)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.aut_notification_icon)
                .setColor(ContextCompat.getColor(context, R.color.notificationColor));

        manager.notify(Integer.valueOf(UPDATE_CHANNEL_ID), builder.build());
    }

    public static void displayNotification(Context context, String description) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(GENERAL_CHANNEL_ID, "General", NotificationManager.IMPORTANCE_LOW);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            assert manager != null;
            manager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("Important Notice")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(description))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setSmallIcon(R.drawable.aut_notification_icon)
                .setColor(ContextCompat.getColor(context, R.color.notificationColor));

        manager.notify(Integer.valueOf(GENERAL_CHANNEL_ID), builder.build());
    }


    public static void saveToFirebase(String jsonString, String path) {
        if (jsonString.equals("")) return;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path);

        Map<String, Object> userMap = getUserMap(jsonString);
        myRef.updateChildren(userMap);
    }

    public static String getUserInfo(Context context) {
        try {
            SharedPreferences sharedPreference = context.getSharedPreferences(MainActivity.SHARED_PREFERENCE, MainActivity.MODE_PRIVATE);
            String version = getAppVersion(context);
            long IsSaved = sharedPreference.getLong(version,0);

            if(IsSaved==0){
                sharedPreference.edit().putLong(version,getCurrentHour()).apply();
            }
            JSONObject ob = new JSONObject();
            Map<String,?> map = sharedPreference.getAll();

            for (Map.Entry<String,?> entry : map.entrySet())
            {
                try {
                    ob.put(removeDot(entry.getKey()), entry.getValue());
                }
                catch (Exception E){}
            }
            //Log.d("objectcheck","ok");
            return ob.toString();
        } catch (Exception ignored) {}
        return "";
    }


    public static void saveEverything(Context context) {
        try {
            SharedPreferences sharedPreference = context.getSharedPreferences(MainActivity.SHARED_PREFERENCE, MainActivity.MODE_PRIVATE);
            String regNo = sharedPreference.getString("regNo", "");

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.goOnline();

            //save 4json files recursively
            DatabaseReference db = database.getReference("UsageHistory/" + regNo);
            Map<String, Object> userMap = getUserMap(ImportantStuffs.getStringFromJsonObjectPath("History.json", context));

            db.updateChildren(userMap).addOnCompleteListener(aVoid -> {
                DatabaseReference db1 = database.getReference("UserInfo/" + regNo);
                Map<String, Object> userMap1 = getUserMap(getUserInfo(context));
                db1.updateChildren(userMap1).addOnCompleteListener(aVoid1 -> {
                    DatabaseReference db2 = database.getReference("TargetHistory/" + regNo);
                    Map<String, Object> userMap2 = getUserMap(ImportantStuffs.getStringFromJsonObjectPath("info.json", context));
                    db2.updateChildren(userMap2).addOnCompleteListener(aVoid2 -> {
//                        DatabaseReference db3 = database.getReference("NotificationFrequency/" + regNo);
//                        String data = ImportantStuffs.getStringFromJsonObjectPath("notificationInfo.json", context);
//                        if(!data.equals("")){
//                            Map<String, Object> userMap3 = getUserMap(data);
//                            db3.updateChildren(userMap3).addOnCompleteListener(aVoid3 -> database.goOffline());
//                        }
//                        else{
//                            database.goOffline();
//                        }
                        database.goOffline();
                    });
                });
            });

        }catch (Exception ignored){
            ignored.printStackTrace();
        }
    }

    public static Map<String,Object> getUserMap(String jsonString){
        return new Gson().fromJson(jsonString, new TypeToken<HashMap<String, Object>>() {
        }.getType());
    }

    public static HashMap<String, AppUsageInfo> copyUsageMap(HashMap<String, AppUsageInfo> original) {
        HashMap<String, AppUsageInfo> copy = new HashMap<String, AppUsageInfo>();

        for (Map.Entry<String, AppUsageInfo> entry : original.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().getClone());
        }
        return copy;
    }

    public static String getAppVersion(Context context){
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }


    public static void showLog(String message) {
        Log.v(TAG, message);
    }

    public static void showLog(String... messages) {
        String fullMessage = "";
        for (String message : messages) {
            fullMessage += message + " ";
        }
        showLog(fullMessage);
    }

    public static void showLog(int message) {
        showLog(Integer.toString(message));
    }

    public static void showLog(int... messages) {
        String fullMessage = "";
        for (int message : messages) {
            fullMessage += message + " ";
        }
        showLog(fullMessage);
    }

    public static void showLog(long message) {
        showLog(Long.toString(message));
    }

    public static void showLog(long... messages) {
        String fullMessage = "";
        for (long message : messages) {
            fullMessage += message + " ";
        }
        showLog(fullMessage);
    }


    public static void showErrorLog(String message) {
        Log.e(TAG, message);
    }

    public static void showErrorLog(String... messages) {
        String fullMessage = "";
        for (String message : messages) {
            fullMessage += message + " ";
        }
        showErrorLog(fullMessage);
    }

    public static void showErrorLog(int message) {
        showErrorLog(Integer.toString(message));
    }

    public static void showErrorLog(int... messages) {
        String fullMessage = "";
        for (int message : messages) {
            fullMessage += message + " ";
        }
        showErrorLog(fullMessage);
    }

    public static void showErrorLog(long message) {
        showErrorLog(Long.toString(message));
    }

    public static void showErrorLog(long... messages) {
        String fullMessage = "";
        for (long message : messages) {
            fullMessage += message + " ";
        }
        showLog(fullMessage);
    }

//    public static void saveAllAppName(Context context) {
//        try {
//            SharedPreferences sharedPreference = context.getSharedPreferences(MainActivity.SHARED_PREFERENCE, MainActivity.MODE_PRIVATE);
//            String regNo = sharedPreference.getString("regNo", "");
//            final PackageManager pm = context.getPackageManager();
//
////        //get a list of installed apps.
//            List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_META_DATA);
//
//            JSONObject installedApps = new JSONObject();
//            for (int i = 0; i < packages.size(); i++) {
////            if((packages.get(i).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
//                String label = null;
//                //label = packages.get(i).applicationInfo.loadLabel(pm).toString();
//                label = getAppName(packages.get(i).packageName, context);
//                try {
//                    installedApps.put(removeDot(packages.get(i).packageName), label);
//                } catch (Exception e) {
//                }
//                //}
//            }
//            saveToFirebase(installedApps.toString(), "InstalledApps/" + regNo);
//        } catch (Exception e) {
//        }
//    }

//    private HashMap<String, AppUsageInfo> getAllAppsInfo() {
//        PackageManager pm = getPackageManager();
//        HashMap<String, AppUsageInfo> apps = new HashMap<>();
//        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
//
//        for (int i = 0; i < packs.size(); i++) {
//            PackageInfo p = packs.get(i);
//            String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
//            Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
//            String packageName = p.applicationInfo.packageName;
//            long installed = 0;
//            try {
//                installed = pm.getPackageInfo(packageName, 0).firstInstallTime;
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
//            boolean isSystem = isSystemPackage(p);
//            apps.put(packageName, new AppUsageInfo(appName, packageName, icon, installed, isSystem));
//        }
//        return apps;
//    }


//    private boolean hasUsagePermission(){
//        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//        UsageEvents usageEvents = mUsageStatsManager.queryEvents(getDayStartTime(), getDayEndTime());
//        return usageEvents.hasNextEvent();
//    }


//    private void initAppsUsageInfo(){
//        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//        Long dayStartTime = ImportantMethods.getDayStartTime();
//        Long dayEndTime = ImportantMethods.getDayEndTime();
//        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(dayStartTime, dayEndTime);
//        for(UsageStats stat:stats.values()){
//
//            String packageName = stat.getPackageName();
//            String appName = getAppName(packageName, this);
//            Long usageTime = stat.getTotalTimeInForeground();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                usageTime = stat.getTotalTimeVisible();
//            }
//            Long lastUsedTime = stat.getLastTimeUsed();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                lastUsedTime = stat.getLastTimeVisible();
//            }
//            Long installationDate = getAppInstallationDate(packageName);
//            boolean isSystem = isSystemPackage(packageName);
//            Drawable appIcon = getAppIcon(packageName);
//
//            AppUsageInfo appUsageInfo = new AppUsageInfo(appName, packageName, appIcon, installationDate, usageTime, lastUsedTime, isSystem);
//            appsUsageInfo.put(packageName, appUsageInfo);
//
////            String usageTimeString = ImportantMethods.getTimeFromMillisecond(usageTime);
////            String lastUsedTimeString = ImportantMethods.getTimeInAgoFromMillisecond(lastUsedTime);
////            showLog("\n"+appName, "-->", "Used time:", usageTimeString, "|||| Last used time", "-->", lastUsedTimeString);
//        }
//
//        UsageEvents events = usageStatsManager.queryEvents(dayStartTime, dayEndTime);
//        UsageEvents.Event currentEvent = new UsageEvents.Event();
//        while (events.hasNextEvent()){
//            events.getNextEvent(currentEvent);
//            String packageName = currentEvent.getPackageName();
//            AppUsageInfo appUsageInfo = appsUsageInfo.get(packageName);
//            if(appUsageInfo != null){
//                if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED)
//                    appUsageInfo.incrementLaunchCount();
//            }
//        }
//
//        return;
//    }


//    void checkIfFirst(){
//        String jsonString =  ImportantMethods.readJSON("details.json",this);
//
//        if(jsonString=="") {
//            Calendar calendar = Calendar.getInstance();
//            // JSONObject userDetails = new JSONObject();
//
//            calendar.set(Calendar.MILLISECOND,0);
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.HOUR, 0);
//            calendar.add(Calendar.DAY_OF_WEEK,1);
//            calendar.add(Calendar.WEEK_OF_MONTH,-1);
//            calendar.set(Calendar.AM_PM, Calendar.AM);
//
//            long Time = calendar.getTimeInMillis();
//            showLog(ImportantMethods.getDateFromMilliseconds(Time));
//
//            try {
//                JSONObject userDetails = new JSONObject();
//                userDetails.put("InstallationTime" , calendar.getTimeInMillis());
//                userDetails.put("checkPoint" , Time);
//                MyBroadcastReceiver.saveToPhone(userDetails.toString(),"details.json" ,this);
//            } catch (JSONException e) {
//                Toast.makeText(this,"error",Toast.LENGTH_LONG).show();
//            }
//        }
////
////        String jsonString1 =  MyBroadcastReceiver.readJSON("userInfo.json",this);
////        if(jsonString1!="") {
////            startActivity(new Intent(Info.this,MainActivity.class));
////            finish();
////        }
//    }


//    public void initAppsUsageInfo() {
//        appsUsageInfo = new HashMap<>();
//        long startTime = ImportantMethods.getDayStartTime();
//        long endTme = ImportantMethods.getDayEndTime();
//
//        UsageEvents.Event currentEvent;
//        HashMap<String, List<UsageEvents.Event>> sameEvents = new HashMap<>();
//        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//        UsageEvents usageEvents = mUsageStatsManager.queryEvents(startTime, endTme);
//
//        while (usageEvents.hasNextEvent()) {
//            currentEvent = new UsageEvents.Event();
//            usageEvents.getNextEvent(currentEvent);
//
//            final int RESUMED = UsageEvents.Event.ACTIVITY_RESUMED;
//            final int PAUSED = UsageEvents.Event.ACTIVITY_PAUSED;
//            if (currentEvent.getEventType() == RESUMED || currentEvent.getEventType() == PAUSED) {
//                String packageName = currentEvent.getPackageName();
//                String appName = getAppName(packageName, this);
//                Drawable icon = getAppIcon(packageName);
//                Long installationDate = getAppInstallationDate(packageName);
//                boolean isSystemApp = isSystemPackage(packageName);
//
//                if (appsUsageInfo.get(packageName) == null) {
//                    appsUsageInfo.put(packageName, new AppUsageInfo(appName, packageName, icon, installationDate, isSystemApp));
//                    sameEvents.put(packageName, new ArrayList<>());
//                }
//                sameEvents.get(packageName).add(currentEvent);
//            }
//        }
//        addOtherAppsToAppsUsageInfo();
//
//        // Traverse through each app data and count launch, calculate duration
//        for (Map.Entry<String, List<UsageEvents.Event>> entry : sameEvents.entrySet()) {
//            int totalEvents = entry.getValue().size();
//            if (totalEvents > 1) {
//                for (int i = 0; i < totalEvents - 1; i++) {
//                    UsageEvents.Event E0 = entry.getValue().get(i);
//                    UsageEvents.Event E1 = entry.getValue().get(i + 1);
//
//                    if (E1.getEventType() == 1) {
//                        appsUsageInfo.get(E1.getPackageName()).incrementLaunchCount();
//                    }
//                    if (E0.getEventType() == 1) {
//                        appsUsageInfo.get(E1.getPackageName()).incrementLaunchCount();
//                    }
//
//                    if (E0.getEventType() == 1 && E1.getEventType() == 2) {
//                        long diff = E1.getTimeStamp() - E0.getTimeStamp();
//                        appsUsageInfo.get(E0.getPackageName()).addToTimeInForeground(diff);
//                    }
//                }
//            }
//            // shurur event jodi app closing hoy taile start_time and app closing time er difference add korlam
//            if (entry.getValue().get(0).getEventType() == 2) {
//                long diff = entry.getValue().get(0).getTimeStamp() - startTime;
//                appsUsageInfo.get(entry.getValue().get(0).getPackageName()).addToTimeInForeground(diff);
//            }
//            //shesher event jodi app starting hoy  tahole app starting time and end_time er diiferece add korlaam
//            if (entry.getValue().get(totalEvents - 1).getEventType() == 1) {
//                appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).incrementLaunchCount();
//                long diff = endTme - entry.getValue().get(totalEvents - 1).getTimeStamp();
//                appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).addToTimeInForeground(diff);
//            }
//
//            appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).lastTimeUsed = entry.getValue().get(totalEvents - 1).getTimeStamp();
//
//        }
//
//    }

//    private void dunno(){
//        for (Map.Entry<String, List<UsageEvents.Event>> entry : sameEvents.entrySet()) {
//            ImportantStuffs.showLog(packageName + " -> " + totalEvents, " -> " + ImportantStuffs.getTimeFromMillisecond(usageTime));

//            if (totalEvents > 1) {
//                for (int i = 0; i < totalEvents - 1; i++) {
//                    UsageEvents.Event E0 = entry.getValue().get(i);
//                    UsageEvents.Event E1 = entry.getValue().get(i + 1);
//
//                    if (E1.getEventType() == 1) {
//                        appsUsageInfo.get(E1.getPackageName()).incrementLaunchCount();
//                    }
//                    if (E0.getEventType() == 1) {
//                        appsUsageInfo.get(E1.getPackageName()).incrementLaunchCount();
//                    }
//
//                    if (E0.getEventType() == 1 && E1.getEventType() == 2) {
//                        long diff = E1.getTimeStamp() - E0.getTimeStamp();
//                        appsUsageInfo.get(E0.getPackageName()).addToTimeInForeground(diff);
//                    }
//                }
//            }
//
//            // shurur event jodi app closing hoy taile start_time and app closing time er difference add korlam
//            if (entry.getValue().get(0).getEventType() == 2) {
//                long diff = entry.getValue().get(0).getTimeStamp() - startTime;
//                appsUsageInfo.get(entry.getValue().get(0).getPackageName()).addToTimeInForeground(diff);
//            }
//
//            // shesher event jodi app starting hoy  tahole app starting time and end_time er diiferece add korlaam
//            if (entry.getValue().get(totalEvents - 1).getEventType() == 1) {
//                appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).incrementLaunchCount();
//                long diff = endTime - entry.getValue().get(totalEvents - 1).getTimeStamp();
//                appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).addToTimeInForeground(diff);
//            }
//
//            appsUsageInfo.get(entry.getValue().get(totalEvents - 1).getPackageName()).lastTimeUsed = entry.getValue().get(totalEvents - 1).getTimeStamp();
//        }
//    }

//    private BarDataSet getRandomBarDataSet(){
//        ArrayList<BarEntry> dataValues = new ArrayList<>();
//        for (int i = 0; i < 24; i++) {
//            dataValues.add(new BarEntry(i, ImportantStuffs.getRandomInt(60)));
//        }
//        BarDataSet barDataSet = new BarDataSet(dataValues, "");
//        barDataSet.setColor(ContextCompat.getColor(this, R.color.barGraphBarColor1));
//        barDataSet.setDrawValues(false);
//        return barDataSet;
//    }
}
