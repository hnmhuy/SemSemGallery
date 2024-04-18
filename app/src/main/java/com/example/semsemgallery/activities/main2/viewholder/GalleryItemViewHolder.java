package com.example.semsemgallery.activities.main2.viewholder;

import static com.example.semsemgallery.activities.base.GridMode.SELECTING;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.GridModeEvent;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.base.SelectableItem;

public abstract class GalleryItemViewHolder extends SelectableItem<GalleryItem> {

    private int type;
    public ImageView thumnail = null;
    public TextView groupDisplayText = null;
    public ImageView isFav = null;

    public GalleryItemViewHolder(@NonNull View itemView, ObservableGridMode<GalleryItem> observedObj, int type) {
        super(itemView, observedObj);
        this.type = type;
        isFav = itemView.findViewById(R.id.isFav);
        if (type == GalleryItem.THUMBNAIL) {
            thumnail = itemView.findViewById(R.id.gallery_item);
        } else if (type == GalleryItem.GROUPDATE) {
            groupDisplayText = itemView.findViewById(R.id.group_date);
        }

    }

    @Override
    public void clickOnSelectingMode(View v) {
        super.clickOnSelectingMode(v);
    }

    @Override
    public void onModeChange(GridModeEvent event) {
        if (event.getGridMode() == SELECTING) {
            if (type == GalleryItem.GROUPDATE) {
                selector.setVisibility(View.INVISIBLE);
            } else if (type == GalleryItem.THUMBNAIL) {
                selector.setVisibility(View.VISIBLE);
            }
        } else if (event.getGridMode() == GridMode.NORMAL) selector.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSelectingAll(GridModeEvent event) {
        super.onSelectingAll(event);
        if (event.getGridMode() == SELECTING) {
            if (type == GalleryItem.GROUPDATE) {
                selector.setVisibility(View.INVISIBLE);
            } else if (type == GalleryItem.THUMBNAIL) {
                selector.setVisibility(View.VISIBLE);
            }
        } else if (event.getGridMode() == GridMode.NORMAL) selector.setVisibility(View.INVISIBLE);
    }
}
