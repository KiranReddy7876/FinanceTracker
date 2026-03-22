package com.financetracker.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.financetracker.data.db.AppDatabase;
import com.financetracker.data.db.dao.CategoryDao;
import com.financetracker.data.db.dao.SyncLogDao;
import com.financetracker.data.db.entity.Category;
import com.financetracker.data.db.entity.SyncLog;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {

    private final CategoryDao categoryDao;
    private final SyncLogDao syncLogDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CategoryRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.categoryDao = db.categoryDao();
        this.syncLogDao = db.syncLogDao();
    }

    public void insert(Category category, Runnable onComplete) {
        executor.execute(() -> {
            categoryDao.insert(category);
            logSync(category.uuid, "category", "INSERT");
            if (onComplete != null) onComplete.run();
        });
    }

    public void update(Category category, Runnable onComplete) {
        executor.execute(() -> {
            category.updatedAt = System.currentTimeMillis();
            categoryDao.update(category);
            logSync(category.uuid, "category", "UPDATE");
            if (onComplete != null) onComplete.run();
        });
    }

    public void delete(String uuid, Runnable onComplete) {
        executor.execute(() -> {
            categoryDao.softDelete(uuid, System.currentTimeMillis());
            logSync(uuid, "category", "DELETE");
            if (onComplete != null) onComplete.run();
        });
    }

    public LiveData<List<Category>> getAllActive() {
        return categoryDao.getAllActive();
    }

    public List<Category> getAllActiveSync() {
        return categoryDao.getAllActiveSync();
    }

    public LiveData<List<Category>> getByType(String type) {
        return categoryDao.getByType(type);
    }

    public List<Category> getModifiedSince(long since) {
        return categoryDao.getModifiedSince(since);
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
