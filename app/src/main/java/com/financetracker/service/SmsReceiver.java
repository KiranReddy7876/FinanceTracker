package com.financetracker.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "=== BroadcastReceiver.onReceive() called ===");
        Log.d(TAG, "Action: " + (intent != null ? intent.getAction() : "null"));
        
        try {
            if (intent == null) {
                Log.w(TAG, "Intent is null, cannot process SMS");
                return;
            }

            if (!SMS_RECEIVED.equals(intent.getAction())) {
                Log.d(TAG, "Not an SMS_RECEIVED action, ignoring. Got: " + intent.getAction());
                return;
            }

            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                Log.w(TAG, "Bundle is null");
                return;
            }

            Object[] pdus = (Object[]) bundle.get("pdus");
            String format = bundle.getString("format");
            if (pdus == null) {
                Log.w(TAG, "PDUs are null - SMS_RECEIVED broadcast has no SMS data!");
                return;
            }

            Log.d(TAG, "Received SMS with " + pdus.length + " part(s), format: " + format);

            StringBuilder fullMessage = new StringBuilder();
            String sender = null;

            for (Object pdu : pdus) {
                try {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                    if (sms == null) continue;
                    if (sender == null) sender = sms.getOriginatingAddress();
                    fullMessage.append(sms.getMessageBody());
                } catch (Exception e) {
                    Log.w(TAG, "Error processing individual SMS PDU", e);
                }
            }

            String body = fullMessage.toString();
            Log.d(TAG, "Received SMS from: " + sender + ", body length: " + body.length());

            if (!SmsParser.isTransactionSms(body, sender)) {
                Log.d(TAG, "Not a transaction SMS, ignoring");
                return;
            }

            SmsParser.ParsedTransaction parsed = SmsParser.parse(body);
            if (parsed == null) {
                Log.w(TAG, "Could not parse SMS");
                return;
            }

            Log.d(TAG, "Parsed SMS: amount=" + parsed.amount + ", type=" + parsed.type + 
                       ", merchant=" + parsed.merchant);

            // Use JobIntentService which handles WakeLocks automatically
            Log.d(TAG, "Queuing work with JobIntentService");
            try {
                SmsProcessingService.startSmsProcessing(context, body, parsed);
                Log.d(TAG, "✓ Work queued successfully with JobIntentService");
            } catch (Exception e) {
                Log.e(TAG, "✗ Error queuing work with JobIntentService", e);
                throw e;
            }
            
            // CRITICAL: Consume the SMS broadcast AFTER successful processing
            // This ensures we don't forward the SMS to other apps/default SMS app
            Log.d(TAG, "Consuming SMS broadcast - setResultCode(RESULT_OK)");
            try {
                setResultCode(Activity.RESULT_OK);
                Log.d(TAG, "✓ Broadcast consumed - setResultCode(RESULT_OK) successful");
            } catch (Exception e) {
                Log.e(TAG, "✗ Error consuming broadcast with setResultCode()", e);
                // Don't re-throw - broadcast consumption error shouldn't fail the whole process
                // But we still want to log it for debugging
                e.printStackTrace();
            }
            
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException in onReceive", e);
            Log.e(TAG, "Exception details: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in onReceive", e);
            Log.e(TAG, "Exception details: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
