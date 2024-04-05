package com.example.semsemgallery.activities.deleted;

import android.content.Context;
import android.text.Layout;
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
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.core.ObservableViewModeEvent;
import com.example.semsemgallery.activities.core.SelectableImageControl;
import com.example.semsemgallery.models.Picture;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

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
        Picture pic = observedObj.getDataAt(position);
        holder.position = holder.getAbsoluteAdapterPosition();
        Glide.with(context).load(pic.getPath()).centerCrop().into(holder.thumbnail);
        holder.selector.setChecked(pic.isSelected);
    }

    @Override
    public int getItemCount() {
        return observedObj.getDataSize();
    }
}
