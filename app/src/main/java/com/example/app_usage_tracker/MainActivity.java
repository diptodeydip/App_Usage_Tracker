package com.example.app_usage_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.judemanutd.autostarter.AutoStartPermissionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    TextInputEditText regiNumInput, cgpaInput;
    TextInputLayout regiLayout, cgpaLayout;
    RadioGroup genderRadioGroup;

//    private static final String TAG = "temp";
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    public static final String SHARED_PREFERENCE = "UserInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testThings();

        sharedPreference = getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE);
        editor = sharedPreference.edit();

        if(checkIfUserRegistered() == true){
            if(initializeJsonIfNot() == false)
                Toast.makeText(this, "Json initialization failed. App won't work properly.", Toast.LENGTH_SHORT).show();
            else
                AppsDataController.startAlarm(this, 500);

            Intent intent = new Intent(this, AppList.class);
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

        if (ImportantStuffs.isUsagePermissionEnabled(this) == true)
            return;
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        Toast.makeText(this, R.string.permission_message, Toast.LENGTH_LONG).show();
    }

    private void testThings(){

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
        else if(registrationNumber.charAt(0) != '2' || registrationNumber.charAt(1) != '0' || registrationNumber.charAt(2) != '1' )
            regiLayout.setError("Invalid registration number");
        else
            regiLayout.setErrorEnabled(false);

        if(cg < 1 || cg > 4)
            cgpaLayout.setError("Invalid cgpa");
        else
            cgpaLayout.setErrorEnabled(false);


        if(!regiLayout.isErrorEnabled() && !cgpaLayout.isErrorEnabled()){
            RadioButton genderButton = findViewById(genderRadioGroup.getCheckedRadioButtonId());
            saveData(registrationNumber, cgpa, genderButton.getText().toString());
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


    private void saveData(String registrationNumber, String cgpa, String gender){
        editor.putString("regNo", registrationNumber);
        editor.putString("cg", cgpa);
        editor.putString("gender", gender);
        editor.commit();
        if(initializeJsonIfNot() == false)
            Toast.makeText(this, "Json initialization failed. App won't work properly.", Toast.LENGTH_SHORT).show();
        else
            AppsDataController.startAlarm(this, 500);

        Intent intent = new Intent(this, AppList.class);
        startActivity(intent);
        finish();
    }

    private boolean checkIfUserRegistered(){
        if(sharedPreference.getString("regNo", "").equals(""))
            return false;
        return true;
    }

    private boolean initializeJsonIfNot(){
        String info = ImportantStuffs.getStringFromJsonObjectPath("info.json", this);
        if(info == ""){
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
            } catch (JSONException e) {
                ImportantStuffs.showErrorLog("Checkpoint can't be initialized");
                return false;
            }
            if(!ImportantStuffs.saveFileLocally("info.json", jsonInfo.toString(), this)){
                ImportantStuffs.showErrorLog("Checkpoint can't be initialized");
                return false;
            }
        }
        return true;
    }
}