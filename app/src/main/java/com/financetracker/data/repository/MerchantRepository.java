package com.financetracker.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.financetracker.data.db.AppDatabase;
import com.financetracker.data.db.dao.MerchantDao;
import com.financetracker.data.db.dao.SyncLogDao;
import com.financetracker.data.db.entity.Merchant;
import com.financetracker.data.db.entity.SyncLog;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MerchantRepository {

    private final MerchantDao merchantDao;
    private final SyncLogDao syncLogDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MerchantRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.merchantDao = db.merchantDao();
        this.syncLogDao = db.syncLogDao();
    }

    public void insert(Merchant merchant, Runnable onComplete) {
        executor.execute(() -> {
            merchantDao.insert(merchant);
            logSync(merchant.uuid, "merchant", "INSERT");
            if (onComplete != null) onComplete.run();
        });
    }

    public void update(Merchant merchant, Runnable onComplete) {
        executor.execute(() -> {
            merchant.updatedAt = System.currentTimeMillis();
            merchantDao.update(merchant);
            logSync(merchant.uuid, "merchant", "UPDATE");
            if (onComplete != null) onComplete.run();
        });
    }

    public void delete(String uuid, Runnable onComplete) {
        executor.execute(() -> {
            merchantDao.softDelete(uuid, System.currentTimeMillis());
            logSync(uuid, "merchant", "DELETE");
            if (onComplete != null) onComplete.run();
        });
    }

    public LiveData<List<Merchant>> getAllActive() {
        return merchantDao.getAllActive();
    }

    public List<Merchant> getAllActiveSync() {
        return merchantDao.getAllActiveSync();
    }

    public Merchant findByName(String name) {
        return merchantDao.findByName(name);
    }

    /**
     * Save or update merchant→category mapping synchronously (call from background thread).
     * If merchant with same name exists update its categoryId, otherwise create a new one.
     */
    public void saveMerchantCategorySync(String merchantName, String categoryId) {
        if (merchantName == null || merchantName.trim().isEmpty()) return;
        Merchant existing = merchantDao.findByName(merchantName.trim());
        if (existing != null) {
            existing.categoryId = categoryId;
            existing.updatedAt = System.currentTimeMillis();
            merchantDao.update(existing);
            logSync(existing.uuid, "merchant", "UPDATE");
        } else {
            Merchant m = new Merchant(UUID.randomUUID().toString(), merchantName.trim(), categoryId);
            merchantDao.insert(m);
            logSync(m.uuid, "merchant", "INSERT");
        }
    }

    public List<Merchant> getModifiedSince(long since) {
        return merchantDao.getModifiedSince(since);
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
