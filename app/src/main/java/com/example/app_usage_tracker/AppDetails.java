package com.example.app_usage_tracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public class AppDetails extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    public static final String TAG = "extra";

    BarChart chart;
    long currentGraphDate;

    TextView targetTypesTextView;
    ArrayList<String> targetTypes;
    ArrayList<Integer> selectedTargetTypeIndexes = new ArrayList<>();

    ConstraintLayout setWeeklyTargetLayout, setDailyTargetLayout;
    long weeklyTarget = 0, dailyTarget = 0;
    TextView weeklyTargetTextView, dailyTargetTextView;

    ConstraintLayout setWeeklyNotificationsLayout;
    TextView weeklyNotificationTypesTextView;
    ArrayList<Integer> weeklySelectedNotificationIndexes = new ArrayList<>();
    ConstraintLayout setDailyNotificationsLayout;
    TextView dailyNotificationTypesTextView;
    ArrayList<Integer> dailySelectedNotificationIndexes = new ArrayList<>();
    ArrayList<String> notificationTypes;

    ArrayList<Long> usageData;
    long usageCollectionTime;

    private String currentPackage = "", currentPackageNoDot = "";

    private JSONObject jsonInfo;

    public static final int MODE_WEEKLY = 0, MODE_DAILY = 1;
    private int calendarMode = MODE_DAILY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        String history = ImportantStuffs.getStringFromJsonObjectPath("History.json", this);
        if (history.equals(""))
            Toast.makeText(this, R.string.details_loading, Toast.LENGTH_LONG).show();

        currentPackage = getIntent().getStringExtra("packageName");
        currentPackageNoDot = ImportantStuffs.removeDot(currentPackage);
        String appName = ImportantStuffs.getAppName(currentPackage, this);
        setTitle(appName);

        initJson();

        usageCollectionTime = ImportantStuffs.getDayStartingHour();
        currentGraphDate = ImportantStuffs.getDayStartingHour();
        chart = findViewById(R.id.usage_graph);

        new GraphAsyncTask(this, MODE_DAILY).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        initTargetNotificationStuffs();
        testStuffs();
    }

    private void testStuffs() {

    }

    private void initJson() {
        jsonInfo = ImportantStuffs.getJsonObject("info.json", this);
        String thisAppInfo = "";
        try {
            thisAppInfo = jsonInfo.getJSONObject("appsInfo").getJSONObject(currentPackageNoDot).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(thisAppInfo.equals("")){
            ImportantStuffs.showLog("No app info for", ImportantStuffs.getAppName(currentPackage, this));
            try {
                JSONObject thisAppInfoJson = new JSONObject();
                thisAppInfoJson.put("targetTypes", new JSONArray());
                thisAppInfoJson.put("weeklyTarget", 14*ImportantStuffs.MILLISECONDS_IN_HOUR);
                thisAppInfoJson.put("weeklyNotifications", new JSONArray("[0]"));
                thisAppInfoJson.put("dailyTarget", 2*ImportantStuffs.MILLISECONDS_IN_HOUR);
                thisAppInfoJson.put("dailyNotifications", new JSONArray("[0]"));
                jsonInfo.getJSONObject("appsInfo").put(currentPackageNoDot, thisAppInfoJson);
                ImportantStuffs.saveFileLocally("info.json", jsonInfo.toString(), this);
                ImportantStuffs.showLog("App info has been created successfully");
            } catch (JSONException e) {
                e.printStackTrace();
                ImportantStuffs.showErrorLog("Can't put app to appInfo");
            }
        }

        {
            try {
                thisAppInfo = jsonInfo.getJSONObject("appsInfo").getJSONObject(currentPackageNoDot).toString();
            } catch (JSONException e) {
                thisAppInfo = "";
                e.printStackTrace();
            }
            Log.d(TAG, currentPackage + ": " + thisAppInfo);
        }
    }

    private void initTargetNotificationStuffs(){
        try {
            JSONObject thisAppInfoJson = jsonInfo.getJSONObject("appsInfo").getJSONObject(currentPackageNoDot);
            JSONArray targetTypesJson = thisAppInfoJson.getJSONArray("targetTypes");
            for(int i=0; i<targetTypesJson.length(); i++)
                selectedTargetTypeIndexes.add(targetTypesJson.getInt(i));
            weeklyTarget = thisAppInfoJson.getLong("weeklyTarget");
            dailyTarget = thisAppInfoJson.getLong("dailyTarget");
        } catch (JSONException e) {
            e.printStackTrace();
            ImportantStuffs.showErrorLog("Can't initialize target");
        }
        targetTypesTextView = findViewById(R.id.target_types_text);
        targetTypes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.target_types)));

        setWeeklyTargetLayout = findViewById(R.id.set_weekly_target_layout);
        weeklyTargetTextView = findViewById(R.id.weekly_target_text);
        setUsageTarget(MODE_WEEKLY, weeklyTarget);
        setDailyTargetLayout = findViewById(R.id.set_daily_target_layout);
        dailyTargetTextView = findViewById(R.id.daily_target_text);
        setUsageTarget(MODE_DAILY, dailyTarget);

        notificationTypes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.notification_types)));
        setWeeklyNotificationsLayout = findViewById(R.id.set_weekly_notifications_type);
        weeklyNotificationTypesTextView = findViewById(R.id.weekly_notifications_text);
        setDailyNotificationsLayout = findViewById(R.id.set_daily_notifications_type);
        dailyNotificationTypesTextView = findViewById(R.id.daily_notifications_text);
        try {
            JSONObject thisAppInfoJson = jsonInfo.getJSONObject("appsInfo").getJSONObject(currentPackageNoDot);
            JSONArray weeklyNotificationsJson = thisAppInfoJson.getJSONArray("weeklyNotifications");
            for(int i=0; i<weeklyNotificationsJson.length(); i++)
                weeklySelectedNotificationIndexes.add(weeklyNotificationsJson.getInt(i));
            JSONArray dailyNotificationsJson = thisAppInfoJson.getJSONArray("dailyNotifications");
            for(int i=0; i<dailyNotificationsJson.length(); i++)
                dailySelectedNotificationIndexes.add(dailyNotificationsJson.getInt(i));
        } catch (JSONException e) {
            e.printStackTrace();
            ImportantStuffs.showErrorLog("Can't initialize notifications");
        }
        setWeeklyNotificationsText();
        setDailyNotificationsText();

        setTargetTypes();
    }


    private void makeConstraintLayoutGrayedOut(ConstraintLayout layout, boolean value) {
        TextView targetText;
        if (value) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                targetText = (TextView) layout.getChildAt(i);
                targetText.setTextColor(Color.GRAY);
            }
            layout.setClickable(false);
        } else {
            for (int i = 0; i < layout.getChildCount(); i++) {
                targetText = (TextView) layout.getChildAt(i);
                targetText.setTextColor(Color.WHITE);
            }
            layout.setClickable(true);
        }
    }


    public void onTargetTypeClicked(View view) {
//        setTargetTypes();
        boolean[] selectedItems = {false, false};
        for (int i : selectedTargetTypeIndexes)
            selectedItems[i] = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(R.string.select_target_type).setMultiChoiceItems(R.array.target_types, selectedItems, (dialog, which, isChecked) -> {
            boolean contains = selectedTargetTypeIndexes.contains(which);
            if(isChecked && !contains)
                selectedTargetTypeIndexes.add(which);
            else if(!isChecked && contains)
                selectedTargetTypeIndexes.remove(selectedTargetTypeIndexes.indexOf(which));
            setTargetTypes();
        }).setPositiveButton(R.string.set, (dialog, id) -> {});

        builder.setOnDismissListener((dialog)-> AppsDataController.startAlarm(this, 50));
        builder.create();
        builder.show();
    }

    public void onSetWeeklyTargetClicked(View view) {
        int hour = (int) ImportantStuffs.getHourFromTime(weeklyTarget);
        int min = ImportantStuffs.getRemainingMinuteFromTime(weeklyTarget);
        showTimePickerDialog(MODE_WEEKLY, hour, min, 24*7-1, 59);
    }

    public void onSetDailyTargetClicked(View view) {
        int hour = (int) ImportantStuffs.getHourFromTime(dailyTarget);
        int min = (int) ImportantStuffs.getRemainingMinuteFromTime(dailyTarget);
        showTimePickerDialog(MODE_DAILY, hour, min, 23, 59);
    }

    public void onSetWeeklyNotificationsClicked(View view) {
        boolean[] selectedItems = {false, false, false, false, false, false};
        for (int i : weeklySelectedNotificationIndexes)
            selectedItems[i] = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(R.string.select_notification_type)
                .setMultiChoiceItems(R.array.notification_types, selectedItems, (dialog, which, isChecked) -> {
                    boolean contains = weeklySelectedNotificationIndexes.contains(which);
                    if (isChecked && !contains)
                        weeklySelectedNotificationIndexes.add(which);
                    else if (!isChecked && contains)
                        weeklySelectedNotificationIndexes.remove(weeklySelectedNotificationIndexes.indexOf(which));
                    setWeeklyNotificationsText();
                })
                .setPositiveButton(R.string.set, (dialog, id) -> {
                });
        builder.setOnDismissListener((dialog) -> AppsDataController.startAlarm(this, 50));

        builder.create();
        builder.show();
    }

    public void onSetDailyNotificationsClicked(View view) {
        boolean[] selectedItems = {false, false, false, false, false, false};
        for (int i : dailySelectedNotificationIndexes)
            selectedItems[i] = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(R.string.select_notification_type)
                .setMultiChoiceItems(R.array.notification_types, selectedItems, (dialog, which, isChecked) -> {
                    boolean contains = dailySelectedNotificationIndexes.contains(which);
                    if (isChecked && !contains)
                        dailySelectedNotificationIndexes.add(which);
                    else if (!isChecked && contains)
                        dailySelectedNotificationIndexes.remove(dailySelectedNotificationIndexes.indexOf(which));
                    setDailyNotificationsText();
                })
                .setPositiveButton(R.string.set, (dialog, id) -> {
                });
        builder.setOnDismissListener((dialog) -> AppsDataController.startAlarm(this, 50));

        builder.create();
        builder.show();
    }

    public void onTargetHistoryClicked(View view){
        Intent historyIntent = new Intent(this, TargetHistory.class);
        historyIntent.putExtra("packageName", currentPackage);
        startActivity(historyIntent);
    }

    private void showTimePickerDialog(int mode, int currentHour, int currentMin, int maxHour, int maxMin){
        final Dialog dialog = new Dialog(this);
        View dialogLayout = getLayoutInflater().inflate(R.layout.set_app_usage_target, null);
        NumberPicker hourPicker = dialogLayout.findViewById(R.id.hour_picker);
        NumberPicker minPicker = dialogLayout.findViewById(R.id.min_picker);
        Button cancelButton = dialogLayout.findViewById(R.id.cancelButton);
        Button setButton = dialogLayout.findViewById(R.id.setButton);

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(maxHour);
        hourPicker.setValue(currentHour);

        minPicker.setMinValue(0);
        minPicker.setMaxValue(maxMin);
        minPicker.setValue(currentMin);

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        setButton.setOnClickListener(v -> {
            int hour = hourPicker.getValue();
            int min = minPicker.getValue();

            if(hour == 0 && min == 0){
                Toast.makeText(this, "Target can't be 0", Toast.LENGTH_SHORT).show();
                if(mode == MODE_WEEKLY)
                    hour = 14;
                else
                    hour = 2;
            }

            setUsageTarget(mode, hour, min);
            dialog.dismiss();
            AppsDataController.startAlarm(this, 500);
        });

        dialog.setTitle("Set target");
        dialog.setContentView(dialogLayout);
        dialog.show();
    }


    private void setWeeklyNotificationsText() {
        int numOfNotification = weeklySelectedNotificationIndexes.size();

        if (numOfNotification == 0) {
            weeklyNotificationTypesTextView.setText("None");
        } else if (numOfNotification == 1) {
            String selectedNotification = notificationTypes.get(weeklySelectedNotificationIndexes.get(0));
            weeklyNotificationTypesTextView.setText(selectedNotification);
        } else {
            weeklyNotificationTypesTextView.setText("Multiple");
        }
        String data = weeklySelectedNotificationIndexes.toString();
        try {
            JSONArray notificationTypesJson = new JSONArray(data);
            jsonInfo.getJSONObject("appsInfo").getJSONObject(currentPackageNoDot).put("weeklyNotifications", notificationTypesJson);
            ImportantStuffs.saveFileLocally("info.json", jsonInfo.toString(), this);

        } catch (JSONException e) {
            e.printStackTrace();
            ImportantStuffs.showLog("Can't save weekly notification types");
        }
    }

    private void setDailyNotificationsText() {
        int numOfNotification = dailySelectedNotificationIndexes.size();

        if (numOfNotification == 0) {
            dailyNotificationTypesTextView.setText("None");
        } else if (numOfNotification == 1) {
            String selectedNotification = notificationTypes.get(dailySelectedNotificationIndexes.get(0));
            dailyNotificationTypesTextView.setText(selectedNotification);
        } else {
            dailyNotificationTypesTextView.setText("Multiple");
        }
        String data = dailySelectedNotificationIndexes.toString();
        try {
            JSONArray notificationTypesJson = new JSONArray(data);
            jsonInfo.getJSONObject("appsInfo").getJSONObject(currentPackageNoDot).put("dailyNotifications", notificationTypesJson);
            ImportantStuffs.saveFileLocally("info.json", jsonInfo.toString(), this);

        } catch (JSONException e) {
            e.printStackTrace();
            ImportantStuffs.showLog("Can't save daily notification types");
        }
    }

    private void setTargetTypes() {
        int numOfTargets = selectedTargetTypeIndexes.size();
        if(numOfTargets == 0){
            targetTypesTextView.setText("None");
            makeConstraintLayoutGrayedOut(setDailyTargetLayout, true);
            makeConstraintLayoutGrayedOut(setWeeklyTargetLayout, true);
            makeConstraintLayoutGrayedOut(setDailyNotificationsLayout, true);
            makeConstraintLayoutGrayedOut(setWeeklyNotificationsLayout, true);
        }
        else if(numOfTargets == 1){
            String targetType = targetTypes.get(selectedTargetTypeIndexes.get(0));
            targetTypesTextView.setText(targetType);
            if(targetType.equals("Daily")){
                makeConstraintLayoutGrayedOut(setWeeklyTargetLayout, true);
                makeConstraintLayoutGrayedOut(setDailyTargetLayout, false);
                makeConstraintLayoutGrayedOut(setWeeklyNotificationsLayout, true);
                makeConstraintLayoutGrayedOut(setDailyNotificationsLayout, false);
            }
            else{
                makeConstraintLayoutGrayedOut(setWeeklyTargetLayout, false);
                makeConstraintLayoutGrayedOut(setDailyTargetLayout, true);
                makeConstraintLayoutGrayedOut(setWeeklyNotificationsLayout, false);
                makeConstraintLayoutGrayedOut(setDailyNotificationsLayout, true);
            }
        }
        else{
            targetTypesTextView.setText("Weekly and Daily");
            makeConstraintLayoutGrayedOut(setWeeklyTargetLayout, false);
            makeConstraintLayoutGrayedOut(setDailyTargetLayout, false);
            makeConstraintLayoutGrayedOut(setWeeklyNotificationsLayout, false);
            makeConstraintLayoutGrayedOut(setDailyNotificationsLayout, false);
        }
        saveTargetTypes();
    }

    private void setUsageTarget(int mode, int hour, int min) {
        String targetText = hour + " hour " + min + " min";
        if(mode == MODE_WEEKLY)
            weeklyTargetTextView.setText(targetText);
        else
            dailyTargetTextView.setText(targetText);

        long target = hour * ImportantStuffs.MILLISECONDS_IN_HOUR + min * ImportantStuffs.MILLISECONDS_IN_MINUTE;
        try {
            if(mode == MODE_WEEKLY) {
                weeklyTarget = target;
                jsonInfo.getJSONObject("appsInfo").getJSONObject(currentPackageNoDot).put("weeklyTarget", target);
            }
            else {
                dailyTarget = target;
                jsonInfo.getJSONObject("appsInfo").getJSONObject(currentPackageNoDot).put("dailyTarget", target);
            }
            ImportantStuffs.saveFileLocally("info.json", jsonInfo.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
            ImportantStuffs.showErrorLog("Can't save target");
        }

    }

    private void setUsageTarget(int mode, long time){
        int hour = (int) ImportantStuffs.getHourFromTime(time);
        int min = (int) ImportantStuffs.getRemainingMinuteFromTime(time);

        setUsageTarget(mode, hour, min);
    }

    private void saveTargetTypes(){
        String data = selectedTargetTypeIndexes.toString();
        try {
            JSONArray targetTypes = new JSONArray(data);
            jsonInfo.getJSONObject("appsInfo").getJSONObject(currentPackageNoDot).put("targetTypes", targetTypes);
            ImportantStuffs.saveFileLocally("info.json", jsonInfo.toString(), this);

        } catch (JSONException e) {
            e.printStackTrace();
            ImportantStuffs.showLog("Can't save target types");
        }

    }


    private void createGraph() {
        initBarChart();

        BarDataSet set = getBarDataSetFromUsage();
        BarData data = new BarData(set);
        chart.setData(data);

        initXAxis();
        initYAxis();

        chart.invalidate();
        chart.animateY(1000, Easing.EaseOutCubic);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if(calendarMode == MODE_DAILY){
                    int min = (int) e.getY();
                    Toast.makeText(AppDetails.this, min + " min", Toast.LENGTH_SHORT).show();
                }
                else{
                    int hour = (int) e.getY();
                    int min = (int) ((e.getY() - (float) hour) * 60f);
                    Toast.makeText(AppDetails.this, String.format("%d hour %d min", hour, min), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });

    }

    private void updateGraphData() {
        chart.clear();
        createGraph();
    }

    private void initBarChart(){
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setDrawGridBackground(false);
        chart.setFitBars(true);
        chart.getDescription().setEnabled(false);
//        chart.setPinchZoom(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getLegend().setEnabled(true);
        chart.setExtraBottomOffset(10f);
    }

    private void initYAxis(){
        YAxis leftAxis = chart.getAxisLeft();
        if(usageData.size() == 24)
            leftAxis.setAxisMaximum(60);
        else{
            leftAxis.setAxisMaximum(24);
//            leftAxis.setAxisMaximum(chart.getData().getYMax());
        }
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(Color.WHITE);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
    }

    private void initXAxis(){
        XAxis xAxis = chart.getXAxis();

        ArrayList<String> label = new ArrayList<>();
        if(usageData.size() == 24){
            xAxis.setLabelCount(24);

            label.add("12 am");
            for(int i=1; i<=11; i++)
                label.add(i+" am");
            label.add("12 pm");
            for(int i=1; i<=11; i++)
                label.add(i+" pm");
        }
        else{
            xAxis.setLabelCount(7);

            for(long time=usageCollectionTime, count=0; count<7; time+=ImportantStuffs.MILLISECONDS_IN_DAY, count++)
                label.add(ImportantStuffs.getDayAndMonthFromMilliseconds(time));
        }


        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setLabelRotationAngle(90);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(label));
    }

    private BarDataSet getBarDataSetFromUsage(){
        ArrayList<BarEntry> dataValues = new ArrayList<>();
        int dataSize = usageData.size();
        for (int i = 0; i < dataSize; i++) {
            float time;
            if (dataSize == 24)
                time = ImportantStuffs.getMinuteFromTime(usageData.get(i));
            else
                time = ImportantStuffs.getHourFromTime(usageData.get(i));
            dataValues.add(new BarEntry(i, time));
        }
        String usageDate = ImportantStuffs.getDateFromMilliseconds(currentGraphDate);
        long usageSum = 0;
        for (long usage : usageData)
            usageSum += usage;
        String usageString = ImportantStuffs.getTimeFromMillisecond(usageSum);

        BarDataSet barDataSet = new BarDataSet(dataValues, usageDate + " (Overall Usage: " + usageString + ")");
        barDataSet.setColor(ContextCompat.getColor(this, R.color.barColor));
        barDataSet.setDrawValues(false);
        return barDataSet;
    }


    public void onDailyCalendarSelected(View view) {
        calendarMode = MODE_DAILY;
        usageCollectionTime = currentGraphDate;
        new GraphAsyncTask(this, MODE_DAILY).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onWeeklyCalendarSelected(View view) {
        calendarMode = MODE_WEEKLY;
        usageCollectionTime = ImportantStuffs.getWeekStartTimeFromTime(currentGraphDate);
        new GraphAsyncTask(this, MODE_WEEKLY).execute();
//        usageData = AppsDataController.getWeeklyUsageDataInDailyList(usageCollectionTime, currentPackage, this);
//        updateGraphData();
    }

    public void onPickDateClicked(View view) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(usageCollectionTime);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.DatePickerTheme,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        currentGraphDate = calendar.getTimeInMillis();
        if(calendarMode == MODE_DAILY)
            onDailyCalendarSelected(null);
        else
            onWeeklyCalendarSelected(null);
//        testStuffs();
    }


    private class GraphAsyncTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<AppDetails> activityWeakReference;
        private int mode;

        GraphAsyncTask(AppDetails activity, int mode) {
            activityWeakReference = new WeakReference<>(activity);
            this.mode = mode;
        }

        @Override
        protected void onPreExecute() {
            AppDetails activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing())
                return;

            if(mode == MODE_DAILY)
                activity.usageData = new ArrayList<>(Collections.nCopies(24, (long) 0));
            else
                activity.usageData = new ArrayList<>(Collections.nCopies(7, (long) 0));
            activity.updateGraphData();
        }

        @Override
        protected Void doInBackground(Void... aVoid) {
            Log.d("flag", "GraphAsync: started");
            AppDetails activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            JSONObject historyJsonObject = ImportantStuffs.getJsonObject("History.json", activity);
            JSONObject currentHourChecker = ImportantStuffs.getJsonObject("notificationErrorChecker.json" , activity);

            if (mode == MODE_DAILY) {
                activity.usageData = AppsDataController.getDailyUsageDataInHourlyList(currentHourChecker,historyJsonObject, usageCollectionTime, currentPackage, activity);
            } else {
                activity.usageData = AppsDataController.getWeeklyUsageDataInDailyList(currentHourChecker,historyJsonObject, usageCollectionTime, currentPackage, activity);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            AppDetails activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing())
                return;
            activity.updateGraphData();
            Log.d("flag", "GraphAsync: ended");
        }
    }
}