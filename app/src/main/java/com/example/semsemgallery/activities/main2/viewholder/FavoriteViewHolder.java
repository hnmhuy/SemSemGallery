package com.example.semsemgallery.activities.main2.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.base.SelectableItem;
import com.example.semsemgallery.models.Picture;

import java.util.List;

public class FavoriteViewHolder extends SelectableItem<Picture> {
    public ImageView heart;
    public ImageView thumbnail;

    public FavoriteViewHolder(@NonNull View itemView, ObservableGridMode<Picture> observedObj) {
        super(itemView, observedObj);
        this.heart = itemView.findViewById(R.id.isFav);
        this.thumbnail = itemView.findViewById(R.id.gallery_item);
    }

    @Override
    public void clickOnNormalMode(View v) {

    }

    public interface OnPictureItemClickListener {
        void onPictureItemClickListener(List<Picture> pictureList, int position);
    }
}
