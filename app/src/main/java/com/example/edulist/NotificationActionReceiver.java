package com.example.edulist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationManagerCompat;

public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long listId = intent.getLongExtra("LIST_ID", -1);

        Log.d(TAG, "Action received: " + action + " for list ID: " + listId);

        if (listId == -1) {
            Log.e(TAG, "Invalid list ID");
            return;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        try {
            // Always cancel the notification first
            notificationManager.cancel((int) listId);

            if ("COMPLETE_ACTION".equals(action)) {
                // Mark the task as completed
                DatabaseHelper dbHelper = new DatabaseHelper(context);
                EduList eduList = dbHelper.getList(listId);

                if (eduList != null) {
                    // Here you can add logic to mark the list item as completed
                    // For example, you might add a 'completed' field to your EduList class
                    // eduList.setCompleted(true);
                    // dbHelper.updateList(eduList);

                    Log.d(TAG, "Marked list as completed: " + eduList.getName());
                }
            } else if ("DISMISS_ACTION".equals(action)) {
                // Just dismiss the notification, already done above
                Log.d(TAG, "Notification dismissed for list ID: " + listId);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission issue: " + e.getMessage());
        }
    }
}