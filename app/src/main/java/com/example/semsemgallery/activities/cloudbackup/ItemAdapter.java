package com.example.semsemgallery.activities.cloudbackup;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.models.TrashedPicture;

import org.checkerframework.common.value.qual.StringVal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;

public class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final ObservableGridMode<Picture> observedObj;
    private final Context context;

    public ItemAdapter(ObservableGridMode<Picture> obj, Context context) {
        this.observedObj = obj;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View frame = inflater.inflate(R.layout.component_deleted_item, parent, false);
        return new ViewHolder(frame, observedObj);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ObservableGridMode<Picture>.DataItem data = observedObj.getDataAt(position);
        holder.selector.setChecked(data.isSelected);
        if(data.data.getPath() != null){
            Glide.with(context).load(data.data.getPath()).centerCrop().into(holder.thumbnail);
        } else {
            Glide.with(context).load(data.data.getUrl()).centerCrop().into(holder.thumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return observedObj.getDataSize();
    }
}
