package com.example.edulist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NewFolderActivity extends AppCompatActivity {

    private static final String TAG = "NewFolderActivity";
    private EditText folderNameEditText;
    private Button doneButton;
    private DatabaseHelper dbHelper;
    // Default color for all folders
    private final int defaultFolderColor = Color.parseColor("#03A9F4");  // Light Blue

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_newfolder);

        Log.d(TAG, "onCreate: Starting with XML layout");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views from XML
        folderNameEditText = findViewById(R.id.foldername);
        doneButton = findViewById(R.id.done);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Set click listener for the done button
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFolderAndFinish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }

    private void saveFolderAndFinish() {
        try {
            String folderName = folderNameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(folderName)) {
                folderNameEditText.setError("Please enter a folder name");
                return;
            }

            // Create and save the folder directly with default color
            Folder newFolder = new Folder(folderName, defaultFolderColor);
            long folderId = dbHelper.addFolder(newFolder);

            if (folderId != -1) {
                newFolder.setId(folderId);

                // Return the folder ID and name in case the parent activity needs it
                Intent resultIntent = new Intent();
                resultIntent.putExtra("FOLDER_ID", folderId);
                resultIntent.putExtra("FOLDER_NAME", folderName);
                setResult(RESULT_OK, resultIntent);

                Toast.makeText(this, "Folder created: " + folderName, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Folder created with ID: " + folderId);
                finish();
            } else {
                Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to create folder");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving folder: " + e.getMessage());
            Toast.makeText(this, "Error saving folder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}