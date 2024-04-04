package com.example.semsemgallery.activities.deleted;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.core.ObservableViewModeEvent;
import com.example.semsemgallery.activities.core.SelectableImageControl;
import com.example.semsemgallery.models.Picture;

import java.util.ArrayList;
import java.util.List;

public class DeletedItemAdapter extends RecyclerView.Adapter<DeletedItemAdapter.ViewHolder> {

    private final List<Picture> pictureList;
    private final List<Integer> selectedIndexes = new ArrayList<>();
    private final Context context;

    private final ObservableViewModeEvent observableViewModeEvent;

    public DeletedItemAdapter(List<Picture> pictureList, Context context, ObservableViewModeEvent observableObj) {
        this.pictureList = pictureList;
        this.context = context;
        observableViewModeEvent = observableObj;
        Log.d("DELETED-ITEMS", "Number of image " + pictureList.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_deleted_item, parent, false);
        ViewHolder holder = new ViewHolder(view, observableViewModeEvent);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picture picture = pictureList.get(position);
        Glide.with(context).load(picture.getPath())
                .fitCenter().into(holder.control.getThumbnail());
        holder.control.setCurrPosition(position);
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout selectableImage;
        TextView remaining;
        SelectableImageControl control;

        public ViewHolder(@NonNull View itemView, ObservableViewModeEvent observableObj) {
            super(itemView);
            selectableImage = itemView.findViewById(R.id.selectable_image);
            control = new SelectableImageControl(selectableImage, observableObj);
            remaining = itemView.findViewById(R.id.remaining);
        }
    }
}
