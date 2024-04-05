package com.example.semsemgallery.activities.base;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecylerViewItemDecoration extends RecyclerView.ItemDecoration {
    private int spacing;

    public RecylerViewItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = spacing;
        outRect.right = spacing;
        outRect.bottom = spacing;

        // Add top spacing only for the first row to avoid double spacing between items
        if (parent.getChildLayoutPosition(view) < ((GridLayoutManager) parent.getLayoutManager()).getSpanCount()) {
            outRect.top = spacing;
        } else {
            outRect.top = 0;
        }
    }
}
