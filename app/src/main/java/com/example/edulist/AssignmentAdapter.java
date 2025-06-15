package com.example.edulist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    private Context context;
    private List<EduList> assignments;
    private OnAssignmentActionListener listener;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    // For double-click detection
    private static final int DOUBLE_CLICK_TIME_DELTA = 300; // milliseconds
    private long lastClickTime = 0;

    public interface OnAssignmentActionListener {
        void onAssignmentClick(EduList assignment);
        void onAssignmentDelete(EduList assignment);
        void onAssignmentComplete(EduList assignment);
    }

    public AssignmentAdapter(Context context, List<EduList> assignments, OnAssignmentActionListener listener) {
        this.context = context;
        this.assignments = assignments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        EduList assignment = assignments.get(position);

        // ALWAYS show assignment name with DEFAULT BLACK COLOR FIRST
        holder.listName.setText(assignment.getName());
        holder.listName.setVisibility(View.VISIBLE);
        holder.listName.setTextColor(Color.parseColor("#FFFFFF")); // Default black

        // Set completion status visual effects
        if (assignment.isCompleted()) {
            holder.listName.setPaintFlags(holder.listName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.listName.setAlpha(0.6f);
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.listName.setPaintFlags(holder.listName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.listName.setAlpha(1.0f);
            holder.itemView.setAlpha(1.0f);
        }

        // ALWAYS show due date if it exists
        // Show the horizontal container if we have either due date OR reminder
        boolean hasDueDate = assignment.getDueDate() != null && !assignment.getDueDate().isEmpty();
        boolean hasReminder = assignment.getReminder() != null && !assignment.getReminder().isEmpty();

// Find the parent LinearLayout that contains both due_date and reminder_date
        View dueDateContainer = holder.dueDateText.getParent() instanceof View ? (View) holder.dueDateText.getParent() : null;
        if (dueDateContainer != null) {
            dueDateContainer.setVisibility(hasDueDate || hasReminder ? View.VISIBLE : View.GONE);
        }

// ALWAYS show due date if it exists
        if (hasDueDate) {
            try {
                Date dueDate = inputFormat.parse(assignment.getDueDate());
                if (dueDate != null) {
                    // Due:
                    holder.dueDateText.setText(outputFormat.format(dueDate));
                    holder.dueDateText.setVisibility(View.VISIBLE);

                    // Color coding for overdue assignments (only if not completed)
                    if (!assignment.isCompleted()) {
                        Calendar today = Calendar.getInstance();
                        Calendar dueCal = Calendar.getInstance();
                        dueCal.setTime(dueDate);

                        // Set both to start of day for comparison
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MILLISECOND, 0);

                        dueCal.set(Calendar.HOUR_OF_DAY, 0);
                        dueCal.set(Calendar.MINUTE, 0);
                        dueCal.set(Calendar.SECOND, 0);
                        dueCal.set(Calendar.MILLISECOND, 0);

                        if (dueCal.before(today)) {
                            holder.dueDateText.setTextColor(Color.parseColor("#F44336")); // Red for overdue
                            holder.listName.setTextColor(Color.parseColor("#F44336"));
                        } else if (dueCal.equals(today)) {
                            holder.dueDateText.setTextColor(Color.parseColor("#FF9800")); // Orange for due today
                            holder.listName.setTextColor(Color.parseColor("#FF9800"));
                        } else {
                            holder.dueDateText.setTextColor(Color.parseColor("#AFEFDF")); // Dark gray for future
                            // Keep assignment name black for future dates
                        }
                    } else {
                        // For completed items, keep muted colors
                        holder.dueDateText.setTextColor(Color.parseColor("#999999"));
                    }
                }
            } catch (ParseException e) {
                holder.dueDateText.setVisibility(View.GONE);
                // Keep assignment name black on parse error
            }
        } else {
            holder.dueDateText.setVisibility(View.GONE);
            // Keep assignment name black when no due date
        }

        // ALWAYS show reminder icon and date if reminder exists
        if (assignment.getReminder() != null && !assignment.getReminder().isEmpty()) {
            holder.reminderIcon.setVisibility(View.VISIBLE);
            holder.reminderDate.setVisibility(View.VISIBLE);

            // Format reminder date for display
            try {
                Date reminderDate = inputFormat.parse(assignment.getReminder());
                if (reminderDate != null) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.US);
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mma", Locale.US);
                    String dayText = dayFormat.format(reminderDate);
                    String monthText = monthFormat.format(reminderDate).toUpperCase();
                    String timeText = timeFormat.format(reminderDate);
                    holder.reminderDate.setText(dayText + " " + monthText + " " + timeText);
                }
            } catch (ParseException e) {
                holder.reminderDate.setVisibility(View.GONE);
            }

            if (assignment.isCompleted()) {
                holder.reminderIcon.setAlpha(0.6f);
                holder.reminderDate.setAlpha(0.6f);
            } else {
                holder.reminderIcon.setAlpha(1.0f);
                holder.reminderDate.setAlpha(1.0f);
            }
        } else {
            holder.reminderIcon.setVisibility(View.GONE);
            holder.reminderDate.setVisibility(View.GONE);
        }

        // Handle click events for double-click detection
        holder.itemView.setOnClickListener(v -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                // Double click - open edit activity
                Intent intent = new Intent(context, NewListActivity.class);
                intent.putExtra("EDIT_MODE", true);
                intent.putExtra("LIST_ID", assignment.getId());
                intent.putExtra("LIST_NAME", assignment.getName());
                intent.putExtra("FOLDER_ID", assignment.getFolderId());
                intent.putExtra("DUE_DATE", assignment.getDueDate());
                intent.putExtra("REMINDER", assignment.getReminder());
                context.startActivity(intent);
            } else {
                // Single click - call listener
                if (listener != null) {
                    listener.onAssignmentClick(assignment);
                }
            }
            lastClickTime = clickTime;
        });
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    public void updateAssignments(List<EduList> newAssignments) {
        this.assignments.clear();
        this.assignments.addAll(newAssignments);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < assignments.size()) {
            assignments.remove(position);
            notifyItemRemoved(position);
        }
    }

    public EduList getItem(int position) {
        if (position >= 0 && position < assignments.size()) {
            return assignments.get(position);
        }
        return null;
    }

    public static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        TextView listName;
        TextView dueDateText;
        TextView reminderDate;
        ImageView reminderIcon;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            listName = itemView.findViewById(R.id.list_name);
            dueDateText = itemView.findViewById(R.id.due_date);
            reminderDate = itemView.findViewById(R.id.reminder_date);
            reminderIcon = itemView.findViewById(R.id.reminder_icon);
        }
    }
}