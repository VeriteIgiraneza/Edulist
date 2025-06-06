package com.example.edulist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BootReceiver triggered with action: " + intent.getAction());
        if (intent.getAction() != null &&
                intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Log.d(TAG, "Device rebooted, rescheduling all reminders");
            NotificationHelper.rescheduleAllReminders(context);
        }
    }
}