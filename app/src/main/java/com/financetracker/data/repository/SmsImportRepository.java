package com.financetracker.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.financetracker.data.db.AppDatabase;
import com.financetracker.data.db.dao.SmsImportDao;
import com.financetracker.data.db.entity.SmsImport;
import com.financetracker.service.SmsImportConversionService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsImportRepository {

    private final SmsImportDao smsImportDao;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SmsImportRepository(Context context) {
        this.context = context;
        this.smsImportDao = AppDatabase.getInstance(context).smsImportDao();
    }

    public void insert(SmsImport smsImport) {
        executor.execute(() -> smsImportDao.insert(smsImport));
    }

    public LiveData<List<SmsImport>> getPending() {
        return smsImportDao.getPending();
    }

    public LiveData<Integer> getPendingCount() {
        return smsImportDao.getPendingCount();
    }

    public void confirm(String uuid) {
        executor.execute(() -> {
            smsImportDao.updateStatus(uuid, "CONFIRMED", System.currentTimeMillis());
            // Convert to transaction immediately after confirming
            SmsImport smsImport = smsImportDao.getById(uuid);
            if (smsImport != null) {
                SmsImportConversionService.convertToTransaction(context, smsImport);
            }
        });
    }

    public void confirmWithoutUserReview(String uuid) {
        // Auto-confirm without user interaction for SMS with matched accounts
        executor.execute(() -> {
            smsImportDao.updateStatus(uuid, "CONFIRMED", System.currentTimeMillis());
            // Convert to transaction immediately
            SmsImport smsImport = smsImportDao.getById(uuid);
            if (smsImport != null && smsImport.accountId != null) {
                SmsImportConversionService.convertToTransaction(context, smsImport);
            }
        });
    }

    public void ignore(String uuid) {
        executor.execute(() -> smsImportDao.updateStatus(uuid, "IGNORED", System.currentTimeMillis()));
    }

    public void delete(String uuid) {
        // Soft delete SMS import
        executor.execute(() -> {
            SmsImport smsImport = smsImportDao.getById(uuid);
            if (smsImport != null) {
                smsImport.deleted = true;
                smsImport.updatedAt = System.currentTimeMillis();
                smsImportDao.update(smsImport);
            }
        });
    }

    public void updateAccountAndCategory(String smsImportId, String accountId, String categoryId) {
        executor.execute(() -> {
            smsImportDao.updateAccountAndCategory(smsImportId, accountId, categoryId, System.currentTimeMillis());
        });
    }

    public void updateMerchant(String smsImportId, String merchantName) {
        executor.execute(() -> {
            smsImportDao.updateMerchant(smsImportId, merchantName, System.currentTimeMillis());
        });
    }

    public SmsImport getById(String uuid) {
        return smsImportDao.getById(uuid);
    }

    public List<SmsImport> getConfirmed() {
        return smsImportDao.getConfirmed();
    }

    public void updateAccountAndCategoryThenConfirm(String smsImportId, String accountId, String categoryId) {
        executor.execute(() -> {
            // First update account and category
            smsImportDao.updateAccountAndCategory(smsImportId, accountId, categoryId, System.currentTimeMillis());
            // Then confirm (which will convert to transaction with fresh data)
            smsImportDao.updateStatus(smsImportId, "CONFIRMED", System.currentTimeMillis());
            // Convert to transaction with fresh record
            SmsImport smsImport = smsImportDao.getById(smsImportId);
            if (smsImport != null) {
                SmsImportConversionService.convertToTransaction(context, smsImport);
            }
        });
    }

    /**
     * Update transfer SMS import with transfer-specific details and confirm
     * For SELF transfer: updates transferToAccountId
     * For FRIEND transfer: updates merchantName (friend name) and categoryId (for settle payment)
     */
    public void updateTransferAndConfirm(String smsImportId, String fromAccountId, String toAccountId,
                                        String friendName, String transferSubType, String settleCategoryId) {
        executor.execute(() -> {
            SmsImport smsImport = smsImportDao.getById(smsImportId);
            if (smsImport != null) {
                smsImport.accountId = fromAccountId;
                smsImport.transferToAccountId = toAccountId;
                smsImport.merchantName = friendName;  // Store friend name in merchantName field
                smsImport.categoryId = settleCategoryId;  // Store settle category if applicable
                smsImport.updatedAt = System.currentTimeMillis();
                smsImportDao.update(smsImport);
                
                // Confirm and convert to transaction
                smsImportDao.updateStatus(smsImportId, "CONFIRMED", System.currentTimeMillis());
                SmsImport updatedSmsImport = smsImportDao.getById(smsImportId);
                if (updatedSmsImport != null) {
                    SmsImportConversionService.convertToTransaction(context, updatedSmsImport);
                }
            }
        });
    }
}
