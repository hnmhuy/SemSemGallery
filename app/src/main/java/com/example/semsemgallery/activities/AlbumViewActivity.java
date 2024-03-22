package com.example.semsemgallery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

        // ====== Get albumId & albumName from Intent
        albumId = getIntent().getStringExtra("albumId");
        albumName = getIntent().getStringExtra("albumName");

        // ====== Check and Render picture from album
        if (albumId != null) {
            // Render data to gallery_recycler
            pictureList = new MediaRetriever(this).getPicturesByAlbumId(albumId);
            RecyclerView recyclerView = findViewById(R.id.activity_album_view_recycler);
            PictureRecyclerAdapter adapter = new PictureRecyclerAdapter(pictureList, this);
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

        // ====== Listener for the BackButton in the TopBar
        topBar.setNavigationOnClickListener(v -> finish());

        // ====== Listener for AddIcon in TopBar clicked
        topBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.add) {
                Toast.makeText(AlbumViewActivity.this, "Create Album", Toast.LENGTH_SHORT).show();
                showCustomDialog();
                return true;
            }
            return false;
        });
    }


    private void showCustomDialog() {
        // Inflate the custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.component_option_dialog, null);

        // Create a MaterialAlertDialogBuilder with the dialog view
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView);

        // Show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }
}