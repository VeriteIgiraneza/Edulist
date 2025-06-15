package com.example.edulist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ImageView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.util.Log;

public class NewListActivity extends AppCompatActivity {

    private EditText listNameEditText;
    private Button doneButton;
    private LinearLayout dueDateContainer;
    private LinearLayout reminderContainer;

    private Calendar dueDateCalendar = Calendar.getInstance();
    private Calendar reminderCalendar = Calendar.getInstance();
    private boolean hasDueDate = false;
    private boolean hasReminder = false;

    private long folderId;
    private String folderName;

    private ImageView clearDueDateButton;
    private ImageView clearReminderButton;

    // Edit mode variables
    private boolean isEditMode = false;
    private long editListId = -1;

    public static final String EXTRA_LIST_NAME = "com.example.edulist.LIST_NAME";
    public static final String EXTRA_LIST_DUE_DATE = "com.example.edulist.LIST_DUE_DATE";
    public static final String EXTRA_LIST_REMINDER = "com.example.edulist.LIST_REMINDER";
    public static final String EXTRA_FOLDER_ID = "com.example.edulist.FOLDER_ID";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
    private SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_newlist);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if we're in edit mode
        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);

        if (isEditMode) {
            editListId = getIntent().getLongExtra("LIST_ID", -1);
            folderId = getIntent().getLongExtra("FOLDER_ID", -1);
        } else {
            folderId = getIntent().getLongExtra(EXTRA_FOLDER_ID, -1);
        }

        if (folderId == -1) {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listNameEditText = findViewById(R.id.listname);
        doneButton = findViewById(R.id.donelist);
        dueDateContainer = findViewById(R.id.dueDateContainer);
        reminderContainer = findViewById(R.id.reminderContainer);
        clearDueDateButton = findViewById(R.id.clearDueDateButton);
        clearReminderButton = findViewById(R.id.clearReminderButton);

        // Pre-fill data if in edit mode
        if (isEditMode) {
            loadEditData();
        }

        dueDateContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        reminderContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        clearDueDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDueDate();
            }
        });

        clearReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearReminder();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveListAndFinish();
            }
        });
    }

    private void clearDueDate() {
        hasDueDate = false;
        TextView dueDateText = findViewById(R.id.dueDateText);
        dueDateText.setText("Due");
        clearDueDateButton.setVisibility(View.GONE);
        Toast.makeText(this, "Due date cleared", Toast.LENGTH_SHORT).show();
    }

    private void clearReminder() {
        hasReminder = false;
        TextView reminderText = findViewById(R.id.reminderText);
        reminderText.setText("Reminder");
        clearReminderButton.setVisibility(View.GONE);
        Toast.makeText(this, "Reminder cleared", Toast.LENGTH_SHORT).show();
    }

    private void loadEditData() {
        // Pre-fill the list name
        String listName = getIntent().getStringExtra("LIST_NAME");
        if (listName != null) {
            listNameEditText.setText(listName);
        }

        // Load due date
        String dueDate = getIntent().getStringExtra("DUE_DATE");
        if (dueDate != null && !dueDate.isEmpty()) {
            try {
                dueDateCalendar.setTime(fullFormat.parse(dueDate));
                hasDueDate = true;
                // Update the TextView to show the selected date
                TextView dueDateText = findViewById(R.id.dueDateText);
                dueDateText.setText(dateFormat.format(dueDateCalendar.getTime()));
                clearDueDateButton.setVisibility(View.VISIBLE);
            } catch (ParseException e) {
                hasDueDate = false;
            }
        }

        // Load reminder
        String reminder = getIntent().getStringExtra("REMINDER");
        if (reminder != null && !reminder.isEmpty()) {
            try {
                reminderCalendar.setTime(fullFormat.parse(reminder));
                hasReminder = true;
                // Update the TextView to show the selected date and time
                TextView reminderText = findViewById(R.id.reminderText);
                reminderText.setText(dateFormat.format(reminderCalendar.getTime())
                        + " " + timeFormat.format(reminderCalendar.getTime()));
                clearReminderButton.setVisibility(View.VISIBLE);
            } catch (ParseException e) {
                hasReminder = false;
            }
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dueDateCalendar.set(Calendar.YEAR, year);
                        dueDateCalendar.set(Calendar.MONTH, month);
                        dueDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        hasDueDate = true;

                        // Update the TextView to show the loaded date
                        TextView dueDateText = findViewById(R.id.dueDateText);
                        dueDateText.setText(dateFormat.format(dueDateCalendar.getTime()));
                        clearDueDateButton.setVisibility(View.VISIBLE);

                        Toast.makeText(NewListActivity.this,
                                "Due Date set: " + dateFormat.format(dueDateCalendar.getTime()),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                dueDateCalendar.get(Calendar.YEAR),
                dueDateCalendar.get(Calendar.MONTH),
                dueDateCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        reminderCalendar.set(Calendar.YEAR, year);
                        reminderCalendar.set(Calendar.MONTH, month);
                        reminderCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        showTimePicker();
                    }
                },
                reminderCalendar.get(Calendar.YEAR),
                reminderCalendar.get(Calendar.MONTH),
                reminderCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        reminderCalendar.set(Calendar.MINUTE, minute);
                        reminderCalendar.set(Calendar.SECOND, 0);
                        reminderCalendar.set(Calendar.MILLISECOND, 0);
                        hasReminder = true;

                        // Update the TextView to show the loaded reminder
                        TextView reminderText = findViewById(R.id.reminderText);
                        reminderText.setText(dateFormat.format(reminderCalendar.getTime())
                                + " " + timeFormat.format(reminderCalendar.getTime()));
                        clearReminderButton.setVisibility(View.VISIBLE);

                        Toast.makeText(NewListActivity.this,
                                "Reminder set: " + dateFormat.format(reminderCalendar.getTime())
                                        + " at " + timeFormat.format(reminderCalendar.getTime()),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                reminderCalendar.get(Calendar.HOUR_OF_DAY),
                reminderCalendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void saveListAndFinish() {
        Log.d("NewListActivity", "saveListAndFinish called");
        Log.d("NewListActivity", "hasReminder: " + hasReminder);
        if (hasReminder) {
            Log.d("NewListActivity", "Reminder time: " + fullFormat.format(reminderCalendar.getTime()));
        }
        String listName = listNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(listName)) {
            listNameEditText.setError("Please enter a list name");
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        if (isEditMode) {
            // Update existing list
            EduList existingList = dbHelper.getList(editListId);
            if (existingList != null) {
                // Cancel old notification
                if (existingList.getReminder() != null && !existingList.getReminder().isEmpty()) {
                    NotificationHelper.cancelNotification(this, existingList.getId());
                }

                // Update the list
                existingList.setName(listName);
                existingList.setDueDate(hasDueDate ? fullFormat.format(dueDateCalendar.getTime()) : null);
                existingList.setReminder(hasReminder ? fullFormat.format(reminderCalendar.getTime()) : null);

                int rowsUpdated = dbHelper.updateList(existingList);

                if (rowsUpdated > 0) {
                    // Schedule new notification if reminder is set
                    if (hasReminder) {
                        NotificationHelper.scheduleNotification(this, existingList);
                    }

                    Toast.makeText(this, "List updated: " + listName, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update list", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // Create new list (original functionality)
            // Create the list and save to database first
            EduList newList = new EduList(listName, folderId,
                    hasDueDate ? fullFormat.format(dueDateCalendar.getTime()) : null,
                    hasReminder ? fullFormat.format(reminderCalendar.getTime()) : null);

            long listId = dbHelper.addList(newList);

            if (listId != -1) {
                // Set the ID so notification scheduling works properly
                newList.setId(listId);

                // Schedule the notification if we have a reminder
                if (hasReminder) {
                    NotificationHelper.createNotificationChannel(this);
                    NotificationHelper.scheduleNotification(this, newList);
                    Toast.makeText(this, "Reminder scheduled for: " + listName, Toast.LENGTH_SHORT).show();
                }

                // Set result and finish the activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_LIST_NAME, listName);
                resultIntent.putExtra(EXTRA_FOLDER_ID, folderId);
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(this, "List created: " + listName, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to create list", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // For testing in NewListActivity
    private void setupTestReminder() {
        Calendar testReminder = Calendar.getInstance();
        testReminder.add(Calendar.SECOND, 30);  // 30 seconds from now

        reminderCalendar = testReminder;
        hasReminder = true;

        Toast.makeText(this, "Test reminder set for 30 seconds from now", Toast.LENGTH_SHORT).show();
    }
}