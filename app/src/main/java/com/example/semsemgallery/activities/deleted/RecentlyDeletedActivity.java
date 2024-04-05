package com.example.semsemgallery.activities.deleted;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.base.RecylerViewItemDecoration;
import com.example.semsemgallery.activities.core.ChangeViewModeListener;
import com.example.semsemgallery.activities.core.ObservableViewModeEvent;
import com.example.semsemgallery.activities.core.ViewMode;
import com.example.semsemgallery.activities.core.ViewModeEvent;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.domain.MediaRetriever;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class RecentlyDeletedActivity extends AppCompatActivity {
    private List<Picture> pictureList = null;
    private final int spacing = 8;
    private ObservableGridMode<Picture> observedObj;
    private MediaRetriever loader;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recently_deleted);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recently_delete_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        this.loader = new MediaRetriever(this);
        pictureList = loader.getAllPictureList();
        observedObj = new ObservableGridMode<>(pictureList, GridMode.NORMAL);

        RecyclerView recyclerView = findViewById(R.id.deleted_item_recycler_view);
        DeletedItemAdapter adapter = new DeletedItemAdapter(observedObj, this);
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new RecylerViewItemDecoration(spacing));
    }

}
