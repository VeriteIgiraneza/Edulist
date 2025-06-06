package com.example.edulist;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ListAdapter extends ArrayAdapter<EduList> {

    private Context context;
    private List<EduList> lists;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private boolean showFolderNames;

    public ListAdapter(Context context, List<EduList> lists, boolean showFolderNames) {
        super(context, R.layout.item_list, lists);
        this.context = context;
        this.lists = lists;
        this.showFolderNames = showFolderNames;
    }

    public ListAdapter(Context context, List<EduList> lists) {
        this(context, lists, false);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        }

        EduList currentList = lists.get(position);

        TextView listName = listItem.findViewById(R.id.list_name);
        TextView dueDateText = listItem.findViewById(R.id.due_date);
        TextView folderNameText = listItem.findViewById(R.id.folder_name);
        ImageView reminderIcon = listItem.findViewById(R.id.reminder_icon);

        listName.setText(currentList.getName());

        // Show folder name if required and available
        if (showFolderNames && folderNameText != null && currentList.getFolderName() != null) {
            folderNameText.setText("In: " + currentList.getFolderName());
            folderNameText.setVisibility(View.VISIBLE);
        } else if (folderNameText != null) {
            folderNameText.setVisibility(View.GONE);
        }

        // Reset text styles and colors
        dueDateText.setTextColor(Color.BLACK);
        dueDateText.setTypeface(Typeface.DEFAULT);

        if (currentList.getDueDate() != null && !currentList.getDueDate().isEmpty()) {
            try {
                Date dueDate = inputFormat.parse(currentList.getDueDate());
                Date currentDate = new Date();

                // Calculate days until due
                long diffInMillies = dueDate.getTime() - currentDate.getTime();
                long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                // Format the due date text
                String dueDateString = "Due: " + outputFormat.format(dueDate);

                // Apply different formatting based on urgency
                if (diffInDays < 0) {
                    // Overdue
                    dueDateString += " (OVERDUE)";
                    dueDateText.setTextColor(Color.RED);
                    dueDateText.setTypeface(Typeface.DEFAULT_BOLD);
                } else if (diffInDays <= 3) {
                    // Due soon (within 3 days)
                    dueDateString += diffInDays == 0 ? " (TODAY)" :
                            diffInDays == 1 ? " (TOMORROW)" :
                                    " (IN " + diffInDays + " DAYS)";
                    dueDateText.setTextColor(Color.parseColor("#FF5722")); // Deep Orange
                    dueDateText.setTypeface(Typeface.DEFAULT_BOLD);
                }

                dueDateText.setText(dueDateString);
                dueDateText.setVisibility(View.VISIBLE);

            } catch (ParseException e) {
                dueDateText.setVisibility(View.GONE);
            }
        } else {
            dueDateText.setVisibility(View.GONE);
        }

        if (currentList.getReminder() != null && !currentList.getReminder().isEmpty()) {
            reminderIcon.setVisibility(View.VISIBLE);
        } else {
            reminderIcon.setVisibility(View.GONE);
        }

        return listItem;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    public void updateLists(List<EduList> newLists) {
        this.lists.clear();
        this.lists.addAll(newLists);
        notifyDataSetChanged();
    }
}