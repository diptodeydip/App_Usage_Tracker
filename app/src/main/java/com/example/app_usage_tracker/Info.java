package com.example.app_usage_tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class Info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        checkIfFirst();

        EditText name,email,age,regNo,cgpa;
        final String[] gender = {""};
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        cgpa = findViewById(R.id.cgpa);
        age = findViewById(R.id.age);
        regNo = findViewById(R.id.regNo);

        CheckBox male,female;
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);

        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gender[0] = "Male";
                female.setChecked(false);
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gender[0] = "Female";
                male.setChecked(false);
            }
        });

        Button done = findViewById(R.id.done);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(name.getText().toString())){
                    name.setError("fill up name");
                } else if(TextUtils.isEmpty(email.getText().toString())){
                    email.setError("fill up email");
                } else if(TextUtils.isEmpty(cgpa.getText().toString())){
                    cgpa.setError("fill up cgpa");
                } else if(TextUtils.isEmpty(regNo.getText().toString())){
                    regNo.setError("fill up regNo");
                } else if(TextUtils.isEmpty(age.getText().toString())){
                    age.setError("fill up age");
                }else if(gender[0].equals("")){
                    Toast.makeText(getApplicationContext(), "select gender", Toast.LENGTH_SHORT).show();
                } else{
                    JSONObject userInfo = new JSONObject();
                    try {
                        userInfo.put("Name" , name.getText().toString());
                        userInfo.put("Email" , email.getText().toString());
                        userInfo.put("Registration_No" , regNo.getText().toString());
                        userInfo.put("Cgpa" , cgpa.getText().toString());
                        userInfo.put("Age" , age.getText().toString());
                        userInfo.put("Gender" , gender[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    MyBroadcastReceiver.saveToPhone(userInfo.toString(),"userInfo.json" ,getApplicationContext());
                    startActivity(new Intent(Info.this,MainActivity.class));
                    finish();
                }

            }
        });

    }

    void checkIfFirst(){
        String jsonString =  MyBroadcastReceiver.readJSON("details.json",this);
        if(jsonString=="") {

            // JSONObject userDetails = new JSONObject();

            Calendar calendar = Calendar.getInstance();
            long installationTime = calendar.getTimeInMillis();

            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR, 0);
            calendar.add(Calendar.DAY_OF_WEEK,1);
            calendar.add(Calendar.WEEK_OF_MONTH,-1);
            calendar.set(Calendar.AM_PM, Calendar.AM);

            long Time = calendar.getTimeInMillis();



            try {
                JSONObject userDetails = new JSONObject();
                userDetails.put("InstallationTime" , installationTime);
                userDetails.put("checkPoint" , Time);
                MyBroadcastReceiver.saveToPhone(userDetails.toString(),"details.json" ,this);
            } catch (JSONException e) {
                Toast.makeText(this,"error",Toast.LENGTH_LONG).show();
            }
        }

        String jsonString1 =  MyBroadcastReceiver.readJSON("userInfo.json",this);
        if(jsonString1!="") {
            startActivity(new Intent(Info.this,MainActivity.class));
            finish();
        }
    }
}