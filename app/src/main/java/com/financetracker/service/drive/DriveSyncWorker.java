package com.financetracker.service.drive;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;
import java.util.Collections;

public class DriveSyncWorker extends Worker {

    private static final String PREFS_NAME = "SyncPrefs";
    private static final String KEY_LAST_SYNC = "last_sync_timestamp";
    private static final String KEY_ACCOUNT_NAME = "google_account_name";

    public DriveSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String accountName = prefs.getString(KEY_ACCOUNT_NAME, null);
        if (accountName == null) return Result.success(); // not signed in yet

        long lastSync = prefs.getLong(KEY_LAST_SYNC, 0);

        try {
            GoogleAccountCredential credential = GoogleAccountCredential
                .usingOAuth2(context, Collections.singletonList(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccountName(accountName);

            DriveSyncService syncService = new DriveSyncService(context, credential);
            DriveSyncService.SyncResult result = syncService.sync(lastSync);

            prefs.edit().putLong(KEY_LAST_SYNC, result.timestamp).apply();
            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }

    public static void saveAccountName(Context context, String accountName) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_ACCOUNT_NAME, accountName).apply();
    }

    public static long getLastSyncTime(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_SYNC, 0);
    }
}
