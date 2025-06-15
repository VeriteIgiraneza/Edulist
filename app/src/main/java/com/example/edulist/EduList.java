package com.example.edulist;

public class EduList {
    private long id;
    private String name;
    private long folderId;
    private String dueDate;
    private String reminder;
    private String folderName; // Added to store folder name
    private boolean completed;

    public EduList() {
        this.completed = false; // Default to not completed
    }

    public EduList(String name, long folderId, String dueDate, String reminder) {
        this.name = name;
        this.folderId = folderId;
        this.dueDate = dueDate;
        this.reminder = reminder;
        this.completed = false; // Default to not completed
    }

    public EduList(long id, String name, long folderId, String dueDate, String reminder) {
        this.id = id;
        this.name = name;
        this.folderId = folderId;
        this.dueDate = dueDate;
        this.reminder = reminder;
        this.completed = false; // Default to not completed
    }

    // New constructor with completion status
    public EduList(long id, String name, long folderId, String dueDate, String reminder, boolean completed) {
        this.id = id;
        this.name = name;
        this.folderId = folderId;
        this.dueDate = dueDate;
        this.reminder = reminder;
        this.completed = completed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFolderId() {
        return folderId;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return name;
    }
}