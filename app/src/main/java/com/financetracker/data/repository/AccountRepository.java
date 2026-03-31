package com.financetracker.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.financetracker.data.db.AppDatabase;
import com.financetracker.data.db.dao.AccountDao;
import com.financetracker.data.db.dao.SyncLogDao;
import com.financetracker.data.db.entity.Account;
import com.financetracker.data.db.entity.SyncLog;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountRepository {

    private final AccountDao accountDao;
    private final SyncLogDao syncLogDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AccountRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.accountDao = db.accountDao();
        this.syncLogDao = db.syncLogDao();
    }

    public void insert(Account account, Runnable onComplete) {
        executor.execute(() -> {
            accountDao.insert(account);
            logSync(account.uuid, "account", "INSERT");
            if (onComplete != null) onComplete.run();
        });
    }

    public void update(Account account, Runnable onComplete) {
        executor.execute(() -> {
            account.updatedAt = System.currentTimeMillis();
            accountDao.update(account);
            logSync(account.uuid, "account", "UPDATE");
            if (onComplete != null) onComplete.run();
        });
    }

    public void delete(String uuid, Runnable onComplete) {
        executor.execute(() -> {
            accountDao.softDelete(uuid, System.currentTimeMillis());
            logSync(uuid, "account", "DELETE");
            if (onComplete != null) onComplete.run();
        });
    }

    public LiveData<List<Account>> getAllActive() {
        return accountDao.getAllActive();
    }

    public LiveData<List<Account>> getAllActiveWithBalance() {
        return accountDao.getAllActiveWithBalance();
    }

    public List<Account> getAllActiveSync() {
        return accountDao.getAllActiveSync();
    }

    public Account getById(String uuid) {
        return accountDao.getById(uuid);
    }

    public Account findByAccountNumber(String last4Digits) {
        return accountDao.getByAccountNumber(last4Digits);
    }

    public List<Account> getModifiedSince(long since) {
        return accountDao.getModifiedSince(since);
    }

    public void upsertWithConflictResolution(Account incoming) {
        executor.execute(() -> {
            Account existing = accountDao.getById(incoming.uuid);
            if (existing == null || incoming.updatedAt > existing.updatedAt) {
                accountDao.insert(incoming);
            }
        });
    }

    private void logSync(String entityId, String entityType, String action) {
        SyncLog log = new SyncLog();
        log.uuid = UUID.randomUUID().toString();
        log.entityId = entityId;
        log.entityType = entityType;
        log.action = action;
        log.synced = false;
        log.createdAt = System.currentTimeMillis();
        syncLogDao.insert(log);
    }
}
