package com.example.semsemgallery.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.MainActivity;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.utils.MediaRetriever;

import java.util.List;

public class PicturesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        List<Picture> picturesRetriever = new MediaRetriever(appCompatActivity).getAllPictureList();

        return inflater.inflate(R.layout.fragment_pictures, container, false);
    }
}
