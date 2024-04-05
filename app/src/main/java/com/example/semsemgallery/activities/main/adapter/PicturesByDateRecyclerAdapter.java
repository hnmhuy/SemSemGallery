package com.example.semsemgallery.activities.main.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.pictureview.PictureViewActivity;
import com.example.semsemgallery.models.Picture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PicturesByDateRecyclerAdapter extends RecyclerView.Adapter<PicturesByDateRecyclerAdapter.ViewHolder> implements PictureRecyclerAdapter.OnPictureItemClickListener{
    private final List<List<Picture>> pictureList;
    private final Context context;

    public PicturesByDateRecyclerAdapter(List<List<Picture>> pictureList, Context context) {
        super();
        this.pictureList = pictureList;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_image_by_date, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<Picture> p = pictureList.get(position);
        Date date = p.get(0).getDateTaken();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        String formattedDate = dateFormat.format(date);
        PictureRecyclerAdapter innerAdapter = new PictureRecyclerAdapter(p, context);
        GridLayoutManager manager = new GridLayoutManager(context, 4);
        innerAdapter.setOnPictureItemClickListener(this);
        holder.recyclerView.setLayoutManager(manager);
        holder.recyclerView.setAdapter(innerAdapter);
        holder.txtView.setText(formattedDate);
    }


    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    @Override
    public void onPictureItemClickListener(List<Picture> pictureList, int position) {
        Intent intent = new Intent(context, PictureViewActivity.class);
        intent.putParcelableArrayListExtra("pictureList", new ArrayList<>(pictureList));
        intent.putExtra("position", position);

        // Start the activity
        context.startActivity(intent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtView;
        RecyclerView recyclerView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtView = itemView.findViewById(R.id.picture_date);
            recyclerView = itemView.findViewById(R.id.gallery_recycler);
        }
    }
}
