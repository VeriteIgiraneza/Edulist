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
            if ("COMPLETE_ACTION".equals(action)) {
                // Mark the task as completed
                DatabaseHelper dbHelper = new DatabaseHelper(context);
                EduList eduList = dbHelper.getList(listId);

                if (eduList != null) {
                    eduList.setCompleted(true);
                    dbHelper.updateList(eduList);
                    Log.d(TAG, "Marked list as completed: " + eduList.getName());
                }

                // Cancel the notification after completing
                notificationManager.cancel((int) listId);
            } else if ("DISMISS_ACTION".equals(action)) {
                // Cancel the notification on dismiss
                notificationManager.cancel((int) listId);
                Log.d(TAG, "Notification dismissed for list ID: " + listId);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission issue: " + e.getMessage());
        }
    }
}