package com.financetracker.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.financetracker.data.db.entity.Category;
import java.util.List;

@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Category category);

    @Update
    void update(Category category);

    @Query("UPDATE categories SET deleted = 1, updatedAt = :updatedAt WHERE uuid = :uuid")
    void softDelete(String uuid, long updatedAt);

    @Query("SELECT * FROM categories WHERE deleted = 0 ORDER BY name ASC")
    LiveData<List<Category>> getAllActive();

    @Query("SELECT * FROM categories WHERE deleted = 0 ORDER BY name ASC")
    List<Category> getAllActiveSync();

    @Query("SELECT * FROM categories WHERE deleted = 0 AND type = :type ORDER BY name ASC")
    LiveData<List<Category>> getByType(String type);

    @Query("SELECT * FROM categories WHERE uuid = :uuid LIMIT 1")
    Category getById(String uuid);

    @Query("SELECT * FROM categories WHERE updatedAt > :since")
    List<Category> getModifiedSince(long since);

    @Query("SELECT COUNT(*) FROM categories WHERE deleted = 0")
    int getCount();
}
