package com.example.semsemgallery.activities.main2.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.base.SelectableItem;
import com.example.semsemgallery.models.Album;

public class AlbumViewHolder extends SelectableItem<Album> {

    public ImageView thumbnail;
    public TextView albumName;
    public TextView albumQuantity;

    public AlbumViewHolder(@NonNull View itemView, ObservableGridMode<Album> observedObj) {
        super(itemView, observedObj);
        thumbnail = itemView.findViewById(R.id.component_album_item_image);
        albumName = itemView.findViewById(R.id.component_album_item_name);
        albumQuantity = itemView.findViewById(R.id.component_album_item_quantity);
    }

    @Override
    public void clickOnNormalMode(View v) {

    }
}
