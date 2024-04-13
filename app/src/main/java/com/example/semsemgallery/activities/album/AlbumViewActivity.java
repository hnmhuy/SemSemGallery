package com.example.semsemgallery.activities.album;

import android.content.Context;
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
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.adapter.FavoriteAdapter;
import com.example.semsemgallery.activities.main2.adapter.GalleryAdapter;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItem;
import com.example.semsemgallery.activities.pictureview.PictureViewActivity;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.domain.MediaRetriever;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class AlbumViewActivity extends AppCompatActivity {
    private final Context context = this;
    private ObservableGridMode<GalleryItem> observedObj = null;
    private TreeSet<GalleryItem> galleryItems = new TreeSet<>(Comparator.reverseOrder());
    private GalleryAdapter adapter = null;
    private String albumId;
    private String albumName;
    private int albumQuantity;
    private com.google.android.material.appbar.MaterialToolbar topBar;
    private List<Picture> pictureList;
    private RecyclerView recyclerView;

    private PictureLoader loader = new PictureLoader(this) {
        @Override
        public void onProcessUpdate(Picture... pictures) {
            galleryItems.add(new GalleryItem(pictures[0]));
        }

        @Override
        public void postExecute(Boolean res) {
            List<GalleryItem> pictures = new ArrayList<>(galleryItems);
            observedObj = new ObservableGridMode<>(pictures, GridMode.NORMAL);
            adapter = new GalleryAdapter(context, observedObj);
            recyclerView.setAdapter(adapter);
        }
    };

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
        albumQuantity = getIntent().getIntExtra("albumQuantity", 0);


        // Get UI component and set up
        recyclerView = findViewById(R.id.activity_album_view_recycler);
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(manager);
        // ====== Check and load picture from album
        if (albumId != null) {
            loader.execute(PictureLoadMode.BY_ALBUM.toString(), albumId);
        }

        // ====== Set title of Top bar
        topBar = (com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.activity_album_view_topAppBar);
        topBar.setTitle(albumName);
        topBar.setSubtitle(String.valueOf(albumQuantity) + (albumQuantity == 0 ? " image" : " images"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        topBar.setNavigationOnClickListener(v -> finish());
    }

}