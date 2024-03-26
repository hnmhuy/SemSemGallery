package com.example.semsemgallery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.semsemgallery.R;
import com.example.semsemgallery.adapters.PictureAdapter;
import com.example.semsemgallery.models.Picture;

import java.util.ArrayList;

public class PictureViewActivity extends AppCompatActivity {
    private com.google.android.material.appbar.MaterialToolbar topBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_picture_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ArrayList<Picture> pictureList = getIntent().getParcelableArrayListExtra("pictureList");
        int position = getIntent().getIntExtra("position", 0);


        topBar = (com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.activity_picture_view_topAppBar);

        ViewPager2 viewPager = findViewById(R.id.vp_image);
        PictureAdapter adapter = new PictureAdapter(this, pictureList, position);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ====== Listener for the BackButton in the TopBar
        topBar.setNavigationOnClickListener(v -> finish());
    }
}