package com.example.edulist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class MainActivity extends AppCompatActivity implements AssignmentAdapter.OnAssignmentActionListener, SwipeToDeleteCallback.SwipeActionListener {

    private static final String TAG = "MainActivity";
    private RecyclerView workListView;
    private TextView titleTextView;
    private Button addFolderButton;
    private AssignmentAdapter assignmentAdapter;
    private DatabaseHelper dbHelper;
    private List<EduList> eduLists;

    private long currentFolderId = -1;
    private String currentFolderName = "All Education Lists";

    private ActivityResultLauncher<Intent> newListLauncher;
    private ActivityResultLauncher<Intent> viewFoldersLauncher;

    private TextView dueSoonHeaderView;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: MainActivity created");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        workListView = findViewById(R.id.workLists);
        workListView.setLayoutManager(new LinearLayoutManager(this));

        titleTextView = findViewById(R.id.title);
        addFolderButton = findViewById(R.id.addFolderButton);
        dueSoonHeaderView = findViewById(R.id.dueSoonHeader);

        // Setup swipe functionality
        SwipeToDeleteCallback swipeCallback = new SwipeToDeleteCallback(this, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);

        // Check if we're opening a specific folder or showing all lists
        if (getIntent().hasExtra("FOLDER_ID")) {
            currentFolderId = getIntent().getLongExtra("FOLDER_ID", -1);
            currentFolderName = getIntent().getStringExtra("FOLDER_NAME");
            titleTextView.setText(currentFolderName);
            addFolderButton.setText("New List");
        } else {
            // Show all lists from all folders
            currentFolderId = -1;
            titleTextView.setText("All Education Lists");
            addFolderButton.setText("View Folders");
        }

        newListLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Just refresh the list since NewListActivity already created the list
                        loadLists();
                    }
                }
        );


        viewFoldersLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    loadLists();
                }
        );

        loadLists();

        // Attach swipe helper to RecyclerView
        itemTouchHelper.attachToRecyclerView(workListView);

        addFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFolderId != -1) {
                    // We're in a specific folder, add a new list to it
                    Intent intent = new Intent(MainActivity.this, NewListActivity.class);
                    intent.putExtra(NewListActivity.EXTRA_FOLDER_ID, currentFolderId);
                    newListLauncher.launch(intent);
                } else {
                    // We're in the main view, go to folders
                    Intent intent = new Intent(MainActivity.this, FoldersActivity.class);
                    startActivity(intent);
                }
            }
        });

        if (dueSoonHeaderView != null) {
            if (eduLists != null && !eduLists.isEmpty()) {
                dueSoonHeaderView.setVisibility(View.VISIBLE);
            } else {
                dueSoonHeaderView.setVisibility(View.GONE);
            }
        }

//        Button testButton = findViewById(R.id.testButton);
//        if (testButton != null) {
//            testButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    setupTestReminder();
//                }
//            });
//        }
    }

    // AssignmentAdapter.OnAssignmentActionListener implementation
    @Override
    public void onAssignmentClick(EduList assignment) {
        Toast.makeText(this, "Selected: " + assignment.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAssignmentDelete(EduList assignment) {
        // This will be called from swipe - handled in SwipeActionListener
    }

    @Override
    public void onAssignmentComplete(EduList assignment) {
        // This will be called from swipe - handled in SwipeActionListener
    }

    // SwipeToDeleteCallback.SwipeActionListener implementation
    @Override
    public void onDelete(int position) {
        EduList assignment = assignmentAdapter.getItem(position);
        if (assignment == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Assignment")
                .setMessage("Are you sure you want to delete '" + assignment.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Cancel notification if it exists
                    if (assignment.getReminder() != null && !assignment.getReminder().isEmpty()) {
                        NotificationHelper.cancelNotification(this, assignment.getId());
                    }

                    // Delete from database
                    dbHelper.deleteList(assignment.getId());

                    // Remove from adapter
                    assignmentAdapter.removeItem(position);

                    Toast.makeText(this, "Assignment deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Restore the item
                    assignmentAdapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> {
                    // Restore the item if dialog is canceled
                    assignmentAdapter.notifyItemChanged(position);
                })
                .show();
    }

    @Override
    public void onComplete(int position) {
        EduList assignment = assignmentAdapter.getItem(position);
        if (assignment == null) return;

        // Toggle completion status
        assignment.setCompleted(!assignment.isCompleted());

        // Update in database
        dbHelper.updateList(assignment);

        // Refresh the list to reorder items (completed items go to bottom)
        loadLists();

        String message = assignment.isCompleted() ? "Assignment completed!" : "Assignment marked as incomplete";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupTestReminder() {
        // Create a test list with a reminder 3 seconds from now
        Calendar testReminder = Calendar.getInstance();
        testReminder.add(Calendar.SECOND, 3);

        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String reminderTime = fullFormat.format(testReminder.getTime());

        // Create a test list
        EduList testList = new EduList("Test Reminder", currentFolderId, null, reminderTime);

        // Save the list to database
        long listId = dbHelper.addList(testList);
        if (listId != -1) {
            testList.setId(listId);

            // Schedule notification
            NotificationHelper.scheduleNotification(this, testList);
            Toast.makeText(this, "Test notification scheduled for 3 seconds from now", Toast.LENGTH_LONG).show();

            // Refresh the list view
            loadLists();
        } else {
            Toast.makeText(this, "Failed to create test reminder", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLists() {
        if (currentFolderId != -1) {
            // Load lists for specific folder
            eduLists = dbHelper.getAllListsByFolderSortedByDueDate(currentFolderId);
            Log.d(TAG, "Loaded " + eduLists.size() + " lists for folder " + currentFolderId);
        } else {
            // Load all lists from all folders
            eduLists = dbHelper.getAllListsSortedByDueDate();
            Log.d(TAG, "Loaded " + eduLists.size() + " lists from all folders");
        }

        if (assignmentAdapter == null) {
            assignmentAdapter = new AssignmentAdapter(this, eduLists, this);
            workListView.setAdapter(assignmentAdapter);
        } else {
            assignmentAdapter.updateAssignments(eduLists);
        }

        // Update header visibility
        if (dueSoonHeaderView != null) {
            dueSoonHeaderView.setVisibility(eduLists.isEmpty() ? View.GONE : View.VISIBLE);
        }

        // Show a message if no folders exist
        if (currentFolderId == -1 && dbHelper.getFoldersCount() == 0) {
            Toast.makeText(this, "No folders yet. Click 'View Folders' to create one!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        loadLists();
    }
}