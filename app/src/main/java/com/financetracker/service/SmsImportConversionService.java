package com.financetracker.service;

import android.content.Context;
import android.util.Log;
import com.financetracker.data.db.AppDatabase;
import com.financetracker.data.db.dao.MerchantDao;
import com.financetracker.data.db.dao.SmsImportDao;
import com.financetracker.data.db.dao.TransactionDao;
import com.financetracker.data.db.entity.Merchant;
import com.financetracker.data.db.entity.SmsImport;
import com.financetracker.data.db.entity.Transaction;
import java.util.List;
import java.util.UUID;

/**
 * Service to convert confirmed SMS imports into actual transactions.
 * This is called when user confirms an SMS import in the review UI.
 */
public class SmsImportConversionService {

    private static final String TAG = "SmsImportConversion";

    /**
     * Convert a confirmed SMS import to a transaction
     * @param context App context
     * @param smsImport The confirmed SMS import record
     */
    public static void convertToTransaction(Context context, SmsImport smsImport) {
        if (smsImport == null) {
            Log.w(TAG, "Cannot convert null SMS import");
            return;
        }

        try {
            AppDatabase db = AppDatabase.getInstance(context);
            if (db == null) {
                Log.e(TAG, "Database instance is null");
                return;
            }
            
            SmsImportDao smsImportDao = db.smsImportDao();
            TransactionDao transactionDao = db.transactionDao();
            MerchantDao merchantDao = db.merchantDao();

            // Fetch fresh record from database to ensure all fields are populated
            SmsImport freshRecord = null;
            try {
                freshRecord = smsImportDao.getById(smsImport.uuid);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching fresh SMS import record: " + e.getMessage());
            }
            
            if (freshRecord == null) {
                Log.w(TAG, "SMS import record not found for UUID: " + smsImport.uuid);
                return;
            }
            
            if (freshRecord.status == null || !freshRecord.status.equals("CONFIRMED")) {
                Log.w(TAG, "Cannot convert non-confirmed SMS import. Status: " + freshRecord.status);
                return;
            }

            if (freshRecord.accountId == null || freshRecord.accountId.isEmpty()) {
                Log.w(TAG, "SMS import missing accountId, cannot convert to transaction");
                return;
            }

            // Use the fresh record with all updated values
            smsImport = freshRecord;

            // Get or create merchant if merchant name exists
            String merchantId = null;
            if (smsImport.merchantName != null && !smsImport.merchantName.isEmpty()) {
                // Trim merchant name for consistency
                String trimmedMerchantName = smsImport.merchantName.trim();
                
                Merchant existing = null;
                try {
                    existing = merchantDao.findByName(trimmedMerchantName);
                } catch (Exception e) {
                    Log.e(TAG, "Error finding merchant by name: " + e.getMessage());
                }
                
                if (existing != null) {
                    merchantId = existing.uuid;
                    Log.d(TAG, "Found existing merchant: " + existing.name + " (categoryId: " + 
                            (existing.categoryId != null ? existing.categoryId : "null") + ")");
                } else {
                    // Only create new merchant if we have a category to assign
                    // If no category, let user categorize it later through pending queue
                    if (smsImport.categoryId != null && !smsImport.categoryId.isEmpty()) {
                        try {
                            Merchant newMerchant = new Merchant(
                                UUID.randomUUID().toString(),
                                trimmedMerchantName,
                                smsImport.categoryId // Only set if not null
                            );
                            merchantDao.insert(newMerchant);
                            merchantId = newMerchant.uuid;
                            Log.d(TAG, "Created new merchant: " + trimmedMerchantName + 
                                    " with categoryId: " + smsImport.categoryId);
                        } catch (Exception e) {
                            Log.e(TAG, "Error creating new merchant: " + e.getMessage());
                        }
                    } else {
                        Log.d(TAG, "Skipping merchant creation for '" + trimmedMerchantName + 
                                "' - no category assigned yet");
                    }
                }
            }

            // Create transaction from SMS import
            Transaction transaction = new Transaction();
            transaction.uuid = UUID.randomUUID().toString();
            transaction.accountId = smsImport.accountId;
            transaction.type = smsImport.detectedType; // EXPENSE or INCOME
            transaction.amount = smsImport.amount;
            transaction.date = smsImport.date;
            transaction.categoryId = smsImport.categoryId; // Can be null if user didn't select
            transaction.merchantId = merchantId; // Set the merchant ID
            transaction.referenceId = smsImport.uuid; // Link back to SMS import for audit
            // Use the raw SMS text as the note for full transaction history
            transaction.note = smsImport.smsText != null ? smsImport.smsText : "";
            transaction.createdAt = System.currentTimeMillis();
            transaction.updatedAt = System.currentTimeMillis();
            transaction.deleted = false;

            try {
                transactionDao.insert(transaction);
                Log.d(TAG, "Successfully converted SMS import to transaction. " +
                        "Amount: " + transaction.amount + ", Type: " + transaction.type +
                        ", Account: " + transaction.accountId +
                        ", Merchant: " + (smsImport.merchantName != null ? smsImport.merchantName : "None") +
                        ", Category: " + (smsImport.categoryId != null ? smsImport.categoryId : "None"));
            } catch (Exception e) {
                Log.e(TAG, "Failed to insert transaction: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in convertToTransaction: " + e.getMessage(), e);
        }
    }

    /**
     * Process all confirmed SMS imports and convert them to transactions
     * @param context App context
     */
    public static void processAllConfirmed(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        SmsImportDao smsImportDao = db.smsImportDao();


        TransactionDao transactionDao = db.transactionDao();

        new Thread(() -> {
            try {
                // Get all confirmed but not yet processed SMS imports
                // We use a flag in the database to track which ones have been converted
                List<SmsImport> confirmed = smsImportDao.getConfirmed();
                if (confirmed == null || confirmed.isEmpty()) {
                    return;
                }

                for (SmsImport smsImport : confirmed) {
                    if (smsImport.accountId == null) {
                        Log.w(TAG, "Skipping SMS import " + smsImport.uuid + " - no account assigned");
                        continue;
                    }

                    Transaction transaction = new Transaction();
                    transaction.uuid = UUID.randomUUID().toString();
                    transaction.accountId = smsImport.accountId;
                    transaction.type = smsImport.detectedType;
                    transaction.amount = smsImport.amount;
                    transaction.date = smsImport.date;
                    transaction.categoryId = smsImport.categoryId;
                    transaction.referenceId = smsImport.uuid;
                    transaction.note = "Auto-imported from SMS";
                    transaction.createdAt = System.currentTimeMillis();
                    transaction.updatedAt = System.currentTimeMillis();
                    transaction.deleted = false;

                    transactionDao.insert(transaction);
                    Log.d(TAG, "Converted SMS import " + smsImport.uuid + " to transaction");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing confirmed SMS imports", e);
            }
        }).start();
    }
}

