package com.financetracker.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.financetracker.data.db.AppDatabase;
import com.financetracker.data.db.dao.TransactionDao;
import com.financetracker.data.db.dao.AccountDao;
import com.financetracker.data.db.dao.SyncLogDao;
import com.financetracker.data.db.entity.Transaction;
import com.financetracker.data.db.entity.SyncLog;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {

    private final TransactionDao transactionDao;
    private final AccountDao accountDao;
    private final SyncLogDao syncLogDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "TransactionRepository";

    public TransactionRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.transactionDao = db.transactionDao();
        this.accountDao = db.accountDao();
        this.syncLogDao = db.syncLogDao();
    }

    public void insert(Transaction transaction, Runnable onComplete) {
        executor.execute(() -> {
            Log.d(TAG, "INSERT: Creating transaction type=" + transaction.type + ", amount=" + transaction.amount + ", accountId=" + transaction.accountId);
            transactionDao.insert(transaction);
            Log.d(TAG, "INSERT: Transaction inserted, now updating balances...");
            updateAccountBalances(transaction, true); // true = adding transaction
            Log.d(TAG, "INSERT: Balance updated");
            logSync(transaction.uuid, "transaction", "INSERT");
            if (onComplete != null) onComplete.run();
        });
    }

    public void update(Transaction transaction, Runnable onComplete) {
        executor.execute(() -> {
            // Get the old transaction to reverse its balance impact
            Transaction oldTransaction = transactionDao.getById(transaction.uuid);
            
            transaction.updatedAt = System.currentTimeMillis();
            transactionDao.update(transaction);
            
            // First reverse the old transaction's balance impact
            if (oldTransaction != null) {
                updateAccountBalances(oldTransaction, false); // false = removing transaction
            }
            
            // Then apply the new transaction's balance impact
            updateAccountBalances(transaction, true); // true = adding transaction
            
            logSync(transaction.uuid, "transaction", "UPDATE");
            if (onComplete != null) onComplete.run();
        });
    }

    public void delete(String uuid, Runnable onComplete) {
        executor.execute(() -> {
            // Get the transaction before deleting to reverse its balance impact
            Transaction transaction = transactionDao.getById(uuid);
            
            transactionDao.softDelete(uuid, System.currentTimeMillis());
            
            // Reverse the transaction's balance impact
            if (transaction != null) {
                updateAccountBalances(transaction, false); // false = removing transaction
            }
            
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

    /**
     * Update account balances based on transaction type
     * @param transaction The transaction to process
     * @param isAdding true if adding transaction impact, false if removing
     */
    private void updateAccountBalances(Transaction transaction, boolean isAdding) {
        long now = System.currentTimeMillis();
        double amount = isAdding ? transaction.amount : -transaction.amount;
        
        Log.d(TAG, "UPDATE_BALANCE: Type=" + transaction.type + ", Amount=" + transaction.amount + ", IsAdding=" + isAdding + ", FinalAmount=" + amount);

        if ("INCOME".equals(transaction.type)) {
            // Income increases the balance of the account
            Log.d(TAG, "UPDATE_BALANCE: INCOME - Adding " + amount + " to account " + transaction.accountId);
            accountDao.updateBalance(transaction.accountId, amount, now);
        } else if ("EXPENSE".equals(transaction.type)) {
            // Expense decreases the balance of the account
            Log.d(TAG, "UPDATE_BALANCE: EXPENSE - Subtracting " + amount + " from account " + transaction.accountId);
            accountDao.updateBalance(transaction.accountId, -amount, now);
        } else if ("TRANSFER".equals(transaction.type)) {
            // Transfer: decrease from source account, increase to destination account
            Log.d(TAG, "UPDATE_BALANCE: TRANSFER - From account " + transaction.accountId + " (debit " + amount + "), To account " + transaction.transferToAccountId + " (credit " + amount + ")");
            accountDao.updateBalance(transaction.accountId, -amount, now);
            if (transaction.transferToAccountId != null) {
                accountDao.updateBalance(transaction.transferToAccountId, amount, now);
                Log.d(TAG, "UPDATE_BALANCE: TRANSFER completed");
            } else {
                Log.d(TAG, "UPDATE_BALANCE: TRANSFER ERROR - transferToAccountId is null!");
            }
        }
    }
}
