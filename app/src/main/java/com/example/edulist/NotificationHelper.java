package com.example.edulist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

        try {
            // Parse the reminder date
            Date reminderDate = dateFormat.parse(eduList.getReminder());
            if (reminderDate == null) {
                Log.e(TAG, "Failed to parse reminder date: " + eduList.getReminder());
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(reminderDate);

            // Skip if the reminder time is in the past
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                Log.d(TAG, "Skipping reminder in the past for: " + eduList.getName());
                return;
            }

            // Create an intent for the broadcast receiver
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("LIST_NAME", eduList.getName());
            intent.putExtra("LIST_ID", eduList.getId());
            intent.putExtra("FOLDER_ID", eduList.getFolderId());

            // Create a PendingIntent to be triggered when the alarm fires
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) eduList.getId(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );

            // Setup alarm to trigger at the reminder time
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }

                Log.d(TAG, "Reminder scheduled for " + eduList.getName() + " at " +
                        dateFormat.format(reminderDate));
            }

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing reminder date: " + e.getMessage());
        }
    }

    public static void cancelNotification(Context context, long listId) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) listId,
                intent,
                PendingIntent.FLAG_IMMUTABLE
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