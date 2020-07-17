package com.example.app_usage_tracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class StoreData extends Worker {


    public StoreData(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();

        saveToFirebase(data.getString("jsonString"));

        return Result.success();
    }


    void saveToFirebase(String jsonString){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("User1");

        Map<String, Object> userMap = new Gson().fromJson(jsonString, new TypeToken<HashMap<String, Object>>() {}.getType());
        myRef.setValue(userMap);

    }

}
