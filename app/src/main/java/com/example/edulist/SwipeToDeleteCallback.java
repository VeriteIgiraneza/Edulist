package com.example.edulist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private Paint paint;
    private Drawable deleteIcon;
    private Drawable completeIcon;
    private int intrinsicWidth;
    private int intrinsicHeight;
    private Context context;
    private SwipeActionListener listener;

    public interface SwipeActionListener {
        void onDelete(int position);
        void onComplete(int position);
    }

    public SwipeToDeleteCallback(Context context, SwipeActionListener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.listener = listener;

        paint = new Paint();
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        completeIcon = ContextCompat.getDrawable(context, R.drawable.ic_check);

        if (deleteIcon != null) {
            intrinsicWidth = deleteIcon.getIntrinsicWidth();
            intrinsicHeight = deleteIcon.getIntrinsicHeight();
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        if (direction == ItemTouchHelper.LEFT) {
            // Swipe left to delete
            listener.onDelete(position);
        } else if (direction == ItemTouchHelper.RIGHT) {
            // Swipe right to complete
            listener.onComplete(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;
            float height = (float) itemView.getBottom() - (float) itemView.getTop();
            float width = height / 3;

            if (dX > 0) {
                // Swipe right - Complete (green background)
                paint.setColor(Color.parseColor("#4CAF50"));
                RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                c.drawRect(background, paint);

                // Draw complete icon
                if (completeIcon != null) {
                    int iconMargin = (int) ((height - intrinsicHeight) / 2);
                    int iconTop = itemView.getTop() + (int) ((height - intrinsicHeight) / 2);
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = itemView.getLeft() + iconMargin + intrinsicWidth;
                    int iconBottom = iconTop + intrinsicHeight;

                    completeIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    completeIcon.draw(c);
                }
            } else {
                // Swipe left - Delete (red background)
                paint.setColor(Color.parseColor("#f44336"));
                RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                c.drawRect(background, paint);

                // Draw delete icon
                if (deleteIcon != null) {
                    int iconMargin = (int) ((height - intrinsicHeight) / 2);
                    int iconTop = itemView.getTop() + (int) ((height - intrinsicHeight) / 2);
                    int iconLeft = itemView.getRight() - iconMargin - intrinsicWidth;
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconBottom = iconTop + intrinsicHeight;

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}