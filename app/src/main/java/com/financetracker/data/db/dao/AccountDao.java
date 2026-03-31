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

    // Get account first, update it, then save it back (Room will properly notify LiveData)
    @Transaction
    default void updateBalance(String accountId, double amount, long updatedAt) {
        Account account = getById(accountId);
        if (account != null) {
            account.currentBalance = account.currentBalance + amount;
            account.updatedAt = updatedAt;
            update(account);
        }
    }

    @Query("SELECT * FROM accounts WHERE deleted = 0 ORDER BY name ASC")
    LiveData<List<Account>> getAllActiveWithBalance();
}
