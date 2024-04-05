package com.example.semsemgallery.activities.deleted;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.models.Picture;

public class DeletedItemAdapter extends RecyclerView.Adapter<DeletedViewHolder> {

    private final ObservableGridMode<Picture> observedObj;
    private final Context context;

    public DeletedItemAdapter(ObservableGridMode<Picture> obj, Context context) {
        this.observedObj = obj;
        this.context = context;
    }

    @NonNull
    @Override
    public DeletedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View frame = inflater.inflate(R.layout.component_deleted_item, parent, false);
        return new DeletedViewHolder(frame, observedObj);
    }

    @Override
    public void onBindViewHolder(@NonNull DeletedViewHolder holder, int position) {
        ObservableGridMode<Picture>.DataItem data = observedObj.getDataAt(position);
        holder.selector.setChecked(data.isSelected);
        //new ThumbnailLoader(holder.thumbnail).execute(data.data.getPath());
        Glide.with(context).load(data.data.getPath()).centerCrop().into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return observedObj.getDataSize();
    }
}
