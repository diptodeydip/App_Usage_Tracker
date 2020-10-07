package com.ahtrapotpid.appusagetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "temp";
    TextInputEditText regiNumInput, cgpaInput;
    TextInputLayout regiLayout, cgpaLayout;
    RadioGroup genderRadioGroup;

    static SharedPreferences sharedPreference;
    static SharedPreferences.Editor editor;
    public static final String SHARED_PREFERENCE = "UserInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testThings();

        sharedPreference = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
        editor = sharedPreference.edit(); 

        if(checkIfUserRegistered() == true){

            if (initializeJsonIfNot() == false) {
                Toast.makeText(this, "Json initialization failed. App won't work properly.", Toast.LENGTH_SHORT).show();
                ImportantStuffs.showErrorLog("Json initialization failed. App won't work properly.");
            }

            Intent intent = new Intent(this, AppListTabbed.class);
            startActivity(intent);
            finish();
            return;
        }

        regiLayout = findViewById(R.id.registration_number);
        cgpaLayout = findViewById(R.id.recent_semester_cgpa);
        regiNumInput = findViewById(R.id.registration_num_input);
        cgpaInput = findViewById(R.id.cgpa_input);
        genderRadioGroup = findViewById(R.id.gender_radio_group);

        cgpaInput.setOnFocusChangeListener((v, hasFocus) -> onCGInputFocusChanged(hasFocus));
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean autoStartEnabled = sharedPreference.getBoolean("autoStart", false);
        boolean usagePermissionEnabled = ImportantStuffs.isUsagePermissionEnabled(this);

        if(!usagePermissionEnabled)
            showUsagePermissionDialog();
        else if(!autoStartEnabled)
            showAutoStartPermissionDialog(editor);
    }

    private void testThings() {
//        long startTime = ImportantStuffs.getDayStartingHour();
//        long endTime = ImportantStuffs.getCurrentTime();
//        JSONObject infoJson = ImportantStuffs.getJsonObject("info.json", this);
//        AppsDataController.testSetAutoTargetForAllApps(startTime, endTime, infoJson, this);
//        AppsDataController.testResetAutoTargetForAllApps(infoJson, this);

//        ImportantStuffs.displayUpdateNotification(this);
//        ImportantStuffs.displayUpdateNotification(this);
//        ImportantStuffs.displayUpdateNotification(this);

//        String versionName = "";
//        try {
//            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        Toast.makeText(this, versionName, Toast.LENGTH_SHORT).show();
    }


    public void onRegisterClicked(View view) {
        String registrationNumber = regiNumInput.getText().toString();
        String cgpa = cgpaInput.getText().toString();
        float cg = 0;
        try {
            cg = Float.valueOf(cgpa);
        } catch (Exception e) {}

        if(registrationNumber.equals(""))
            regiLayout.setError("Registration number can't be empty");
        else if(registrationNumber.length() != 10)
            regiLayout.setError("Invalid registration number length");
        else
            regiLayout.setErrorEnabled(false);

        if (cg < 1 || cg > 4)
            cgpaLayout.setError("Invalid cgpa");
        else
            cgpaLayout.setErrorEnabled(false);


        if (!regiLayout.isErrorEnabled() && !cgpaLayout.isErrorEnabled()) {
            RadioButton genderButton = findViewById(genderRadioGroup.getCheckedRadioButtonId());
            saveUserDataForTheFirstTime(registrationNumber, cgpa, genderButton.getText().toString());
            Intent intent = new Intent(this, AppListTabbed.class);
            intent.putExtra("firstTime", true);
            startActivity(intent);
            finish();
        }


    }

    public void onMaleClicked(View view) {
    }

    public void onFemaleClicked(View view) {
    }

    public void onCGInputFocusChanged(boolean hasFocus) {
        if(!hasFocus)
            return;
        Toast.makeText(this, R.string.cgpa_message, Toast.LENGTH_LONG).show();
    }


    private void showUsagePermissionDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.DialogTheme);
        dialog.setTitle("Usage access");
        dialog.setMessage(R.string.usage_permission_message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("set", (dialog12, which) -> MainActivity.this.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        dialog.create();
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if( keyCode == KeyEvent.KEYCODE_BACK ){
                dialog1.cancel();
                MainActivity.this.finish();
                return true;
            }
            return false;
        });
        dialog.show();
    }

    private void showAutoStartPermissionDialog(SharedPreferences.Editor editor){
        AutoStartHelper.getInstance().getAutoStartPermission(this,  editor);
    }

    private void saveUserDataForTheFirstTime(String registrationNumber, String cgpa, String gender){
        editor.putString("regNo", registrationNumber);
        editor.putString("cg", cgpa);
        editor.putString("gender", gender);
        editor.commit();

        if(initializeJsonIfNot() == false)
            Toast.makeText(this, "Json initialization failed. App won't work properly.", Toast.LENGTH_SHORT).show();
    }

    private boolean checkIfUserRegistered() {
        String regNo = sharedPreference.getString("regNo", "");
        if (regNo.equals(""))
            return false;
        return true;
    }

    private boolean initializeJsonIfNot(){
        String info = ImportantStuffs.getStringFromJsonObjectPath("info.json", this);
        if(info == ""){
            Log.d("flag", "Initializing info.json");
            ImportantStuffs.showLog("No checkpoint data found. Creating new checkpoint---");
            Calendar calendar = Calendar.getInstance();
            JSONObject jsonInfo = new JSONObject();

            calendar.set(Calendar.MILLISECOND,0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            calendar.add(Calendar.DAY_OF_WEEK,1);
            calendar.add(Calendar.WEEK_OF_MONTH,-1);
            calendar.set(Calendar.AM_PM, Calendar.AM);

            Long time = calendar.getTimeInMillis();
            try {
                jsonInfo.put("checkpoint", time);
                jsonInfo.put("appsInfo", new JSONObject());
                jsonInfo.put("appsInstallationInfo", new JSONObject());
                jsonInfo.put("weekNumber", 1);
                // change to get day starting hour
                jsonInfo.put("weekTime", System.currentTimeMillis());
                editor.putLong("weekOneStartTime",System.currentTimeMillis()).apply();
            } catch (JSONException e) {
                e.printStackTrace();
                ImportantStuffs.showErrorLog("Checkpoint can't be initialized");
                return false;
            }
            if (!ImportantStuffs.saveFileLocally("info.json", jsonInfo.toString(), this)) {
                ImportantStuffs.showErrorLog("Checkpoint can't be initialized");
                return false;
            }
            ImportantStuffs.displayNotification(this, getResources().getString(R.string.week_1_notice));
            ImportantStuffs.showLog("info.json initialized.");
            Log.d("flag", "info.json initialized");
        }
        return true;
    }
}