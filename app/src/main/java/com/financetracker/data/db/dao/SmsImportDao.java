package com.financetracker.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.financetracker.data.db.entity.SmsImport;
import java.util.List;

@Dao
public interface SmsImportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SmsImport smsImport);

    @Update
    void update(SmsImport smsImport);

    @Query("SELECT s.* FROM sms_import s " +
            "LEFT JOIN merchants m ON LOWER(TRIM(s.merchantName)) = LOWER(TRIM(m.name)) AND m.deleted = 0 " +
            "WHERE s.status = 'PENDING' " +
            "AND s.deleted = 0 " +
            "AND (s.merchantName IS NULL OR TRIM(s.merchantName) = '' OR m.uuid IS NULL OR m.categoryId IS NULL) " +
            "ORDER BY s.createdAt DESC")
    LiveData<List<SmsImport>> getPending();

    @Query("SELECT COUNT(*) FROM sms_import s " +
            "LEFT JOIN merchants m ON LOWER(TRIM(s.merchantName)) = LOWER(TRIM(m.name)) AND m.deleted = 0 " +
            "WHERE s.status = 'PENDING' " +
            "AND s.deleted = 0 " +
            "AND (s.merchantName IS NULL OR TRIM(s.merchantName) = '' OR m.uuid IS NULL OR m.categoryId IS NULL)")
    LiveData<Integer> getPendingCount();

    @Query("UPDATE sms_import SET status = :status, updatedAt = :updatedAt WHERE uuid = :uuid")
    void updateStatus(String uuid, String status, long updatedAt);

    @Query("UPDATE sms_import SET accountId = :accountId, categoryId = :categoryId, updatedAt = :updatedAt WHERE uuid = :uuid")
    void updateAccountAndCategory(String uuid, String accountId, String categoryId, long updatedAt);

    @Query("UPDATE sms_import SET merchantName = :merchantName, updatedAt = :updatedAt WHERE uuid = :uuid")
    void updateMerchant(String uuid, String merchantName, long updatedAt);


    @Query("SELECT * FROM sms_import WHERE uuid = :uuid LIMIT 1")
    SmsImport getById(String uuid);

    @Query("SELECT * FROM sms_import WHERE updatedAt > :since")
    List<SmsImport> getModifiedSince(long since);

    @Query("SELECT * FROM sms_import WHERE status = 'CONFIRMED' AND deleted = 0 ORDER BY createdAt DESC")
    List<SmsImport> getConfirmed();
}
