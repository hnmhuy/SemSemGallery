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
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.viewholder.AlbumViewHolder;
import com.example.semsemgallery.models.Album;

import java.util.List;

public class AlbumRecyclerAdapter extends RecyclerView.Adapter<AlbumViewHolder> {
    private ObservableGridMode<Album> observabledObj;
    private Context context;

    public AlbumRecyclerAdapter(Context context, ObservableGridMode<Album> observabledObj) {
        this.context = context;
        this.observabledObj = observabledObj;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_album_item, parent, false);
        return new AlbumViewHolder(view, observabledObj);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        ObservableGridMode<Album>.DataItem data = observabledObj.getDataAt(position);
        Glide.with(context).load(data.data.getWallPath()).into(holder.thumbnail);
        holder.albumName.setText(data.data.getName());
        holder.albumQuantity.setText(String.valueOf(data.data.getCount()));
        holder.selector.setChecked(data.isSelected);
    }

    @Override
    public int getItemCount() {
        return observabledObj.getDataSize();
    }
//    private final List<Album> albumList;
//    private final Context context;
//
//    // Handler for item clicks
//    private OnAlbumItemClickListener listener;
//
//    public AlbumRecyclerAdapter(List<Album> albumList, Context context) {
//        this.albumList = albumList;
//        this.context = context;
//    }
//
//
//    public void setOnAlbumItemClickListener(OnAlbumItemClickListener listener) {
//        this.listener = listener;
//    }
//
//
//    @NonNull
//    @Override
//    public AlbumRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_album_item, parent, false);
//        return new AlbumRecyclerAdapter.ViewHolder(view, listener);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull AlbumRecyclerAdapter.ViewHolder holder, int position) {
//        Album album = albumList.get(position);
//        Glide.with(context)
//                .load(album.getWallPath())
//                .fitCenter()
//                .into(holder.imageView);
//        holder.albumName.setText(album.getName());
//        holder.albumQuantity.setText(String.valueOf(album.getCount()));
//        holder.setAlbumId(album.getAlbumId());
//    }
//
//    @Override
//    public int getItemCount() {
//        return albumList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        ImageView imageView;
//        TextView albumName;
//        TextView albumQuantity;
//        OnAlbumItemClickListener listener;
//
//        String albumId;
//
//        public void setAlbumId(String id) {
//            albumId = id;
//        }
//
//        public ViewHolder(@NonNull View itemView, OnAlbumItemClickListener listener) {
//            super(itemView);
//            imageView = itemView.findViewById(R.id.component_album_item_image);
//            albumName = itemView.findViewById(R.id.component_album_item_name);
//            albumQuantity = itemView.findViewById(R.id.component_album_item_quantity);
//            this.listener = listener;
//            itemView.setOnClickListener(this);
//        }
//
//        @Override
//        public void onClick(View v) {
//            if (listener != null) {
//                int position = getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION) {
//                    listener.onAlbumItemClick(albumId , albumName.getText().toString());
//                }
//            }
//        }
//    }
//
//    public interface OnAlbumItemClickListener {
//        void onAlbumItemClick(String albumId, String albumName);
//    }
}
