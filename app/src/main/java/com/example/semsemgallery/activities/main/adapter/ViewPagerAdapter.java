package com.example.semsemgallery.activities.main.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.semsemgallery.activities.main.AlbumsFragment;
import com.example.semsemgallery.activities.main.FavoritesFragment;
import com.example.semsemgallery.activities.main.PicturesFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PicturesFragment();
            case 1:
                return new AlbumsFragment();
            case 2:
                return new FavoritesFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

