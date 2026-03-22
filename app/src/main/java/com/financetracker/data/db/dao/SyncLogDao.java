package com.financetracker.data.db.dao;

import androidx.room.*;
import com.financetracker.data.db.entity.SyncLog;
import java.util.List;

@Dao
public interface SyncLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SyncLog syncLog);

    @Query("SELECT * FROM sync_log WHERE synced = 0 ORDER BY createdAt ASC")
    List<SyncLog> getUnsynced();

    @Query("UPDATE sync_log SET synced = 1 WHERE uuid = :uuid")
    void markSynced(String uuid);

    @Query("DELETE FROM sync_log WHERE synced = 1 AND createdAt < :before")
    void pruneOldSynced(long before);
}
