package com.financetracker.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.financetracker.data.db.AppDatabase;
import com.financetracker.data.db.dao.TransactionDao;
import com.financetracker.data.db.dao.SyncLogDao;
import com.financetracker.data.db.entity.Transaction;
import com.financetracker.data.db.entity.SyncLog;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {

    private final TransactionDao transactionDao;
    private final SyncLogDao syncLogDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TransactionRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.transactionDao = db.transactionDao();
        this.syncLogDao = db.syncLogDao();
    }

    public void insert(Transaction transaction, Runnable onComplete) {
        executor.execute(() -> {
            transactionDao.insert(transaction);
            logSync(transaction.uuid, "transaction", "INSERT");
            if (onComplete != null) onComplete.run();
        });
    }

    public void update(Transaction transaction, Runnable onComplete) {
        executor.execute(() -> {
            transaction.updatedAt = System.currentTimeMillis();
            transactionDao.update(transaction);
            logSync(transaction.uuid, "transaction", "UPDATE");
            if (onComplete != null) onComplete.run();
        });
    }

    public void delete(String uuid, Runnable onComplete) {
        executor.execute(() -> {
            transactionDao.softDelete(uuid, System.currentTimeMillis());
            logSync(uuid, "transaction", "DELETE");
            if (onComplete != null) onComplete.run();
        });
    }

    public LiveData<List<Transaction>> getAllActive() {
        return transactionDao.getAllActive();
    }

    public LiveData<List<Transaction>> getRecent(int limit) {
        return transactionDao.getRecent(limit);
    }

    public LiveData<List<Transaction>> getByAccount(String accountId) {
        return transactionDao.getByAccount(accountId);
    }

    public LiveData<List<Transaction>> getByDateRange(long start, long end) {
        return transactionDao.getByDateRange(start, end);
    }

    public LiveData<List<Transaction>> search(String query) {
        return transactionDao.search("%" + query + "%");
    }

    public Transaction getById(String uuid) {
        return transactionDao.getById(uuid);
    }

    public List<Transaction> getModifiedSince(long since) {
        return transactionDao.getModifiedSince(since);
    }

    public double getTotalIncome(long start, long end) {
        return transactionDao.getTotalIncome(start, end);
    }

    public double getTotalExpense(long start, long end) {
        return transactionDao.getTotalExpense(start, end);
    }

    public double getTotalTransfer(long start, long end) {
        return transactionDao.getTotalTransfer(start, end);
    }

    public LiveData<Double> getTotalTransferLive(long start, long end) {
        return transactionDao.getTotalTransferLive(start, end);
    }

    public LiveData<List<Transaction>> getRecentTransfers(int limit) {
        return transactionDao.getRecentTransfers(limit);
    }

    public LiveData<Double> getTotalIncomeLive(long start, long end) {
        return transactionDao.getTotalIncomeLive(start, end);
    }

    public LiveData<Double> getTotalExpenseLive(long start, long end) {
        return transactionDao.getTotalExpenseLive(start, end);
    }

    public void upsertWithConflictResolution(Transaction incoming) {
        executor.execute(() -> {
            // latest updatedAt wins
            transactionDao.insert(incoming);
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
