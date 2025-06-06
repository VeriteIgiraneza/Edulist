package com.example.edulist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoldersActivity extends AppCompatActivity {

    private static final String TAG = "FoldersActivity";
    private RecyclerView foldersRecyclerView;
    private Button addFolderButton;
    private TextView folderNameTextView;
    private FolderRecyclerAdapter folderAdapter;
    private DatabaseHelper dbHelper;
    private List<Folder> folderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders);
        Log.d(TAG, "onCreate: FoldersActivity created");

        dbHelper = new DatabaseHelper(this);

        foldersRecyclerView = findViewById(R.id.viewFolders);
        foldersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        addFolderButton = findViewById(R.id.newFolderButton);
        folderNameTextView = findViewById(R.id.folderName);

        // Set a clear title
        folderNameTextView.setText("My Folders");

        loadFolders();

        // Super direct approach for New Folder button
        // Simpler, more reliable approach for New Folder button
        addFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoldersActivity.this, NewFolderActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadFolders() {
        folderList = dbHelper.getAllFolders();
        Log.d(TAG, "loadFolders: found " + folderList.size() + " folders");

        if (folderList.isEmpty()) {
            Folder defaultFolder = new Folder("Edu Folder", android.graphics.Color.parseColor("#BFBEBE"));
            long id = dbHelper.addFolder(defaultFolder);
            defaultFolder.setId(id);
            folderList.add(defaultFolder);
        }

        if (folderAdapter == null) {
            folderAdapter = new FolderRecyclerAdapter(this, folderList, new FolderRecyclerAdapter.OnFolderClickListener() {
                @Override
                public void onFolderClick(Folder folder) {
                    Log.d(TAG, "Selected folder: " + folder.getName());

                    // Launch MainActivity with selected folder's ID
                    Intent intent = new Intent(FoldersActivity.this, MainActivity.class);
                    intent.putExtra("FOLDER_ID", folder.getId());
                    intent.putExtra("FOLDER_NAME", folder.getName());
                    startActivity(intent);
                }
            });
            foldersRecyclerView.setAdapter(folderAdapter);
        } else {
            folderAdapter.updateFolders(folderList);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        loadFolders();
    }
}