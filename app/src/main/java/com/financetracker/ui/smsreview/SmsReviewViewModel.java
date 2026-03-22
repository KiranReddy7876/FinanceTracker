package com.financetracker.ui.smsreview;

import android.app.Application;
import androidx.lifecycle.*;
import com.financetracker.data.db.entity.*;
import com.financetracker.data.repository.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsReviewViewModel extends AndroidViewModel {

    private final SmsImportRepository smsImportRepo;
    private final TransactionRepository transactionRepo;
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;
    private final MerchantRepository merchantRepo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public final LiveData<List<SmsImport>> pendingItems;
    public final LiveData<List<Account>> accounts;
    public final LiveData<List<Category>> categories;

    public SmsReviewViewModel(Application application) {
        super(application);
        smsImportRepo = new SmsImportRepository(application);
        transactionRepo = new TransactionRepository(application);
        accountRepo = new AccountRepository(application);
        categoryRepo = new CategoryRepository(application);
        merchantRepo = new MerchantRepository(application);

        pendingItems = smsImportRepo.getPending();
        accounts = accountRepo.getAllActive();
        categories = categoryRepo.getAllActive();
    }

    /** Confirm an SMS import: create transaction and delete from smsImport table. */
    public void confirmAndCreate(SmsImport smsImport, String accountId, String categoryId) {
        // Get or create merchant if merchant name exists
        String merchantId = null;
        // Trim merchant name for consistency
        String trimmedMerchantName = (smsImport.merchantName != null) ? smsImport.merchantName.trim() : null;
        
        // FIRST: Save/update merchant with category
        // This ensures the merchant has categoryId for future SMS from this merchant
        if (trimmedMerchantName != null && !trimmedMerchantName.isEmpty() && !categoryId.isEmpty()) {
            merchantRepo.saveMerchantCategorySync(trimmedMerchantName, categoryId);
        }
        
        // SECOND: Find the merchant
        if (trimmedMerchantName != null && !trimmedMerchantName.isEmpty()) {
            Merchant existing = merchantRepo.findByName(trimmedMerchantName);
            if (existing != null) {
                merchantId = existing.uuid;
            }
        }

        // THIRD: Create the transaction
        Transaction t = new Transaction();
        t.uuid = UUID.randomUUID().toString();
        t.accountId = accountId;
        t.type = smsImport.detectedType;
        t.amount = smsImport.amount;
        t.date = smsImport.date;
        t.categoryId = categoryId.isEmpty() ? null : categoryId;
        t.merchantId = merchantId; // Set the merchant ID
        // Use the raw SMS text as the note for full transaction history
        t.note = smsImport.smsText;
        t.createdAt = System.currentTimeMillis();
        t.updatedAt = System.currentTimeMillis();
        t.deleted = false;

        // FOURTH: Insert transaction and DELETE from smsImport (instead of just confirming)
        transactionRepo.insert(t, () -> {
            // Delete the SmsImport record since we've converted it to a transaction
            smsImportRepo.delete(smsImport.uuid);
        });
    }

    public void ignore(String uuid) {
        smsImportRepo.ignore(uuid);
    }
}
