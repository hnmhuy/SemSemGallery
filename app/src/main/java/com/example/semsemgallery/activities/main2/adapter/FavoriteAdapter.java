package com.example.semsemgallery.activities.main2.adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.viewholder.FavoriteViewHolder;
import com.example.semsemgallery.activities.pictureview.PictureViewActivity;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.models.Picture;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteViewHolder> {
    private final Context context;
    private final ObservableGridMode<Picture> observedData;

    public FavoriteAdapter(Context context, ObservableGridMode<Picture> data) {
        this.context = context;
        this.observedData = data;
    }
    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_picture_item, parent, false);
        return new FavoriteViewHolder(view, observedData) {
            @Override
            public void clickOnNormalMode(View v) {
                Intent intent = new Intent(context, PictureViewActivity.class);
                Picture transferData = observedObj.getDataList().get(getAbsoluteAdapterPosition());
                intent.putExtra("selectingPic", transferData);
                intent.putExtra("loadMode", PictureLoadMode.FAVORITE.toString());
                startActivity(context, intent, null);
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        ObservableGridMode<Picture>.DataItem item = observedData.getDataAt(position);
        holder.heart.setVisibility(item.data.isFav() ? View.VISIBLE : View.INVISIBLE);
        Glide.with(context).load(item.data.getPath()).into(holder.thumbnail);
        holder.selector.setChecked(item.isSelected);
    }

    @Override
    public int getItemCount() {
        return observedData.getDataSize();
    }
}
