package com.example.edulist;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditListActivity extends AppCompatActivity {

    private EditText assignmentNameEditText;
    private Button saveButton, deleteButton;
    private LinearLayout dueDateContainer, reminderContainer;
    private TextView dueDateText, reminderText;
    private Button clearDueDateButton, clearReminderButton;
    private Spinner folderSpinner;
    private EditText notesEditText;

    private DatabaseHelper dbHelper;
    private EduList currentAssignment;
    private List<Folder> folderList;
    private ArrayAdapter<Folder> folderAdapter;

    private Calendar dueDateCalendar = Calendar.getInstance();
    private Calendar reminderCalendar = Calendar.getInstance();
    private boolean hasDueDate = false;
    private boolean hasReminder = false;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
    private SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);

    public static final String EXTRA_ASSIGNMENT_ID = "com.example.edulist.ASSIGNMENT_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        // Get assignment ID from intent
        long assignmentId = getIntent().getLongExtra(EXTRA_ASSIGNMENT_ID, -1);
        if (assignmentId == -1) {
            Toast.makeText(this, "Error: Assignment not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load assignment from database
        currentAssignment = dbHelper.getList(assignmentId);
        if (currentAssignment == null) {
            Toast.makeText(this, "Error: Assignment not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadFolders();
        loadAssignmentData();
        setupClickListeners();
    }

    private void initializeViews() {
        assignmentNameEditText = findViewById(R.id.assignmentName);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        dueDateContainer = findViewById(R.id.dueDateContainer);
        reminderContainer = findViewById(R.id.reminderContainer);
        dueDateText = findViewById(R.id.dueDateText);
        reminderText = findViewById(R.id.reminderText);
        clearDueDateButton = findViewById(R.id.clearDueDateButton);
        clearReminderButton = findViewById(R.id.clearReminderButton);
        folderSpinner = findViewById(R.id.folderSpinner);
        notesEditText = findViewById(R.id.notesEditText);
    }

    private void loadFolders() {
        folderList = dbHelper.getAllFolders();

        if (folderList.isEmpty()) {
            // Create default folder if none exist
            Folder defaultFolder = new Folder("Edu Folder", android.graphics.Color.parseColor("#03A9F4"));
            long id = dbHelper.addFolder(defaultFolder);
            defaultFolder.setId(id);
            folderList.add(defaultFolder);
        }

        folderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, folderList);
        folderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        folderSpinner.setAdapter(folderAdapter);

        // Set current folder as selected
        for (int i = 0; i < folderList.size(); i++) {
            if (folderList.get(i).getId() == currentAssignment.getFolderId()) {
                folderSpinner.setSelection(i);
                break;
            }
        }
    }

    private void loadAssignmentData() {
        // Set assignment name
        assignmentNameEditText.setText(currentAssignment.getName());

        // Load due date
        if (currentAssignment.getDueDate() != null && !currentAssignment.getDueDate().isEmpty()) {
            try {
                dueDateCalendar.setTime(fullFormat.parse(currentAssignment.getDueDate()));
                hasDueDate = true;
                dueDateText.setText(dateFormat.format(dueDateCalendar.getTime()));
                clearDueDateButton.setVisibility(View.VISIBLE);
            } catch (ParseException e) {
                hasDueDate = false;
                dueDateText.setText("No due date set");
                clearDueDateButton.setVisibility(View.GONE);
            }
        } else {
            hasDueDate = false;
            dueDateText.setText("No due date set");
            clearDueDateButton.setVisibility(View.GONE);
        }

        // Load reminder
        if (currentAssignment.getReminder() != null && !currentAssignment.getReminder().isEmpty()) {
            try {
                reminderCalendar.setTime(fullFormat.parse(currentAssignment.getReminder()));
                hasReminder = true;
                reminderText.setText(displayFormat.format(reminderCalendar.getTime()));
                clearReminderButton.setVisibility(View.VISIBLE);
            } catch (ParseException e) {
                hasReminder = false;
                reminderText.setText("No reminder set");
                clearReminderButton.setVisibility(View.GONE);
            }
        } else {
            hasReminder = false;
            reminderText.setText("No reminder set");
            clearReminderButton.setVisibility(View.GONE);
        }

        // Load notes (you might need to add a notes field to your EduList model)
        // For now, we'll leave it empty as the current model doesn't have notes
        notesEditText.setText("");
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveAssignment());
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());

        dueDateContainer.setOnClickListener(v -> showDatePicker());
        reminderContainer.setOnClickListener(v -> showDateTimePicker());

        clearDueDateButton.setOnClickListener(v -> clearDueDate());
        clearReminderButton.setOnClickListener(v -> clearReminder());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    dueDateCalendar.set(Calendar.YEAR, year);
                    dueDateCalendar.set(Calendar.MONTH, month);
                    dueDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    hasDueDate = true;
                    dueDateText.setText(dateFormat.format(dueDateCalendar.getTime()));
                    clearDueDateButton.setVisibility(View.VISIBLE);
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
                (view, year, month, dayOfMonth) -> {
                    reminderCalendar.set(Calendar.YEAR, year);
                    reminderCalendar.set(Calendar.MONTH, month);
                    reminderCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker();
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
                (view, hourOfDay, minute) -> {
                    reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    reminderCalendar.set(Calendar.MINUTE, minute);
                    hasReminder = true;
                    reminderText.setText(displayFormat.format(reminderCalendar.getTime()));
                    clearReminderButton.setVisibility(View.VISIBLE);
                },
                reminderCalendar.get(Calendar.HOUR_OF_DAY),
                reminderCalendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void clearDueDate() {
        hasDueDate = false;
        dueDateText.setText("No due date set");
        clearDueDateButton.setVisibility(View.GONE);
    }

    private void clearReminder() {
        // Cancel existing notification
        NotificationHelper.cancelNotification(this, currentAssignment.getId());

        hasReminder = false;
        reminderText.setText("No reminder set");
        clearReminderButton.setVisibility(View.GONE);
    }

    private void saveAssignment() {
        String assignmentName = assignmentNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(assignmentName)) {
            assignmentNameEditText.setError("Please enter an assignment name");
            return;
        }

        // Get selected folder
        Folder selectedFolder = (Folder) folderSpinner.getSelectedItem();
        if (selectedFolder == null) {
            Toast.makeText(this, "Please select a folder", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cancel old notification if it exists
        if (currentAssignment.getReminder() != null && !currentAssignment.getReminder().isEmpty()) {
            NotificationHelper.cancelNotification(this, currentAssignment.getId());
        }

        // Update assignment data
        currentAssignment.setName(assignmentName);
        currentAssignment.setFolderId(selectedFolder.getId());
        currentAssignment.setDueDate(hasDueDate ? fullFormat.format(dueDateCalendar.getTime()) : null);
        currentAssignment.setReminder(hasReminder ? fullFormat.format(reminderCalendar.getTime()) : null);

        // Update in database
        int rowsUpdated = dbHelper.updateList(currentAssignment);

        if (rowsUpdated > 0) {
            // Schedule new notification if reminder is set
            if (hasReminder) {
                NotificationHelper.scheduleNotification(this, currentAssignment);
            }

            Toast.makeText(this, "Assignment updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update assignment", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Assignment")
                .setMessage("Are you sure you want to delete this assignment? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAssignment())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAssignment() {
        // Cancel notification if it exists
        if (currentAssignment.getReminder() != null && !currentAssignment.getReminder().isEmpty()) {
            NotificationHelper.cancelNotification(this, currentAssignment.getId());
        }

        // Delete from database
        dbHelper.deleteList(currentAssignment.getId());

        Toast.makeText(this, "Assignment deleted", Toast.LENGTH_SHORT).show();
        finish();
    }
}