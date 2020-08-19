package com.example.app_usage_tracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

public class AppDetails extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    BarChart chart;
    long currentGraphDate;

    int targetTypeIndex;
    String targetTypeString;
    TextView targetTypeTextView;
    ArrayList<String> targetTypes;

    ConstraintLayout setTargetLayout;
    int hourTarget, minTarget;
    TextView targetTextView;

    ConstraintLayout setNotificationsLayout;
    TextView notificationTypesTextView;
    ArrayList<String> notificationTypes;
    HashSet<String> selectedNotificationsString = new HashSet<>();
    ArrayList<Integer> selectedNotifications = new ArrayList<>();

    ArrayList<Long> usageData;
    long usageCollectionTime;

    private String currentPackage = "";

    private final int CALENDAR_DAILY = 0, CALENDAR_WEEKLY = 1;
    private int calendarMode = CALENDAR_DAILY;

//    private SharedPreferences targetSharedPreferences;
//    private SharedPreferences.Editor targetEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        initStuffs();
        createGraph();
        testStuffs();
    }

    private void testStuffs(){
//        ImportantStuffs.showLog(currentPackage, usageData.toString());
        HashSet<String> notifications = new HashSet<>();
        notifications.add("hello");
//        targetEditor.putStringSet("hi", notifications);
    }

    private void initStuffs() {
        currentPackage = getIntent().getStringExtra("packageName");
        usageCollectionTime = ImportantStuffs.getDayStartingHour();
        usageData = AppUsageDataController.getDailyUsageDataInHourlyList(usageCollectionTime, currentPackage, this);

//        targetSharedPreferences = getSharedPreferences(ImportantStuffs.SHARED_PREFERENCE_TARGET, MODE_PRIVATE);
//        targetEditor = targetSharedPreferences.edit();

        chart = findViewById(R.id.usage_graph);
        currentGraphDate = usageCollectionTime;

        targetTypeTextView = findViewById(R.id.target_type_text);
        targetTypes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.target_types)));
        targetTypeString = "None";
        targetTypeIndex = targetTypes.indexOf(targetTypeString);
        targetTypeTextView.setText(targetTypeString);

        setTargetLayout = findViewById(R.id.set_target);
        targetTextView = findViewById(R.id.target_text);
        hourTarget = 2;
        minTarget = 0;
        setUsageTarget(hourTarget, minTarget);


        setNotificationsLayout = findViewById(R.id.set_notifications_type);
        notificationTypesTextView = findViewById(R.id.notifications_text);
        notificationTypes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.notification_types)));
        selectedNotifications.add(0);
        setNotificationsText();

        if (targetTypeIndex == 2) {
            makeConstraintLayoutGrayedOut(setTargetLayout, true);
            makeConstraintLayoutGrayedOut(setNotificationsLayout, true);
        }
    }

    private void setNotificationsText() {
        switch (selectedNotifications.size()) {
            case 0:
                notificationTypesTextView.setText("None");
                break;
            case 1:
                String selectedNotification = notificationTypes.get(selectedNotifications.get(0));
                notificationTypesTextView.setText(selectedNotification);
                break;
            default:
                notificationTypesTextView.setText("Multiple");
        }
    }

    private void setUsageTarget(int hour, int min) {
//        targetEditor.putInt("hourTarget", hour);
//        targetEditor.putInt("minTarget", min);
//        targetEditor.commit();

        String targetText = hour + " hour " + min + " min";
        targetTextView.setText(targetText);
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

    private void createGraph() {
        initBarChart();

        BarDataSet set = getBarDataSetFromUsage();
        BarData data = new BarData(set);
        chart.setData(data);

        initXAxis();
        initYAxis();

        chart.invalidate();
        chart.animateY(1000, Easing.EaseOutCubic);
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
        chart.setPinchZoom(false);
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
        for(int i=0; i<dataSize; i++){
            float time;
            if(dataSize == 24)
                time = ImportantStuffs.getMinuteFromTime(usageData.get(i));
            else
                time = ImportantStuffs.getHourFromTime(usageData.get(i));
            dataValues.add(new BarEntry(i, time));
        }
        BarDataSet barDataSet = new BarDataSet(dataValues, ImportantStuffs.getDateFromMilliseconds(currentGraphDate));
        barDataSet.setColor(ContextCompat.getColor(this, R.color.barGraphBarColor1));
        barDataSet.setDrawValues(false);
        return barDataSet;
    }


    public void onTargetTypeClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(R.string.select_target_type).setSingleChoiceItems(R.array.target_types, targetTypeIndex, (dialog, which) -> {
            if (targetTypeIndex == 2 && which != 2) {
                makeConstraintLayoutGrayedOut(setTargetLayout, false);
                makeConstraintLayoutGrayedOut(setNotificationsLayout, false);
            } else if (which == 2) {
                makeConstraintLayoutGrayedOut(setTargetLayout, true);
                makeConstraintLayoutGrayedOut(setNotificationsLayout, true);
            }
            targetTypeIndex = which;
            String selectedType = targetTypes.get(which);
//            targetEditor.putString(currentPackage, selectedType);
//            targetEditor.commit();
            targetTypeTextView.setText(selectedType);
            final Handler handler = new Handler();
            handler.postDelayed(dialog::dismiss, 100);
        });
        builder.create();
        builder.show();
    }

    public void onSetNotificationsClicked(View view) {
        boolean[] selectedItems = {false, false, false, false, false, false};
        for (int i : selectedNotifications)
            selectedItems[i] = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(R.string.select_notification_type)
                .setMultiChoiceItems(R.array.notification_types, selectedItems,
                        (dialog, which, isChecked) -> {
                            if (isChecked) {
                                selectedNotifications.add(which);
                            } else if (selectedNotifications.contains(which)) {
                                selectedNotifications.remove(Integer.valueOf(which));
                            }
                            setNotificationsText();
                        })
                .setPositiveButton(R.string.set, (dialog, id) -> {

                });
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        showToast(selectedNotifications.toString());
//                    }
//                });

        builder.create();
        builder.show();
    }

    public void onSetTargetClicked(View view) {
        final Dialog dialog = new Dialog(this);
        View setTargetDialog = getLayoutInflater().inflate(R.layout.set_app_usage_target, null);
        NumberPicker hourPicker = setTargetDialog.findViewById(R.id.hour_picker);
        NumberPicker minPicker = setTargetDialog.findViewById(R.id.min_picker);
        Button cancelButton = setTargetDialog.findViewById(R.id.cancelButton);
        Button setButton = setTargetDialog.findViewById(R.id.setButton);

        int maxHour = 23;
        if (targetTypeIndex == 0)
            maxHour = 24 * 7 - 1;
        int maxMin = 59;

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(maxHour);
        hourPicker.setValue(hourTarget);

        minPicker.setMinValue(0);
        minPicker.setMaxValue(maxMin);
        minPicker.setValue(minTarget);

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        setButton.setOnClickListener(v -> {
            int hour = hourPicker.getValue();
            int min = minPicker.getValue();
            setUsageTarget(hour, min);
            dialog.dismiss();
        });

        dialog.setTitle("Set target");
        dialog.setContentView(setTargetDialog);
        dialog.show();
    }

    public void onDailyCalendarSelected(View view) {
//        showToast("onDailyCalendarSelected");
        calendarMode = CALENDAR_DAILY;
        usageCollectionTime = currentGraphDate;
        usageData = AppUsageDataController.getDailyUsageDataInHourlyList(usageCollectionTime, currentPackage, this);
        updateGraphData();
    }

    public void onWeeklyCalendarSelected(View view) {
//        showToast("onWeeklyCalendarSelected");
        calendarMode = CALENDAR_WEEKLY;
        usageCollectionTime = ImportantStuffs.getRecentWeekFromTime(currentGraphDate);
        usageData = AppUsageDataController.getWeeklyUsageDataInDailyList(usageCollectionTime, currentPackage, this);
        updateGraphData();
    }

    public void onPickDateClicked(View view) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(usageCollectionTime);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.DatePicker,
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
        if(calendarMode == CALENDAR_DAILY)
            onDailyCalendarSelected(null);
        else
            onWeeklyCalendarSelected(null);
//        testStuffs();
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int message) {
        showToast(Integer.toString(message));
    }

    private void showToast(String... messages) {
        String fullMessage = "";
        for (String message : messages) {
            fullMessage += message + " ";
        }
        showToast(fullMessage);
    }

    private void showToast(int... messages) {
        String fullMessage = "";
        for (int message : messages) {
            fullMessage += message + " ";
        }
        showToast(fullMessage);
    }
}