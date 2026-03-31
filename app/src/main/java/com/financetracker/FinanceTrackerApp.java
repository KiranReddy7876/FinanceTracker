package com.financetracker;

import android.app.Application;
import android.util.Log;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.*;
import com.financetracker.data.db.DatabaseInitializer;
import com.financetracker.service.drive.DriveSyncWorker;
import java.util.concurrent.TimeUnit;

public class FinanceTrackerApp extends Application {

    private static final String TAG = "FinanceTrackerApp";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "FinanceTrackerApp.onCreate() called");

        try {
            // Force light mode — app does not support dark theme
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            
            // Initialize database first
            DatabaseInitializer.initialize(this);
            
            Log.d(TAG, "SMS Receiver should be registered for: android.provider.Telephony.SMS_RECEIVED");
            
            try {
                scheduleSyncWorker();
            } catch (Exception e) {
                Log.e(TAG, "Failed to schedule sync worker", e);
                e.printStackTrace();
                // Continue app startup even if WorkManager fails
            }
        } catch (Exception e) {
            // Catch all errors during app initialization including system service errors
            Log.e(TAG, "ERROR during app initialization: " + e.getMessage(), e);
            e.printStackTrace();
            // App will still start - this prevents immediate crashes
        }
    }

    private void scheduleSyncWorker() {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
            DriveSyncWorker.class, 1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DriveSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        );
    }
}
