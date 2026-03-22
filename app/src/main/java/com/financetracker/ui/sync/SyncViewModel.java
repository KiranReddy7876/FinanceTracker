package com.financetracker.ui.sync;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.*;
import androidx.work.*;
import com.financetracker.service.drive.DriveSyncWorker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SyncViewModel extends AndroidViewModel {

    private final MutableLiveData<String> syncStatus = new MutableLiveData<>("Idle");
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public LiveData<String> getSyncStatus() { return syncStatus; }
    public LiveData<Boolean> getIsSyncing() { return isSyncing; }

    public SyncViewModel(Application application) {
        super(application);
    }

    public String getLastSyncTime() {
        long ts = DriveSyncWorker.getLastSyncTime(getApplication());
        if (ts == 0) return "Never synced";
        return new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(ts));
    }

    public void triggerManualSync() {
        isSyncing.setValue(true);
        syncStatus.setValue("Syncing…");

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DriveSyncWorker.class)
            .setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .build();

        WorkManager wm = WorkManager.getInstance(getApplication());
        wm.enqueue(request);

        wm.getWorkInfoByIdLiveData(request.getId()).observeForever(info -> {
            if (info == null) return;
            switch (info.getState()) {
                case SUCCEEDED:
                    syncStatus.postValue("Sync complete");
                    isSyncing.postValue(false);
                    break;
                case FAILED:
                    syncStatus.postValue("Sync failed — check connection");
                    isSyncing.postValue(false);
                    break;
                case RUNNING:
                    syncStatus.postValue("Syncing…");
                    break;
                default:
                    break;
            }
        });
    }

    public void saveGoogleAccount(String accountName) {
        DriveSyncWorker.saveAccountName(getApplication(), accountName);
    }
}
