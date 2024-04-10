package com.example.semsemgallery.activities.main2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.viewholder.FavoriteViewHolder;
import com.example.semsemgallery.models.Picture;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteViewHolder> implements FavoriteViewHolder.OnPictureItemClickListener {
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
        return new FavoriteViewHolder(view, observedData);
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

    @Override
    public void onPictureItemClickListener(List<Picture> pictureList, int position) {

    }
//    private final List<Picture> pictureList;
//    private final Context context;
//    private OnPictureItemClickListener listener;
//    public FavoriteAdapter(List<Picture> pictureList, Context context) {
//        this.pictureList = pictureList;
//        this.context = context;
//    }
//
//    public void setOnPictureItemClickListener(OnPictureItemClickListener listener) {
//        this.listener = listener;
//    }
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_picture_item, parent, false);
//        return new ViewHolder(view, listener,pictureList);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull FavoriteAdapter.ViewHolder holder, int position) {
//        Picture picture = pictureList.get(position);
//        if(picture.isFav()) {
//            holder.isFav.setImageResource(R.drawable.ic_heart_fill);
//            int color = ContextCompat.getColor(context, R.color.is_favorite);
//            holder.isFav.setColorFilter(color);
//        }
//        Glide.with(context)
//                .load(picture.getPath())
//                .fitCenter()
//                .into(holder.imageView);
//    }
//
//    @Override
//    public int getItemCount() {
//        return pictureList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
//        ImageView isFav;
//        ImageView imageView;
//        OnPictureItemClickListener listener;
//        List<Picture> pictureList;
//
//        public ViewHolder(@NonNull View itemView, OnPictureItemClickListener listener,  List<Picture> pictureList) {
//            super(itemView);
//            imageView = itemView.findViewById(R.id.gallery_item);
//            isFav = itemView.findViewById(R.id.isFav);
//            this.listener = listener;
//            this.pictureList = pictureList;
//            itemView.setOnClickListener(this);
//        }
//
//        @Override
//        public void onClick(View v) {
//            if (listener != null) {
//                int position = getAbsoluteAdapterPosition();
//                if (position != RecyclerView.NO_POSITION) {
//                    listener.onPictureItemClickListener(this.pictureList, position);
//                }
//            }
//        }
//    }
//
//        public interface OnPictureItemClickListener {
//        void onPictureItemClickListener(List<Picture> pictureList, int position);
//    }
}
