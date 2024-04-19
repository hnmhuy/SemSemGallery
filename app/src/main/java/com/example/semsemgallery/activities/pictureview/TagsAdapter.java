package com.example.semsemgallery.activities.pictureview;

import static ly.img.android.pesdk.backend.decoder.ImageSource.getResources;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.models.Tag;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder>{
    private final Context context;
    private final ArrayList<Tag> tags;
    private TagClickListener tagClickListener;
    private TagLongLickListener tagLongClickListener;
    public TagsAdapter(Context context, ArrayList<Tag> tags) {
        this.context = context;
        this.tags = tags;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_tag_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tag tag = tags.get(position);
        if(tag.getType() == 1 || tag.getType() == -1) {
            @SuppressLint("UseCompatLoadingForColorStateLists") ColorStateList colorStateList = getResources().getColorStateList(R.color.tag_bg);
            holder.btn.setBackgroundTintList(colorStateList);
            holder.btn.setIconResource(R.drawable.ic_plus);
        } else if(tag.getType() == 2) {
            // Top 3 recent tags
            @SuppressLint("UseCompatLoadingForColorStateLists") ColorStateList colorStateList = getResources().getColorStateList(R.color.tag_bg);
            holder.btn.setBackgroundTintList(colorStateList);
            holder.btn.setIconResource(R.drawable.ic_recent);
        } else if (tag.getType() == 3) {
            // Is picture tag
            @SuppressLint("UseCompatLoadingForColorStateLists") ColorStateList colorStateList = getResources().getColorStateList(R.color.track_on);
            holder.btn.setBackgroundTintList(colorStateList);
        } else if(tag.getType() == 4) {
            @SuppressLint("UseCompatLoadingForColorStateLists") ColorStateList colorStateList = getResources().getColorStateList(R.color.track_on);
            holder.btn.setBackgroundTintList(colorStateList);
            holder.btn.setIconResource(R.drawable.ic_hash_tag);
        }
        holder.btn.setText(tag.getName());
        holder.listener = tagClickListener; // Assign the listener here

        holder.itemView.setOnLongClickListener(v -> {
            if (tagLongClickListener != null) {
                tagLongClickListener.onTagLongClick(v, position, holder.btn.getText().toString());
                return true; // Consume the long click event
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        MaterialButton btn;
        TagClickListener listener;
        TagLongLickListener longClickListener;
        @SuppressLint("WrongViewCast")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btn = itemView.findViewById(R.id.tag_btn);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(listener != null) {
                listener.onTagClick(view, getAbsoluteAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longClickListener != null) {
                longClickListener.onTagLongClick(view, getAbsoluteAdapterPosition(), btn.getText().toString());
                return true; // Consume the long click event
            }
            return false;
        }
    }
    public void setOnItemListener(TagClickListener tagClickListener) {
        this.tagClickListener = tagClickListener;
    }

    public void setOnItemLongClickListener(TagLongLickListener longClickListener) {
        this.tagLongClickListener = longClickListener;
    }

    public interface TagClickListener {
        void onTagClick(View view, int position);
    }

    public interface TagLongLickListener {
        void onTagLongClick(View view, int position, String buttonText);
    }

    public void updateTagIcon(int position) {
        int type = tags.get(position).getType();
        if(type == 1 || type == 2) {
            tags.get(position).setType(4);
        } else if(type == 4) {
            tags.get(position).setType(1);
        } else if(type == 3) {
            tags.get(position).setType(-1);
        }else if (type == -1) {
            tags.get(position).setType(4);
        }
        notifyItemChanged(position);
    }
}