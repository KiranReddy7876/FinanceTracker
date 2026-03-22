package com.financetracker.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.financetracker.data.db.entity.Transaction;
import java.util.List;

@Dao
public interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Query("UPDATE transactions SET deleted = 1, updatedAt = :updatedAt WHERE uuid = :uuid")
    void softDelete(String uuid, long updatedAt);

    @Query("SELECT * FROM transactions WHERE deleted = 0 ORDER BY date DESC")
    LiveData<List<Transaction>> getAllActive();

    @Query("SELECT * FROM transactions WHERE deleted = 0 ORDER BY date DESC LIMIT :limit")
    LiveData<List<Transaction>> getRecent(int limit);

    @Query("SELECT * FROM transactions WHERE deleted = 0 AND accountId = :accountId ORDER BY date DESC")
    LiveData<List<Transaction>> getByAccount(String accountId);

    @Query("SELECT * FROM transactions WHERE deleted = 0 AND date BETWEEN :start AND :end ORDER BY date DESC")
    LiveData<List<Transaction>> getByDateRange(long start, long end);

    @Query("SELECT * FROM transactions WHERE deleted = 0 AND categoryId = :categoryId ORDER BY date DESC")
    LiveData<List<Transaction>> getByCategory(String categoryId);

    @Query("SELECT * FROM transactions WHERE deleted = 0 AND (note LIKE :q OR referenceId LIKE :q) ORDER BY date DESC")
    LiveData<List<Transaction>> search(String q);

    @Query("SELECT * FROM transactions WHERE updatedAt > :since")
    List<Transaction> getModifiedSince(long since);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE deleted = 0 AND type = 'INCOME' AND date BETWEEN :start AND :end")
    double getTotalIncome(long start, long end);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE deleted = 0 AND type = 'EXPENSE' AND date BETWEEN :start AND :end")
    double getTotalExpense(long start, long end);

    @Query("SELECT * FROM transactions WHERE uuid = :uuid AND deleted = 0")
    Transaction getById(String uuid);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE deleted = 0 AND type = 'INCOME' AND date BETWEEN :start AND :end")
    LiveData<Double> getTotalIncomeLive(long start, long end);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE deleted = 0 AND type = 'EXPENSE' AND date BETWEEN :start AND :end")
    LiveData<Double> getTotalExpenseLive(long start, long end);

    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE deleted = 0 AND type = 'EXPENSE' AND date BETWEEN :start AND :end GROUP BY categoryId ORDER BY total DESC")
    List<CategoryTotal> getCategoryTotals(long start, long end);

    static class CategoryTotal {
        public String categoryId;
        public double total;
    }
}
