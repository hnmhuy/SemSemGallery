package com.example.semsemgallery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.models.Picture;

import java.util.List;

public class DeletedItemAdapter extends RecyclerView.Adapter<DeletedItemAdapter.ViewHolder> {

    private final List<Picture> pictureList;
    private final Context context;

    public DeletedItemAdapter(List<Picture> pictureList, Context context) {
        this.pictureList = pictureList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_deleted_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picture picture = pictureList.get(position);
        Glide.with(context).load(picture.getPath())
                .fitCenter().into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView remaining;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.deleted_item);
            remaining = itemView.findViewById(R.id.remaining);
        }
    }
}
