package com.example.semsemgallery.activities.pictureview.adapter;

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
import com.example.semsemgallery.models.Album;

import java.util.List;

public class AlbumRecyclerAdapter_PictureView extends RecyclerView.Adapter<AlbumRecyclerAdapter_PictureView.ViewHolder> {
    private final List<Album> albumList;
    private final Context context;

    // Handler for item clicks
    private OnAlbumItemClickListener listener;


    public AlbumRecyclerAdapter_PictureView(List<Album> albumList, Context context) {
        this.albumList = albumList;
        this.context = context;
    }


    public void setOnAlbumItemClickListener(OnAlbumItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public AlbumRecyclerAdapter_PictureView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_album_item, parent, false);
        return new AlbumRecyclerAdapter_PictureView.ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumRecyclerAdapter_PictureView.ViewHolder holder, int position) {
        Album album = albumList.get(position);
        Glide.with(context)
                .load(album.getWallId())
                .fitCenter()
                .into(holder.imageView);
        holder.albumName.setText(album.getName());
        holder.setAlbumId(album.getAlbumId());
        holder.albumQuantity.setText(String.valueOf(album.getCount()));
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView albumName;
        TextView albumQuantity;
        OnAlbumItemClickListener listener;

        String albumId;

        public void setAlbumId(String id) {
            albumId = id;
        }

        public ViewHolder(@NonNull View itemView, OnAlbumItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.component_album_item_image);
            albumName = itemView.findViewById(R.id.component_album_item_name);
            albumQuantity = itemView.findViewById(R.id.component_album_item_quantity);
            this.listener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAlbumItemClick(albumId , albumName.getText().toString());
                }
            }
        }
    }

    public interface OnAlbumItemClickListener {
        void onAlbumItemClick(String albumId, String albumName);
    }
}