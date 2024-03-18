package com.example.semsemgallery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.models.Picture;

import java.util.List;

public class PictureRecyclerAdapter extends RecyclerView.Adapter<PictureRecyclerAdapter.ViewHolder>{
    private final List<Picture> pictureList;
    private final Context context;

    public PictureRecyclerAdapter(List<Picture> pictureList, Context context) {
        this.pictureList = pictureList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_picture_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureRecyclerAdapter.ViewHolder holder, int position) {
        Picture picture = pictureList.get(position);
        Glide.with(context)
                .load(picture.getPath())
                .fitCenter()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_item);
        }
    }
}