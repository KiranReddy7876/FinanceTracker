package com.financetracker.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.financetracker.data.db.entity.Merchant;
import java.util.List;

@Dao
public interface MerchantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Merchant merchant);

    @Update
    void update(Merchant merchant);

    @Query("UPDATE merchants SET deleted = 1, updatedAt = :updatedAt WHERE uuid = :uuid")
    void softDelete(String uuid, long updatedAt);

    @Query("SELECT * FROM merchants WHERE deleted = 0 ORDER BY name ASC")
    LiveData<List<Merchant>> getAllActive();

    @Query("SELECT * FROM merchants WHERE deleted = 0 ORDER BY name ASC")
    List<Merchant> getAllActiveSync();

    @Query("SELECT * FROM merchants WHERE uuid = :uuid LIMIT 1")
    Merchant getById(String uuid);

    @Query("SELECT * FROM merchants WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) AND deleted = 0 LIMIT 1")
    Merchant findByName(String name);

    @Query("SELECT * FROM merchants WHERE updatedAt > :since")
    List<Merchant> getModifiedSince(long since);

    @Query("SELECT COUNT(*) FROM merchants WHERE deleted = 0")
    int getCount();
}
