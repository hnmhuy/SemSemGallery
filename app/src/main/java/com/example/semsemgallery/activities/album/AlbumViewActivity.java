package com.example.semsemgallery.activities.album;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.main.adapter.PictureRecyclerAdapter;
import com.example.semsemgallery.activities.pictureview.PictureViewActivity;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.domain.MediaRetriever;

import java.util.ArrayList;
import java.util.List;

public class AlbumViewActivity extends AppCompatActivity implements PictureRecyclerAdapter.OnPictureItemClickListener{

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

        // ====== Get albumId & albumName from Intent
        albumId = getIntent().getStringExtra("albumId");
        albumName = getIntent().getStringExtra("albumName");

        // ====== Check and Render picture from album
        if (albumId != null) {
            // Render data to gallery_recycler
            pictureList = new MediaRetriever(this).getPicturesByAlbumId(albumId);
            RecyclerView recyclerView = findViewById(R.id.activity_album_view_recycler);
            PictureRecyclerAdapter adapter = new PictureRecyclerAdapter(pictureList, this);
            adapter.setOnPictureItemClickListener(this);
            GridLayoutManager manager = new GridLayoutManager(this, 4);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(adapter);
        }

        // ====== Set title of Top bar
        topBar = (com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.activity_album_view_topAppBar);
        topBar.setTitle(albumName);
        if (!pictureList.isEmpty()) {
            topBar.setSubtitle(pictureList.size() + " images");
        } else {
            topBar.setSubtitle("0 image");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        topBar.setNavigationOnClickListener(v -> finish());
    }
    @Override
    public void onPictureItemClickListener(List<Picture> pictureList, int position) {
        Intent intent = new Intent(this, PictureViewActivity.class);
        intent.putParcelableArrayListExtra("pictureList", new ArrayList<>(pictureList));
        intent.putExtra("position", position);
        startActivity(intent);
    }

}