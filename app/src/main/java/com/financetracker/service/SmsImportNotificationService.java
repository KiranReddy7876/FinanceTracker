package com.financetracker.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.financetracker.R;
import com.financetracker.ui.MainActivity;

/**
 * Service to send notifications when SMS imports are received and pending review
 */
public class SmsImportNotificationService {

    private static final String CHANNEL_ID = "sms_imports";
    private static final int NOTIFICATION_ID = 1001;
    private static final String TAG = "SmsImportNotification";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "SMS Import Notifications",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for pending SMS transaction imports");
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Show notification that an SMS import is pending review
     * @param context App context
     * @param count Number of pending SMS imports
     */
    public static void notifyPendingImport(Context context, int count) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("navigate_to", "sms_import");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String contentText = count == 1 ? 
            "1 transaction import pending review" : 
            count + " transaction imports pending review";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SMS Transaction Detected")
            .setContentText(contentText)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    /**
     * Clear the notification
     * @param context App context
     */
    public static void clearNotification(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
        }
    }
}

