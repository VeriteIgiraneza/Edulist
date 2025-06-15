package com.example.edulist;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static void scheduleNotification(Context context, EduList eduList) {
        Log.d(TAG, "Attempting to schedule notification for: " + eduList.getName() +
                ", reminder time: " + eduList.getReminder());

        if (eduList.getReminder() == null || eduList.getReminder().isEmpty()) {
            Log.d(TAG, "No reminder set for list: " + eduList.getName());
            return;
        }

        if (eduList.getId() <= 0) {
            Log.e(TAG, "Invalid list ID for scheduling: " + eduList.getId());
            return;
        }

        try {
            // Parse the reminder date
            Date reminderDate = dateFormat.parse(eduList.getReminder());
            if (reminderDate == null) {
                Log.e(TAG, "Failed to parse reminder date: " + eduList.getReminder());
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(reminderDate);

            long currentTime = System.currentTimeMillis();
            long reminderTime = calendar.getTimeInMillis();

            // Skip if the reminder time is in the past (with 1 minute buffer)
            if (reminderTime <= currentTime + 60000) {
                Log.d(TAG, "Skipping reminder in the past for: " + eduList.getName() +
                        ". Reminder: " + reminderTime + ", Current: " + currentTime);
                return;
            }

            // Ensure notification channel exists before scheduling
            createNotificationChannel(context);

            // Wait a moment for channel creation to complete on older devices
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Create an intent for the broadcast receiver
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("LIST_NAME", eduList.getName());
            intent.putExtra("LIST_ID", eduList.getId());
            intent.putExtra("FOLDER_ID", eduList.getFolderId());
            intent.putExtra("DUE_DATE", eduList.getDueDate()); // Pass due date to receiver

            // Create a PendingIntent to be triggered when the alarm fires
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) eduList.getId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Setup alarm to trigger at the reminder time
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                try {
                    // Check if we can schedule exact alarms on Android 12+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    reminderTime,
                                    pendingIntent
                            );
                            Log.d(TAG, "Exact alarm scheduled with setExactAndAllowWhileIdle");
                        } else {
                            // Fallback for when exact alarms aren't allowed
                            alarmManager.setAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    reminderTime,
                                    pendingIntent
                            );
                            Log.d(TAG, "Approximate alarm scheduled with setAndAllowWhileIdle");
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                reminderTime,
                                pendingIntent
                        );
                        Log.d(TAG, "Exact alarm scheduled for Android M+");
                    } else {
                        alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                reminderTime,
                                pendingIntent
                        );
                        Log.d(TAG, "Exact alarm scheduled for older Android");
                    }

                    Log.d(TAG, "Reminder successfully scheduled for " + eduList.getName() +
                            " at " + dateFormat.format(reminderDate) +
                            " (in " + ((reminderTime - currentTime) / 1000) + " seconds)");

                } catch (SecurityException e) {
                    Log.e(TAG, "Permission denied for setting exact alarm: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "AlarmManager not available");
            }

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing reminder date: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "EduList Reminders";
            String description = "Notification channel for EduList assignment reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String CHANNEL_ID = "EduListReminderChannel";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }

    public static void cancelNotification(Context context, long listId) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) listId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        Log.d(TAG, "Cancelled reminder for list ID: " + listId);
    }

    public static void rescheduleAllReminders(Context context) {
        // Get all lists with reminders from the database
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        List<EduList> lists = dbHelper.getAllListsWithReminders();

        for (EduList list : lists) {
            scheduleNotification(context, list);
        }

        Log.d(TAG, "Rescheduled " + lists.size() + " reminders");
    }
}