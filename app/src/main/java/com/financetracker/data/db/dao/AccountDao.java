package com.financetracker.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.financetracker.data.db.entity.Account;
import java.util.List;

@Dao
public interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Account account);

    @Update
    void update(Account account);

    @Query("UPDATE accounts SET deleted = 1, updatedAt = :updatedAt WHERE uuid = :uuid")
    void softDelete(String uuid, long updatedAt);

    @Query("SELECT * FROM accounts WHERE deleted = 0 ORDER BY name ASC")
    LiveData<List<Account>> getAllActive();

    @Query("SELECT * FROM accounts WHERE deleted = 0 ORDER BY name ASC")
    List<Account> getAllActiveSync();

    @Query("SELECT * FROM accounts WHERE uuid = :uuid LIMIT 1")
    Account getById(String uuid);

    @Query("SELECT * FROM accounts WHERE accountNumberLast4 = :last4 AND deleted = 0 LIMIT 1")
    Account getByAccountNumber(String last4);

    @Query("SELECT * FROM accounts WHERE updatedAt > :since")
    List<Account> getModifiedSince(long since);

    @Query("SELECT COUNT(*) FROM accounts WHERE deleted = 0")
    int getCount();

    @Query("""
        SELECT a.*, 
               COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as totalIncome,
               COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as totalExpense
        FROM accounts a
        LEFT JOIN transactions t ON a.uuid = t.accountId AND t.deleted = 0
        WHERE a.deleted = 0
        GROUP BY a.uuid
        ORDER BY a.name ASC
    """)
    LiveData<List<com.financetracker.data.db.entity.AccountWithBalance>> getAllActiveWithBalance();
}
