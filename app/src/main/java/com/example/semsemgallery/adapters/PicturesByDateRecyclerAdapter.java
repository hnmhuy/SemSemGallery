package com.example.semsemgallery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.models.Picture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PicturesByDateRecyclerAdapter extends RecyclerView.Adapter<PicturesByDateRecyclerAdapter.ViewHolder>{
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
        Date date = p.get(0).getDateAdded();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        String formattedDate = dateFormat.format(date);
        PictureRecyclerAdapter innerAdapter = new PictureRecyclerAdapter(p, context);
        GridLayoutManager manager = new GridLayoutManager(context, 4);
        holder.recyclerView.setLayoutManager(manager);
        holder.recyclerView.setAdapter(innerAdapter);
        holder.txtView.setText(formattedDate);
    }


    @Override
    public int getItemCount() {
        return pictureList.size();
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
