package com.example.semsemgallery.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.AlbumViewActivity;
import com.example.semsemgallery.adapters.AlbumRecyclerAdapter;
import com.example.semsemgallery.adapters.PictureRecyclerAdapter;
import com.example.semsemgallery.models.Album;
import com.example.semsemgallery.utils.MediaRetriever;

import java.util.List;

public class AlbumsFragment extends Fragment implements AlbumRecyclerAdapter.OnAlbumItemClickListener {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        List<Album> albumsRetriever = new MediaRetriever(appCompatActivity).getAlbumList();
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.album_recycler);
        AlbumRecyclerAdapter adapter = new AlbumRecyclerAdapter(albumsRetriever, appCompatActivity);

        adapter.setOnAlbumItemClickListener(this); // Event Click

        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAlbumItemClick(String albumId, String albumName) {
        Toast.makeText(requireActivity(), "Album : " + albumName, Toast.LENGTH_SHORT).show();

        // Move to AlbumViewActivity & provide albumId
        Intent intent = new Intent(requireContext(), AlbumViewActivity.class);
        intent.putExtra("albumId", albumId);
        intent.putExtra("albumName", albumName);
        startActivity(intent);
    }
}
