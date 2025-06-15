package com.example.edulist;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";
    public static final String CHANNEL_ID = "EduListReminderChannel";

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "EduList Reminders";
            String description = "Notification channel for EduList assignment reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                channel.setAllowBubbles(false);
            }

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // In ReminderReceiver.java
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ReminderReceiver.onReceive() triggered");

        // FIRST: Create notification channel immediately
        createNotificationChannel(context);

        // Get the assignment details from the intent
        String listName = intent.getStringExtra("LIST_NAME");
        long listId = intent.getLongExtra("LIST_ID", -1);
        long folderId = intent.getLongExtra("FOLDER_ID", -1);
        String dueDate = intent.getStringExtra("DUE_DATE"); // Get due date

        Log.d(TAG, "Processing reminder - Name: " + listName + ", ID: " + listId + ", Due: " + dueDate);

        if (listName == null || listId == -1) {
            Log.e(TAG, "Invalid reminder data received");
            return;
        }

        // Create an intent to open the list when notification is tapped
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("FOLDER_ID", folderId);
        openIntent.putExtra("LIST_ID", listId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) listId,
                openIntent, PendingIntent.FLAG_IMMUTABLE);

        // Create "Complete" action
        Intent completeIntent = new Intent(context, NotificationActionReceiver.class);
        completeIntent.setAction("COMPLETE_ACTION");
        completeIntent.putExtra("LIST_ID", listId);
        completeIntent.putExtra("FOLDER_ID", folderId);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context,
                (int) (listId + 100),
                completeIntent,
                PendingIntent.FLAG_IMMUTABLE);

        // Create "Dismiss" action
        Intent dismissIntent = new Intent(context, NotificationActionReceiver.class);
        dismissIntent.setAction("DISMISS_ACTION");
        dismissIntent.putExtra("LIST_ID", listId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                (int) (listId + 200),
                dismissIntent,
                PendingIntent.FLAG_IMMUTABLE);

        // Format due date for notification
        String notificationText = listName;
        if (dueDate != null && !dueDate.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                Date parsedDate = inputFormat.parse(dueDate);
                if (parsedDate != null) {
                    notificationText = listName + "\nDue: " + outputFormat.format(parsedDate);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing due date: " + e.getMessage());
            }
        }

        // Build the notification with action buttons
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle(listName)
                .setContentText(dueDate != null && !dueDate.isEmpty() ? "Due: " + formatDueDate(dueDate) : "Assignment reminder")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_list, "Complete", completePendingIntent)
                .addAction(R.drawable.ic_reminder, "Dismiss", dismissPendingIntent)
                .setAutoCancel(false) // Prevents auto-cancel on tap
                .setOngoing(true) // Keeps the notification ongoing until user interacts
                .setDeleteIntent(null);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            if (notificationManager.areNotificationsEnabled()) {
                Notification notification = builder.build();
                notification.flags |= Notification.FLAG_NO_CLEAR; // This prevents swipe dismissal
                notificationManager.notify((int) listId, notification);
                Log.d(TAG, "Notification sent successfully for: " + listName);
            } else {
                Log.e(TAG, "Notifications are disabled by user");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage());
        }
    }

    private String formatDueDate(String dueDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            Date parsedDate = inputFormat.parse(dueDate);
            if (parsedDate != null) {
                return outputFormat.format(parsedDate);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting due date: " + e.getMessage());
        }
        return "";
    }
}
