package com.example.semsemgallery.activities.main.adapter;

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

public class AlbumRecyclerAdapter extends RecyclerView.Adapter<AlbumRecyclerAdapter.ViewHolder> {
    private final List<Album> albumList;
    private final Context context;

    // Handler for item clicks
    private OnAlbumItemClickListener listener;


    public AlbumRecyclerAdapter(List<Album> albumList, Context context) {
        this.albumList = albumList;
        this.context = context;
    }


    public void setOnAlbumItemClickListener(OnAlbumItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public AlbumRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_album_item, parent, false);
        return new AlbumRecyclerAdapter.ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumRecyclerAdapter.ViewHolder holder, int position) {
        Album album = albumList.get(position);
        Glide.with(context)
                .load(album.getImgWall())
                .fitCenter()
                .into(holder.imageView);
        holder.albumName.setText(album.getName());
        holder.albumQuantity.setText(String.valueOf(album.getSize()));
        holder.setAlbumId(album.getAlbumId());
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
