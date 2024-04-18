package com.example.semsemgallery.activities.pictureview.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.semsemgallery.activities.pictureview.fragment.PictureViewFragment;
import com.example.semsemgallery.models.Picture;

import java.util.ArrayList;

public class PictureAdapter extends FragmentStateAdapter {
    public ArrayList<Picture> pictures;
    private int position;
    public PictureAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Picture> pictures, int position) {
        super(fragmentActivity);
        this.pictures = pictures;
        this.position = position;
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
    public int getItemCount() {
        if(pictures != null) {
            return pictures.size();
        }
        return 0;
    }
}
