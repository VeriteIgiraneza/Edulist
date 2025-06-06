package com.example.edulist;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    private Context context;
    private List<EduList> assignments;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, MMM dd", Locale.US);
    private OnAssignmentClickListener listener;

    public interface OnAssignmentClickListener {
        void onAssignmentClick(EduList assignment);
    }

    public AssignmentAdapter(Context context, List<EduList> assignments, OnAssignmentClickListener listener) {
        this.context = context;
        this.assignments = assignments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        EduList assignment = assignments.get(position);

        holder.assignmentTitle.setText(assignment.getName());

        if (assignment.getDueDate() != null && !assignment.getDueDate().isEmpty()) {
            try {
                Date dueDate = inputFormat.parse(assignment.getDueDate());
                holder.assignmentDate.setText(outputFormat.format(dueDate));
                holder.assignmentDate.setVisibility(View.VISIBLE);
            } catch (ParseException e) {
                holder.assignmentDate.setVisibility(View.GONE);
            }
        } else {
            holder.assignmentDate.setVisibility(View.GONE);
        }

        // Use a consistent color for all assignment cards
        holder.cardBackground.setBackgroundColor(Color.parseColor("#353434")); // Material Blue color

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAssignmentClick(assignment);
            }
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

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        TextView assignmentTitle;
        TextView assignmentDate;
        View cardBackground;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            assignmentTitle = itemView.findViewById(R.id.assignmentTitle);
            assignmentDate = itemView.findViewById(R.id.assignmentDate);
            cardBackground = itemView.findViewById(R.id.cardBackground);
        }
    }
}