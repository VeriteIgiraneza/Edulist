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
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        folderId = getIntent().getLongExtra(EXTRA_FOLDER_ID, -1);

        if (folderId == -1) {
            Toast.makeText(this, "Error: No folder selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listNameEditText = findViewById(R.id.listname);
        doneButton = findViewById(R.id.donelist);
        dueDateContainer = findViewById(R.id.dueDateContainer);
        reminderContainer = findViewById(R.id.reminderContainer);

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

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveListAndFinish();
            }
        });
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
                        hasReminder = true;

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
        String listName = listNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(listName)) {
            listNameEditText.setError("Please enter a list name");
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_LIST_NAME, listName);
        resultIntent.putExtra(EXTRA_FOLDER_ID, folderId);

        if (hasDueDate) {
            resultIntent.putExtra(EXTRA_LIST_DUE_DATE, fullFormat.format(dueDateCalendar.getTime()));
        }

        if (hasReminder) {
            resultIntent.putExtra(EXTRA_LIST_REMINDER, fullFormat.format(reminderCalendar.getTime()));
        }

        // Create the list and save to database first
        EduList newList = new EduList(listName, folderId,
                hasDueDate ? fullFormat.format(dueDateCalendar.getTime()) : null,
                hasReminder ? fullFormat.format(reminderCalendar.getTime()) : null);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        long listId = dbHelper.addList(newList);

        if (listId != -1) {
            // Set the ID so notification scheduling works properly
            newList.setId(listId);

            // Schedule the notification if we have a reminder
            if (hasReminder) {
                NotificationHelper.scheduleNotification(this, newList);
                Toast.makeText(this, "Reminder scheduled for: " + listName, Toast.LENGTH_SHORT).show();
            }

            // Set result and finish the activity
            setResult(RESULT_OK, resultIntent);
            Toast.makeText(this, "List created: " + listName, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to create list", Toast.LENGTH_SHORT).show();
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
