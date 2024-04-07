package com.example.semsemgallery.activities.deleted;

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

public class DeletedItemAdapter extends RecyclerView.Adapter<DeletedViewHolder> {

    private final ObservableGridMode<TrashedPicture> observedObj;
    private final Context context;

    public DeletedItemAdapter(ObservableGridMode<TrashedPicture> obj, Context context) {
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
        ObservableGridMode<TrashedPicture>.DataItem data = observedObj.getDataAt(position);
        holder.selector.setChecked(data.isSelected);
        //new ThumbnailLoader(holder.thumbnail).execute(data.data.getPath());
        Glide.with(context).load(data.data.getPath()).centerCrop().into(holder.thumbnail);
        holder.remainingDate.setText(calRemainingDate(data.data.getExpired()));
    }

    private String calRemainingDate(Date expired) {
        LocalDate currentDate = LocalDate.now();
        Instant instant = expired.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate expiredDate = instant.atZone(zoneId).toLocalDate();
        long daysDifference = ChronoUnit.DAYS.between(currentDate, expiredDate);
        return String.valueOf(daysDifference - 1) + " day(s)";
    }

    @Override
    public int getItemCount() {
        return observedObj.getDataSize();
    }
}
