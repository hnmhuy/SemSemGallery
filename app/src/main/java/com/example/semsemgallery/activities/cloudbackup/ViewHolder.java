package com.example.semsemgallery.activities.cloudbackup;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.base.SelectableItem;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.models.TrashedPicture;

public class ViewHolder extends SelectableItem<Picture> {
    public ImageView thumbnail;
    public TextView remainingDate;

    public ViewHolder(@NonNull View itemView, ObservableGridMode<Picture> observedObj) {
        super(itemView, observedObj);
        thumbnail = itemView.findViewById(R.id.thumbnail);
        remainingDate = itemView.findViewById(R.id.remaining);
        if (observedObj.getCurrentMode() == GridMode.SELECTING) {
            selector.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void clickOnNormalMode(View v) {
        return;
    }
}
