package com.financetracker.ui.smsimport;

import android.app.Application;
import androidx.lifecycle.*;
import com.financetracker.data.db.entity.SmsImport;
import com.financetracker.data.db.entity.Account;
import com.financetracker.data.db.entity.Category;
import com.financetracker.data.repository.SmsImportRepository;
import com.financetracker.data.repository.AccountRepository;
import com.financetracker.data.repository.CategoryRepository;
import java.util.List;

public class SmsImportViewModel extends AndroidViewModel {

    private final SmsImportRepository smsImportRepo;
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;

    public final LiveData<List<SmsImport>> pendingSmsImports;
    public final LiveData<Integer> pendingCount;
    public final LiveData<List<Account>> accounts;
    public final LiveData<List<Category>> categories;

    public SmsImportViewModel(Application application) {
        super(application);
        smsImportRepo = new SmsImportRepository(application);
        accountRepo = new AccountRepository(application);
        categoryRepo = new CategoryRepository(application);

        pendingSmsImports = smsImportRepo.getPending();
        pendingCount = smsImportRepo.getPendingCount();
        accounts = accountRepo.getAllActive();
        categories = categoryRepo.getAllActive();
    }

    /**
     * Update the account and category for an SMS import
     * @param smsImportId ID of the SMS import
     * @param accountId Selected account ID
     * @param categoryId Selected category ID (can be null)
     */
    public void updateAccountAndCategory(String smsImportId, String accountId, String categoryId) {
        smsImportRepo.updateAccountAndCategory(smsImportId, accountId, categoryId);
    }

    /**
     * Confirm SMS import - mark as CONFIRMED and convert to transaction
     */
    public void confirmImport(String smsImportId) {
        smsImportRepo.confirm(smsImportId);
    }

    /**
     * Get categories that match a specific type (EXPENSE or INCOME)
     */
    public LiveData<List<Category>> getCategoriesByType(String type) {
        return categoryRepo.getByType(type);
    }

    /**
     * Ignore SMS import - mark as IGNORED
     */
    public void ignoreImport(String smsImportId) {
        smsImportRepo.ignore(smsImportId);
    }

    /**
     * Delete SMS import (soft delete)
     */
    public void deleteSmsImport(String smsImportId) {
        smsImportRepo.delete(smsImportId);
    }

    /**
     * Update the merchant name for an SMS import
     * @param smsImportId ID of the SMS import
     * @param merchantName New merchant name
     */
    public void updateMerchant(String smsImportId, String merchantName) {
        smsImportRepo.updateMerchant(smsImportId, merchantName);
    }

    /**
     * Update account/category and then confirm in sequence
     * Ensures update completes before confirmation
     */
    public void updateAndConfirmImport(String smsImportId, String accountId, String categoryId) {
        smsImportRepo.updateAccountAndCategoryThenConfirm(smsImportId, accountId, categoryId);
    }
}
