package com.financetracker.service;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.financetracker.data.db.AppDatabase;
import com.financetracker.data.db.entity.Account;
import com.financetracker.data.db.entity.Merchant;
import com.financetracker.data.db.entity.SmsImport;
import com.financetracker.utils.SmsAccountNumberExtractor;
import java.util.UUID;

/**
 * Worker for processing SMS imports using WorkManager.
 * This is the modern replacement for deprecated JobIntentService.
 * WorkManager automatically handles:
 * - WakeLocks
 * - Background execution constraints
 * - Retry policies
 * - Device compatibility (even on pre-API 26)
 */
public class SmsProcessingWorker extends Worker {

    private static final String TAG = "SmsProcessingWorker";

    public SmsProcessingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "=== SmsProcessingWorker.doWork() START ===");
        Log.d(TAG, "Running on thread: " + Thread.currentThread().getName());
        
        try {
            // Extract data from work request
            String body = getInputData().getString("sms_body");
            double amount = getInputData().getDouble("sms_amount", 0);
            String type = getInputData().getString("sms_type");
            long date = getInputData().getLong("sms_date", 0);
            String merchantName = getInputData().getString("sms_merchant");

            Log.d(TAG, "Processing SMS: amount=" + amount + ", type=" + type + ", merchant=" + merchantName);

            // Check if this is a credit card payment or cash transaction
            boolean isCreditCardPayment = SmsParser.isCreditCardPayment(body);
            boolean isCashWithdrawal = SmsParser.isCashTransaction(body);
            
            if (isCreditCardPayment || isCashWithdrawal) {
                Log.d(TAG, "✓ Detected TRANSFER transaction - CC Payment: " + isCreditCardPayment + ", Cash: " + isCashWithdrawal);
                type = "TRANSFER";  // Override type to TRANSFER
            }

            // Get database instance
            AppDatabase db = null;
            try {
                db = AppDatabase.getInstance(getApplicationContext());
                Log.d(TAG, "✓ Database instance obtained");
            } catch (Exception e) {
                Log.e(TAG, "✗ CRITICAL ERROR: Could not get database instance", e);
                e.printStackTrace();
                return Result.retry();
            }
            
            if (db == null) {
                Log.e(TAG, "✗ CRITICAL ERROR: Database instance is null");
                return Result.retry();
            }
            
            // Step 1: Extract and match account
            String extractedAccountNumber = null;
            String matchedAccountId = null;

            if (SmsAccountNumberExtractor.likelyContainsAccountNumber(body)) {
                extractedAccountNumber = SmsAccountNumberExtractor.extractLast4Digits(body);
                Log.d(TAG, "Step 1: Extracted account number: " + extractedAccountNumber);
            }

            if (extractedAccountNumber != null && SmsAccountNumberExtractor.isValidAccountNumber(extractedAccountNumber)) {
                try {
                    Account matchedAccount = db.accountDao().getByAccountNumber(extractedAccountNumber);
                    if (matchedAccount != null) {
                        matchedAccountId = matchedAccount.uuid;
                        Log.d(TAG, "Step 1: ✓ Matched account: " + matchedAccount.name + " (ID: " + matchedAccountId + ")");
                    } else {
                        Log.d(TAG, "Step 1: ✗ No account found for: " + extractedAccountNumber);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Step 1: Error matching account", e);
                }
            }

            // Step 2: Lookup merchant and check if categorized
            String trimmedMerchantName = (merchantName != null) ? merchantName.trim() : null;
            String autoCategory = null;

            Log.d(TAG, "Step 2: Processing merchant: " + (trimmedMerchantName != null ? trimmedMerchantName : "NONE"));

            if (trimmedMerchantName != null && !trimmedMerchantName.isEmpty()) {
                try {
                    Merchant knownMerchant = db.merchantDao().findByName(trimmedMerchantName);
                    if (knownMerchant != null) {
                        if (knownMerchant.categoryId != null && !knownMerchant.categoryId.isEmpty()) {
                            autoCategory = knownMerchant.categoryId;
                            Log.d(TAG, "Step 2: ✓ Found categorized merchant: " + trimmedMerchantName + " → " + autoCategory);
                        } else {
                            Log.d(TAG, "Step 2: ✓ Found merchant but NO category: " + trimmedMerchantName);
                        }
                    } else {
                        Log.d(TAG, "Step 2: ✗ Merchant not found: " + trimmedMerchantName);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Step 2: Error looking up merchant", e);
                }
            }
            
            // Step 2b: For TRANSFER type, extract destination account
            String transferToAccountId = null;
            if ("TRANSFER".equals(type)) {
                String destAccountNumber = SmsParser.extractTransferDestinationAccount(body);
                if (destAccountNumber != null && SmsAccountNumberExtractor.isValidAccountNumber(destAccountNumber)) {
                    try {
                        Account destAccount = db.accountDao().getByAccountNumber(destAccountNumber);
                        if (destAccount != null) {
                            transferToAccountId = destAccount.uuid;
                            Log.d(TAG, "Step 2b: ✓ Destination account found: " + destAccount.name + " (ID: " + transferToAccountId + ")");
                        } else {
                            Log.d(TAG, "Step 2b: ✗ Destination account not found for: " + destAccountNumber);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Step 2b: Error finding destination account", e);
                    }
                }
            }

            // Step 3: Build SMS import record
            SmsImport record = new SmsImport();
            record.uuid = UUID.randomUUID().toString();
            record.smsText = body;
            record.amount = amount;
            record.detectedType = type;
            record.date = date;
            record.accountId = matchedAccountId;
            record.categoryId = autoCategory;
            record.merchantName = trimmedMerchantName;
            record.transferToAccountId = transferToAccountId;  // Set for TRANSFER transactions
            record.createdAt = System.currentTimeMillis();
            record.updatedAt = System.currentTimeMillis();
            record.deleted = false;

            Log.d(TAG, "Step 3: Built SMS record - UUID: " + record.uuid);

            // Step 4: Determine status and decide flow
            // TRANSFER transactions should ALWAYS require user review for categorization
            // Only auto-confirm EXPENSE/INCOME types with categorized merchants
            boolean autoConfirm = (matchedAccountId != null && autoCategory != null && !"TRANSFER".equals(type));
            record.status = autoConfirm ? "CONFIRMED" : "PENDING";
            
            Log.d(TAG, "Step 4: Status=" + record.status +
                       " (hasAccount=" + (matchedAccountId != null) +
                       ", hasCategory=" + (autoCategory != null) +
                       ", isTransfer=" + "TRANSFER".equals(type) + 
                       ", autoConfirm=" + autoConfirm + ")");

            try {
                if (autoConfirm) {
                    // If merchant is categorized, skip SmsImport record and create Transaction directly
                    Log.d(TAG, "Step 4: Auto-confirm detected - Creating transaction directly");
                    try {
                        com.financetracker.data.db.entity.Transaction transaction = new com.financetracker.data.db.entity.Transaction();
                        transaction.uuid = UUID.randomUUID().toString();
                        transaction.accountId = matchedAccountId;
                        transaction.type = type;
                        transaction.amount = amount;
                        transaction.date = date;
                        transaction.categoryId = autoCategory;

                        // For TRANSFER type, set destination account
                        if ("TRANSFER".equals(type)) {
                            transaction.transferToAccountId = transferToAccountId;
                            Log.d(TAG, "Step 5: Transfer transaction - from: " + matchedAccountId + " to: " + transferToAccountId);
                        }

                        // IMPORTANT: Set merchantId by looking up the merchant we found earlier
                        String merchantId = null;
                        if (trimmedMerchantName != null && !trimmedMerchantName.isEmpty()) {
                            try {
                                Merchant knownMerchant = db.merchantDao().findByName(trimmedMerchantName);
                                if (knownMerchant != null) {
                                    merchantId = knownMerchant.uuid;
                                    Log.d(TAG, "Step 5: ✓ Merchant found, setting merchantId: " + merchantId);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Step 5: Error finding merchant by name", e);
                            }
                        }

                        transaction.merchantId = merchantId; // Set actual merchant ID for nickname lookup
                        String merchantPart = (trimmedMerchantName != null && !trimmedMerchantName.isEmpty())
                                ? " - " + trimmedMerchantName : "";
                        transaction.note = "SMS Import" + merchantPart;
                        transaction.createdAt = System.currentTimeMillis();
                        transaction.updatedAt = System.currentTimeMillis();
                        transaction.deleted = false;

                        db.transactionDao().insert(transaction);
                        Log.d(TAG, "Step 5: ✓ TRANSACTION CREATED DIRECTLY with merchantId: " + merchantId);
                        Log.d(TAG, "=== SMS Processing Complete - TRANSACTION CREATED ===");
                    } catch (Exception e) {
                        Log.e(TAG, "Step 5: ERROR creating transaction", e);
                        e.printStackTrace();
                        return Result.retry();
                    }
                } else {
                    // No auto-confirm: Create SmsImport record for user review
                    Log.d(TAG, "Step 4: No auto-confirm - Creating SmsImport record");
                    try {
                        db.smsImportDao().insert(record);
                        Log.d(TAG, "Step 4: ✓ SMS import saved to database - UUID: " + record.uuid);
                    } catch (Exception e) {
                        Log.e(TAG, "Step 4: ERROR inserting SMS import record", e);
                        e.printStackTrace();
                        return Result.retry();
                    }

                    try {
                        Log.d(TAG, "Step 5: Sending notification");
                        SmsImportNotificationService.notifyPendingImport(getApplicationContext(), 1);
                        Log.d(TAG, "Step 5: ✓ Notification sent");
                    } catch (Exception e) {
                        Log.e(TAG, "Step 5: Error sending notification", e);
                        // Don't fail the whole work - notification is optional
                    }
                    Log.d(TAG, "=== SMS Processing Complete - PENDING REVIEW ===");
                }
            } catch (Exception e) {
                Log.e(TAG, "Step 4: CRITICAL ERROR - could not process SMS", e);
                Log.e(TAG, "=== SMS Processing FAILED ===");
                e.printStackTrace();
                return Result.retry();
            }

            Log.d(TAG, "=== SMS Processing Completed Successfully ===");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "CRITICAL: Unhandled exception in doWork()", e);
            e.printStackTrace();
            return Result.retry();
        }
    }
}

