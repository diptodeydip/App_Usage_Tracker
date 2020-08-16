package com.example.app_usage_tracker;

import android.content.Context;
import android.media.MediaPlayer;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TestWorker extends Worker {
    private Context context;
    public static final String TAG = "ahtrap";

    public TestWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_NOTIFICATION_URI);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
        return null;
    }

//        Constraints constraint = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
//        OneTimeWorkRequest testWorkRequest = new OneTimeWorkRequest.Builder(TestWorker.class).setConstraints(constraint).build();
//        WorkManager.getInstance(this).enqueueUniqueWork("test work", ExistingWorkPolicy.KEEP, testWorkRequest);
}
