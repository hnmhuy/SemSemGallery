package com.example.semsemgallery.fragments;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.PictureViewActivity;
import com.example.semsemgallery.adapters.PictureRecyclerAdapter;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.utils.MediaRetriever;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements PictureRecyclerAdapter.OnPictureItemClickListener{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        List<Picture> favAlbumRetriever = new MediaRetriever(appCompatActivity).getFavoriteAlbumList();
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.gallery_recycler);
        PictureRecyclerAdapter adapter = new PictureRecyclerAdapter(favAlbumRetriever, appCompatActivity);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 4);
        adapter.setOnPictureItemClickListener(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onPictureItemClickListener(List<Picture> pictureList, int position) {
        Toast.makeText(getActivity(), "Image click: " + position, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), PictureViewActivity.class);
        intent.putParcelableArrayListExtra("pictureList", new ArrayList<>(pictureList));
        intent.putExtra("position", position);

        // Start the activity
        startActivity(intent);
    }
}
