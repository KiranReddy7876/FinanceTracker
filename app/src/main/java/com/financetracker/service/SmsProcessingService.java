package com.financetracker.service;

import android.content.Context;
import android.util.Log;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

/**
 * SMS Processing Service - Modern replacement for deprecated JobIntentService
 * Now uses WorkManager which:
 * - Automatically handles WakeLocks
 * - Works on all Android versions
 * - Handles retries and backoff
 * - Respects device constraints
 * - Is not deprecated
 */
public class SmsProcessingService {

    private static final String TAG = "SmsProcessingService";

    public static void startSmsProcessing(Context context, String body, SmsParser.ParsedTransaction parsed) {
        Log.d(TAG, "startSmsProcessing() called");
        
        try {
            // Validate inputs
            if (context == null) {
                Log.e(TAG, "✗ ERROR: Context is null, cannot queue work");
                return;
            }
            
            if (body == null || body.isEmpty()) {
                Log.e(TAG, "✗ ERROR: SMS body is null or empty");
                return;
            }
            
            if (parsed == null) {
                Log.e(TAG, "✗ ERROR: ParsedTransaction is null");
                return;
            }
            
            Log.d(TAG, "✓ Inputs validated - proceeding to WorkManager");
            
            // Build data to pass to Worker
            Data workData = new Data.Builder()
                    .putString("sms_body", body != null ? body : "")
                    .putDouble("sms_amount", parsed.amount)
                    .putString("sms_type", parsed.type != null ? parsed.type : "EXPENSE")
                    .putLong("sms_date", parsed.date)
                    .putString("sms_merchant", parsed.merchant != null ? parsed.merchant : "")
                    .build();
            
            Log.d(TAG, "✓ Work data built with all SMS details");
            
            // Create work request
            OneTimeWorkRequest smsWorkRequest = new OneTimeWorkRequest.Builder(SmsProcessingWorker.class)
                    .setInputData(workData)
                    .build();
            
            Log.d(TAG, "✓ Work request created");
            
            try {
                // Enqueue work with WorkManager
                WorkManager.getInstance(context).enqueue(smsWorkRequest);
                Log.d(TAG, "✓ SUCCESS: Work enqueued with WorkManager - ID: " + smsWorkRequest.getId());
            } catch (Exception e) {
                Log.e(TAG, "✗ ERROR: WorkManager.enqueue() failed", e);
                Log.e(TAG, "   Exception: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            Log.e(TAG, "✗ CRITICAL ERROR in startSmsProcessing()", e);
            e.printStackTrace();
        }
    }
}


