package com.financetracker.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Utility class to manage runtime permissions for SMS reading.
 * Handles permission requests for Android 6.0+ (API 23+)
 */
public class PermissionManager {

    private static final String TAG = "PermissionManager";
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;

    /**
     * Checks if the app has READ_SMS and RECEIVE_SMS permissions
     */
    public static boolean hasSmsPermissions(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Permissions are granted at install time for API < 23
            return true;
        }

        boolean hasReadSms = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED;
        boolean hasReceiveSms = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "SMS Permissions - READ_SMS: " + hasReadSms + ", RECEIVE_SMS: " + hasReceiveSms);
        return hasReadSms && hasReceiveSms;
    }

    /**
     * Requests SMS permissions from the user
     */
    public static void requestSmsPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "API level < 23, skipping permission request");
            return;
        }

        if (!hasSmsPermissions(activity)) {
            Log.d(TAG, "Requesting SMS permissions from user");
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.READ_SMS,
                            Manifest.permission.RECEIVE_SMS
                    },
                    SMS_PERMISSION_REQUEST_CODE
            );
        } else {
            Log.d(TAG, "SMS permissions already granted");
        }
    }

    /**
     * Handles the result of a permission request.
     * Returns true if SMS permissions were granted.
     */
    public static boolean handleSmsPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != SMS_PERMISSION_REQUEST_CODE) {
            return false;
        }

        if (grantResults.length >= 2) {
            boolean readSmsGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean receiveSmsGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

            Log.d(TAG, "Permission result - READ_SMS: " + readSmsGranted + ", RECEIVE_SMS: " + receiveSmsGranted);
            return readSmsGranted && receiveSmsGranted;
        }

        return false;
    }

    /**
     * Gets the permission request code for SMS permissions
     */
    public static int getSmsPermissionRequestCode() {
        return SMS_PERMISSION_REQUEST_CODE;
    }
}

