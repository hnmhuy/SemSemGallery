package com.example.semsemgallery.fragments;

import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.semsemgallery.R;
import com.example.semsemgallery.models.Album;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.utils.MediaRetriever;

import java.util.List;

public class FavoritesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        List<Picture> favAlbumRetriever = new MediaRetriever(appCompatActivity).getFavoriteAlbumList();


        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }
}
