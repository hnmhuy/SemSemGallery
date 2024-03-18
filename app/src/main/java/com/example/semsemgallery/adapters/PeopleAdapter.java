package com.example.semsemgallery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.models.People;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> {

    private ArrayList<People> listitems;
    private Context context;

    public PeopleAdapter(ArrayList<People> items, Context context) {
        this.listitems = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.component_person,parent,false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        People item = listitems.get(position);
        Picasso.with(context).load(item.getImage()).into(holder.im_item);
    }

    @Override
    public int getItemCount() {
        return listitems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView im_item;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            im_item = itemView.findViewById(R.id.image_person);
        }
    }
}