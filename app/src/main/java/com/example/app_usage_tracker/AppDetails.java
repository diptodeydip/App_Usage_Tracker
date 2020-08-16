package com.example.app_usage_tracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.app.DatePickerDialog;
import android.app.Dialog;
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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class AppDetails extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    BarChart chart;
    TextView targetTypeTextView, targetTextView, notificationTypesTextView, selectedAppTextView;
    ConstraintLayout selectAppLayout, setTargetLayout, setNotificationsLayout;
    int targetTypeIndex, hourTarget, minTarget, selectedAppIndex;
    String[] targetTypes, notificationTypes, appList;
    ArrayList<Integer> selectedNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        testStuffs();
        initStuffs();
        createGraph();
    }

    private void testStuffs(){

    }

    private void initStuffs() {
        selectAppLayout = findViewById(R.id.app_list);
        selectedAppTextView = findViewById(R.id.selected_app);
        appList = getResources().getStringArray(R.array.planets_array);
        selectedAppIndex = 0;
        selectedAppTextView.setText(appList[selectedAppIndex]);

//        spinner = findViewById(R.id.app_list);
        chart = findViewById(R.id.usage_graph);

        targetTypes = getResources().getStringArray(R.array.target_types);
        targetTypeTextView = findViewById(R.id.target_type_text);

        setTargetLayout = findViewById(R.id.set_target);
        targetTextView = findViewById(R.id.target_text);
        targetTypeIndex = 2;
        hourTarget = 2;
        minTarget = 0;
        targetTypeTextView.setText(targetTypes[targetTypeIndex]);
        setTargetText(hourTarget, minTarget);

        notificationTypes = getResources().getStringArray(R.array.notification_types);
        setNotificationsLayout = findViewById(R.id.set_notifications_type);
        notificationTypesTextView = findViewById(R.id.notifications_text);
        selectedNotifications = new ArrayList();
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
                String selectedNotification = notificationTypes[selectedNotifications.get(0)];
                notificationTypesTextView.setText(selectedNotification);
                break;
            default:
                notificationTypesTextView.setText("Multiple");
        }
    }

    private void setTargetText(int hour, int min) {
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
//        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(60);
        leftAxis.setTextColor(Color.WHITE);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMaximum(23);
        xAxis.setTextColor(Color.WHITE);

        ArrayList<BarEntry> dataValues = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 24; i++) {
            dataValues.add(new BarEntry(i, random.nextInt(61)));
        }
        BarDataSet barDataSet = new BarDataSet(dataValues, "");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setDrawValues(false);
        BarData barData = new BarData(barDataSet);

        chart.setData(barData);
        chart.invalidate();
        chart.animateY(1000, Easing.EaseOutCubic);

    }

    public void onSelectAppClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(R.string.demo).setSingleChoiceItems(R.array.planets_array, selectedAppIndex, (dialog, which) -> {
            selectedAppIndex = which;
            String selectedApp = appList[which];
            selectedAppTextView.setText(selectedApp);
            final Handler handler = new Handler();
            handler.postDelayed(dialog::dismiss, 100);
//            showToast(selectedApp);
        }).setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());
        builder.create();
        builder.show();
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
            String selectedType = targetTypes[which];
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
            setTargetText(hour, min);
            dialog.dismiss();
        });

        dialog.setTitle("Set target");
        dialog.setContentView(setTargetDialog);
        dialog.show();
    }

    public void onDailyCalendarSelected(View view) {
        showToast("onDailyCalendarSelected");
    }

    public void onWeeklyCalendarSelected(View view) {
        showToast("onWeeklyCalendarSelected");
    }

    public void onPickDateClicked(View view) {
        showDatePickerDialog();
    }

    public void onCompareDateClicked(View view) {
        showDatePickerDialog();
    }

    public void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.DialogTheme,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        showToast("onDateSet");
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