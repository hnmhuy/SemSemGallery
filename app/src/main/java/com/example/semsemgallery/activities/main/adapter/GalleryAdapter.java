package com.example.semsemgallery.activities.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main.viewholders.GalleryItem;
import com.example.semsemgallery.activities.main.viewholders.GalleryItemViewHolder;
import com.example.semsemgallery.models.Picture;

import java.util.zip.Inflater;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryItemViewHolder> {

    private ObservableGridMode<GalleryItem> observableGridMode;
    private Context context;

    public GalleryAdapter(Context context, ObservableGridMode<GalleryItem> data) {
        this.context = context;
        observableGridMode = data;
    }

    @NonNull
    @Override
    public GalleryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == GalleryItem.THUMBNAIL) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_picture_item, parent, false);
            return new GalleryItemViewHolder(view, observableGridMode, viewType);
        } else if (viewType == GalleryItem.GROUPDATE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_header_item, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true);

            return new GalleryItemViewHolder(view, observableGridMode, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryItemViewHolder holder, int position) {
        GalleryItem item = observableGridMode.getDataAt(position).data;
        if (item.getType() == GalleryItem.GROUPDATE) {
            holder.groupDisplayText.setText(item.getDateFormatted());
        } else {
            Glide.with(context).load(((Picture) item.getData()).getPath()).into(holder.thumnail);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return observableGridMode.getDataAt(position).data.getType();
    }

    @Override
    public int getItemCount() {
        return observableGridMode.getDataSize();
    }
}
