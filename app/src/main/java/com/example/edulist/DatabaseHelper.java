package com.example.edulist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "edulist_db";

    private static final String TABLE_FOLDERS = "folders";
    private static final String TABLE_LISTS = "lists";

    private static final String KEY_ID = "id";

    private static final String KEY_FOLDER_NAME = "name";
    private static final String KEY_FOLDER_COLOR = "color";

    private static final String KEY_LIST_NAME = "name";
    private static final String KEY_FOLDER_ID = "folder_id";
    private static final String KEY_DUE_DATE = "due_date";
    private static final String KEY_REMINDER = "reminder";
    private static final String KEY_COMPLETED = "completed";

    private static final String CREATE_FOLDERS_TABLE = "CREATE TABLE " + TABLE_FOLDERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_FOLDER_NAME + " TEXT,"
            + KEY_FOLDER_COLOR + " INTEGER"
            + ")";

    private static final String CREATE_LISTS_TABLE = "CREATE TABLE " + TABLE_LISTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_LIST_NAME + " TEXT,"
            + KEY_FOLDER_ID + " INTEGER,"
            + KEY_DUE_DATE + " TEXT,"
            + KEY_REMINDER + " TEXT,"
            + KEY_COMPLETED + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + KEY_FOLDER_ID + ") REFERENCES " + TABLE_FOLDERS + "(" + KEY_ID + ")"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FOLDERS_TABLE);
        db.execSQL(CREATE_LISTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add completed column to lists table
            db.execSQL("ALTER TABLE " + TABLE_LISTS + " ADD COLUMN " + KEY_COMPLETED + " INTEGER DEFAULT 0");
        }
        // Keep the old upgrade behavior as a fallback if needed
        else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDERS);
            onCreate(db);
        }
    }

    public long addFolder(Folder folder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_FOLDER_NAME, folder.getName());
        values.put(KEY_FOLDER_COLOR, folder.getColor());

        long id = db.insert(TABLE_FOLDERS, null, values);
        db.close();
        return id;
    }

    public long addList(EduList list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_LIST_NAME, list.getName());
        values.put(KEY_FOLDER_ID, list.getFolderId());
        values.put(KEY_DUE_DATE, list.getDueDate());
        values.put(KEY_REMINDER, list.getReminder());
        values.put(KEY_COMPLETED, list.isCompleted() ? 1 : 0);

        long id = db.insert(TABLE_LISTS, null, values);
        db.close();
        return id;
    }

    public Folder getFolder(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_FOLDERS,
                new String[]{KEY_ID, KEY_FOLDER_NAME, KEY_FOLDER_COLOR},
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            Folder folder = new Folder(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getInt(2)
            );
            cursor.close();
            return folder;
        }

        if (cursor != null) {
            cursor.close();
        }

        return null;
    }

    public List<Folder> getAllFolders() {
        List<Folder> folderList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_FOLDERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Folder folder = new Folder();
                folder.setId(cursor.getLong(0));
                folder.setName(cursor.getString(1));
                folder.setColor(cursor.getInt(2));

                folderList.add(folder);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return folderList;
    }

    public int updateFolder(Folder folder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_FOLDER_NAME, folder.getName());
        values.put(KEY_FOLDER_COLOR, folder.getColor());

        return db.update(
                TABLE_FOLDERS,
                values,
                KEY_ID + " = ?",
                new String[]{String.valueOf(folder.getId())}
        );
    }

    public void deleteFolder(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOLDERS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public int getFoldersCount() {
        String countQuery = "SELECT * FROM " + TABLE_FOLDERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public EduList getList(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_LISTS,
                new String[]{KEY_ID, KEY_LIST_NAME, KEY_FOLDER_ID, KEY_DUE_DATE, KEY_REMINDER, KEY_COMPLETED},
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            EduList list = new EduList(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getLong(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5) == 1  // completion status
            );
            cursor.close();
            return list;
        }

        if (cursor != null) {
            cursor.close();
        }

        return null;
    }

    public List<EduList> getAllListsByFolderSortedByDueDate(long folderId) {
        List<EduList> listItems = new ArrayList<>();

        // Order by completion first (incomplete items first), then by due date
        String selectQuery = "SELECT * FROM " + TABLE_LISTS +
                " WHERE " + KEY_FOLDER_ID + " = " + folderId +
                " ORDER BY " + KEY_COMPLETED + " ASC, " +
                "CASE WHEN " + KEY_DUE_DATE + " IS NULL OR " + KEY_DUE_DATE + " = '' THEN 1 ELSE 0 END, " +
                KEY_DUE_DATE + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                EduList list = new EduList();
                list.setId(cursor.getLong(0));
                list.setName(cursor.getString(1));
                list.setFolderId(cursor.getLong(2));
                list.setDueDate(cursor.getString(3));
                list.setReminder(cursor.getString(4));
                list.setCompleted(cursor.getInt(5) == 1);

                listItems.add(list);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return listItems;
    }

    public List<EduList> getAllListsByFolder(long folderId) {
        List<EduList> listItems = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_LISTS +
                " WHERE " + KEY_FOLDER_ID + " = " + folderId;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                EduList list = new EduList();
                list.setId(cursor.getLong(0));
                list.setName(cursor.getString(1));
                list.setFolderId(cursor.getLong(2));
                list.setDueDate(cursor.getString(3));
                list.setReminder(cursor.getString(4));
                list.setCompleted(cursor.getInt(5) == 1);

                listItems.add(list);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return listItems;
    }

    /**
     * Get all lists from all folders, sorted by completion status then due date
     * @return List of all EduList items
     */
    public List<EduList> getAllListsSortedByDueDate() {
        List<EduList> listItems = new ArrayList<>();

        // Order by completion first (incomplete items first), then by due date
        String selectQuery = "SELECT " + TABLE_LISTS + "." + KEY_ID + ", " +
                TABLE_LISTS + "." + KEY_LIST_NAME + ", " +
                TABLE_LISTS + "." + KEY_FOLDER_ID + ", " +
                TABLE_LISTS + "." + KEY_DUE_DATE + ", " +
                TABLE_LISTS + "." + KEY_REMINDER + ", " +
                TABLE_LISTS + "." + KEY_COMPLETED + ", " +
                TABLE_FOLDERS + "." + KEY_FOLDER_NAME +
                " FROM " + TABLE_LISTS +
                " LEFT JOIN " + TABLE_FOLDERS +
                " ON " + TABLE_LISTS + "." + KEY_FOLDER_ID + " = " + TABLE_FOLDERS + "." + KEY_ID +
                " ORDER BY " + TABLE_LISTS + "." + KEY_COMPLETED + " ASC, " +
                "CASE WHEN " + TABLE_LISTS + "." + KEY_DUE_DATE + " IS NULL OR " + TABLE_LISTS + "." + KEY_DUE_DATE + " = '' THEN 1 ELSE 0 END, " +
                TABLE_LISTS + "." + KEY_DUE_DATE + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                EduList list = new EduList();
                list.setId(cursor.getLong(0));
                list.setName(cursor.getString(1));
                list.setFolderId(cursor.getLong(2));
                list.setDueDate(cursor.getString(3));
                list.setReminder(cursor.getString(4));
                list.setCompleted(cursor.getInt(5) == 1);
                // Store folder name in the list object
                if (cursor.getString(6) != null) {
                    list.setFolderName(cursor.getString(6));
                }

                listItems.add(list);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return listItems;
    }

    public List<EduList> getAllListsWithReminders() {
        List<EduList> listItems = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_LISTS +
                " WHERE " + KEY_REMINDER + " IS NOT NULL AND " +
                KEY_REMINDER + " != ''";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                EduList list = new EduList();
                list.setId(cursor.getLong(0));
                list.setName(cursor.getString(1));
                list.setFolderId(cursor.getLong(2));
                list.setDueDate(cursor.getString(3));
                list.setReminder(cursor.getString(4));
                list.setCompleted(cursor.getInt(5) == 1);

                listItems.add(list);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return listItems;
    }

    public int updateList(EduList list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_LIST_NAME, list.getName());
        values.put(KEY_FOLDER_ID, list.getFolderId());
        values.put(KEY_DUE_DATE, list.getDueDate());
        values.put(KEY_REMINDER, list.getReminder());
        values.put(KEY_COMPLETED, list.isCompleted() ? 1 : 0);

        // Update row
        return db.update(
                TABLE_LISTS,
                values,
                KEY_ID + " = ?",
                new String[]{String.valueOf(list.getId())}
        );
    }

    public void deleteList(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LISTS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}