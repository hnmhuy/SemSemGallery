package com.example.semsemgallery.activities.main.viewholders;

import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.base.SelectableItem;

public class GalleryItemViewHolder extends SelectableItem<GalleryItem> {

    private int type;
    public ImageView thumnail = null;
    public TextView groupDisplayText = null;

    public GalleryItemViewHolder(@NonNull View itemView, ObservableGridMode<GalleryItem> observedObj, int type) {
        super(itemView, observedObj);
        this.type = type;
        if (type == GalleryItem.THUMBNAIL) {
            thumnail = itemView.findViewById(R.id.gallery_item);
        } else if (type == GalleryItem.GROUPDATE) {
            groupDisplayText = itemView.findViewById(R.id.group_date);
        }

    }

    @Override
    public void clickOnNormalMode(View v) {

    }
}
