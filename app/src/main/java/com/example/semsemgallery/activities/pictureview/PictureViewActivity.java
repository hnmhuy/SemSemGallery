package com.example.semsemgallery.activities.pictureview;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.main.adapter.PictureAdapter;
import com.example.semsemgallery.activities.pictureview.fragment.MetaDataBottomSheet;
import com.example.semsemgallery.models.Picture;

import java.io.File;
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
        ImageButton infoBtn = findViewById(R.id.info_button);
        infoBtn.setOnClickListener(view -> {
            MetaDataBottomSheet botSheetFrag = new MetaDataBottomSheet(filePath, fileName);
            botSheetFrag.show(getSupportFragmentManager(), botSheetFrag.getTag());

        });
        // Event share to other apps
        ImageButton shareBtn = findViewById(R.id.share_button);
        shareBtn.setOnClickListener(view -> {
            Log.d("Image Path", filePath);
            File imageFile = new File(filePath);
            Uri imageUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", imageFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant read permission

            startActivity(Intent.createChooser(shareIntent, "Share image via..."));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ====== Listener for the BackButton in the TopBar
        topBar.setNavigationOnClickListener(v -> finish());
    }
}