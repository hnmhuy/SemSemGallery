package com.example.semsemgallery.activities.pictureview.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.pictureview.fragment.PictureViewFragment;
import com.example.semsemgallery.models.Picture;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

public class PictureAdapter extends FragmentStateAdapter {
    public ArrayList<Picture> pictures;
    private final Context context;
    private final FragmentManager manager;

    public PictureAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Picture> pictures, int position) {
        super(fragmentActivity);
        this.manager = fragmentActivity.getSupportFragmentManager();
        this.pictures = pictures;
        this.context = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position < 0) return null;
        Picture picture = pictures.get(position);
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putParcelable("picture", picture);
        PictureViewFragment pictureView = new PictureViewFragment();
        pictureView.setArguments(args);
        return pictureView;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        if(pictures != null) {
            return pictures.size();
        }
        return 0;
    }

    public void removePictureAt(int pos) {
        pictures.remove(pos);
        notifyItemRemoved(pos);
        if (pos == pictures.size()) return;
        PictureViewFragment fragment = (PictureViewFragment) manager.findFragmentByTag("f" + pos);
        if (fragment != null) {
            PhotoView ptv = fragment.getPhotoView();
            if (ptv != null) {
                Glide.with(context).load(pictures.get(pos).getPath()).into(ptv);
            }
        }
    }
}
