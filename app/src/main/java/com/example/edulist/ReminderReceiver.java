package com.example.edulist;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";
    private static final String CHANNEL_ID = "EduListReminderChannel";

    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ (Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "EduList Reminders";
            String description = "Notification channel for EduList assignment reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // In ReminderReceiver.java
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Reminder received");

        // Get the assignment details from the intent
        String listName = intent.getStringExtra("LIST_NAME");
        long listId = intent.getLongExtra("LIST_ID", -1);
        long folderId = intent.getLongExtra("FOLDER_ID", -1);

        // Create notification channel for Android O and above
        createNotificationChannel(context);

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
                (int)(listId + 100), // Use different request code
                completeIntent,
                PendingIntent.FLAG_IMMUTABLE);

        // Create "Dismiss" action
        Intent dismissIntent = new Intent(context, NotificationActionReceiver.class);
        dismissIntent.setAction("DISMISS_ACTION");
        dismissIntent.putExtra("LIST_ID", listId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                (int)(listId + 200), // Use different request code
                dismissIntent,
                PendingIntent.FLAG_IMMUTABLE);

        // Build the notification with action buttons
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle("EduList Reminder")
                .setContentText(listName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_list, "Complete", completePendingIntent)
                .addAction(R.drawable.ic_reminder, "Dismiss", dismissPendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify((int) listId, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted: " + e.getMessage());
        }
    }
}