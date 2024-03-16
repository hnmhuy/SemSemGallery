package com.example.semsemgallery.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.models.Album;
import com.example.semsemgallery.utils.MediaRetriever;

import java.util.List;

public class AlbumRecyclerAdapter extends RecyclerView.Adapter<AlbumRecyclerAdapter.ViewHolder> {
    private final List<Album> albumList;
    private final Context context;

    public AlbumRecyclerAdapter(List<Album> albumList, Context context) {
        this.albumList = albumList;
        this.context = context;
    }

    @NonNull
    @Override
    public AlbumRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_album_item, parent, false);
        return new AlbumRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumRecyclerAdapter.ViewHolder holder, int position) {
        Album album = albumList.get(position);
        Glide.with(context)
                .load(album.getImgWall())
                .fitCenter()
                .into(holder.imageView);

        holder.albumName.setText(album.getName());
        AppCompatActivity appCompatActivity = (AppCompatActivity) context;
        MediaRetriever mediaRetriever = new MediaRetriever(appCompatActivity);

        holder.albumQuantity.setText(String.valueOf(mediaRetriever.getAlbumSize(album.getAlbumId())));
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView albumName;
        TextView albumQuantity;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.component_album_item_image);
            albumName = itemView.findViewById(R.id.component_album_item_name);
            albumQuantity = itemView.findViewById(R.id.component_album_item_quantity);
        }
    }
}
