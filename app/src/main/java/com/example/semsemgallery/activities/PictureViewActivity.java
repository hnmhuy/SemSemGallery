package com.example.semsemgallery.activities;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.semsemgallery.R;
import com.example.semsemgallery.adapters.PictureAdapter;
import com.example.semsemgallery.fragments.MetaDataBottomSheet;
import com.example.semsemgallery.models.Picture;

import java.util.ArrayList;
import java.util.Date;

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

        String filePath = pictureList.get(position).getPath();
        String fileName = pictureList.get(position).getFileName();
        Date date = pictureList.get(position).getDateAdded();
        Log.e("FILEPATH", filePath);
        Log.e("DATe", date.toString());

        //Event listener for info button
        ImageButton infoBtn = (ImageButton) findViewById(R.id.info_button);
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MetaDataBottomSheet botSheetFrag = new MetaDataBottomSheet(filePath, fileName);
                botSheetFrag.show(getSupportFragmentManager(), botSheetFrag.getTag());

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // ====== Listener for the BackButton in the TopBar
        topBar.setNavigationOnClickListener(v -> finish());
    }
}