package com.example.semsemgallery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.adapters.PictureRecyclerAdapter;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.utils.MediaRetriever;

import java.util.List;

public class AlbumViewActivity extends AppCompatActivity {

    private String albumId;
    private String albumName;
    private com.google.android.material.appbar.MaterialToolbar topBar;
    private List<Picture> pictureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_album_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_album_view_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get albumId & albumName from Intent
        albumId = getIntent().getStringExtra("albumId");
        albumName = getIntent().getStringExtra("albumName");

        // Check and Render picture from album
        if (albumId != null) {
            // Render data to gallery_recycler
            pictureList = new MediaRetriever(this).getPicturesByAlbumId(albumId);
            RecyclerView recyclerView = findViewById(R.id.activity_album_view_recycler);
            PictureRecyclerAdapter adapter = new PictureRecyclerAdapter(pictureList, this);
            GridLayoutManager manager = new GridLayoutManager(this, 4);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(adapter);
        }

        topBar = (com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.activity_album_view_topAppBar);
        topBar.setTitle(albumName);
        if (!pictureList.isEmpty()) {
            topBar.setSubtitle(pictureList.size() + " images");
        } else {
            topBar.setSubtitle("0 image");
        }
    }
}