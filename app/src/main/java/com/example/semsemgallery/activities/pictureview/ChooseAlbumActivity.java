package com.example.semsemgallery.activities.pictureview;

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
import com.example.semsemgallery.activities.pictureview.adapter.AlbumRecyclerAdapter_PictureView;
import com.example.semsemgallery.domain.MediaRetriever;
import com.example.semsemgallery.models.Album;

import java.util.List;

public class ChooseAlbumActivity extends AppCompatActivity implements AlbumRecyclerAdapter_PictureView.OnAlbumItemClickListener  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_album);

        List<Album> albumsRetriever = new MediaRetriever(ChooseAlbumActivity.this).getAlbumList();

        RecyclerView recyclerView = findViewById(R.id.album_recycler);
        AlbumRecyclerAdapter_PictureView adapter = new AlbumRecyclerAdapter_PictureView(albumsRetriever, ChooseAlbumActivity.this);

        adapter.setOnAlbumItemClickListener(this); // Event Click

        GridLayoutManager manager = new GridLayoutManager(ChooseAlbumActivity.this, 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onAlbumItemClick(String albumId, String albumName) {
        String data = albumName;
        Intent resultIntent = new Intent();
        resultIntent.putExtra("key", data);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}