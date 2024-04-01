package com.example.semsemgallery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.models.Picture;

import java.util.List;

public class PictureRecyclerAdapter extends RecyclerView.Adapter<PictureRecyclerAdapter.ViewHolder>{
    private final List<Picture> pictureList;
    private final Context context;

    private OnPictureItemClickListener listener;
    public PictureRecyclerAdapter(List<Picture> pictureList, Context context) {
        this.pictureList = pictureList;
        this.context = context;
    }

    public void setOnPictureItemClickListener(OnPictureItemClickListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_picture_item, parent, false);
        return new ViewHolder(view, listener,pictureList);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureRecyclerAdapter.ViewHolder holder, int position) {
        Picture picture = pictureList.get(position);
        if(picture.isFav()) {
            holder.isFav.setImageResource(R.drawable.ic_heart_fill);
            int color = ContextCompat.getColor(context, R.color.is_favorite);
            holder.isFav.setColorFilter(color);
        }
        Glide.with(context)
                .load(picture.getPath())
                .fitCenter()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView isFav;
        ImageView imageView;
        OnPictureItemClickListener listener;
        List<Picture> pictureList;

        public ViewHolder(@NonNull View itemView, OnPictureItemClickListener listener,  List<Picture> pictureList) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_item);
            isFav = itemView.findViewById(R.id.isFav);
            this.listener = listener;
            this.pictureList = pictureList;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onPictureItemClickListener(this.pictureList, position);
                }
            }
        }
    }

        public interface OnPictureItemClickListener {
        void onPictureItemClickListener(List<Picture> pictureList, int position);
    }
}
