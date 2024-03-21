package com.example.semsemgallery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.adapters.ChangeViewModeListener;
import com.example.semsemgallery.adapters.DeletedItemAdapter;
import com.example.semsemgallery.adapters.ObservableViewModeEvent;
import com.example.semsemgallery.adapters.ViewMode;
import com.example.semsemgallery.adapters.ViewModeEvent;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.utils.MediaRetriever;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class RecentlyDeletedActivity extends AppCompatActivity implements ChangeViewModeListener {
    private ObservableViewModeEvent observableViewModeEvent;
    private List<Picture> pictureList;
    private MaterialToolbar normalToolBar;
    private MaterialToolbar selectingToolBar;
    private TextView selectedItems;
    private CheckBox selectAllBtn;

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

        observableViewModeEvent = new ObservableViewModeEvent();
        observableViewModeEvent.addObservers(this);
        pictureList = new MediaRetriever(this).getAllPictureList();
        RecyclerView recyclerView = findViewById(R.id.deleted_item_recycler_view);
        DeletedItemAdapter adapter = new DeletedItemAdapter(pictureList, this, observableViewModeEvent);

        GridLayoutManager manager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        normalToolBar = findViewById(R.id.topAppBarNormal);
        selectingToolBar = findViewById(R.id.topAppBar_SelectingMode);
        selectedItems = findViewById(R.id.select_items);
        selectAllBtn = findViewById(R.id.select_all);

        selectAllBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectAllImages();
                    observableViewModeEvent.fireSelectionChangeEvent(true);
                } else {
                    removeAllSelectedImage();
                    observableViewModeEvent.fireSelectionChangeEvent(true);
                }
            }
        });
    }

    private void selectAllImages() {
        for (int i = 0; i < this.pictureList.size(); i++) {
            observableViewModeEvent.getSelectedPositions().add(i);
        }
    }

    private void removeAllSelectedImage() {
        observableViewModeEvent.getSelectedPositions().clear();
    }


    @Override
    public void onChangeMode(ViewModeEvent event) {
        if (event.getViewMode() == ViewMode.NORMAL_VIEW) {
            normalToolBar.setVisibility(View.VISIBLE);
            selectingToolBar.setVisibility(View.INVISIBLE);
            selectedItems.setText(R.string.select_items);
        } else if (event.getViewMode() == ViewMode.SELECTING_VIEW) {
            normalToolBar.setVisibility(View.INVISIBLE);
            selectingToolBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSelectionChange(ViewModeEvent event) {
        if (!event.getSelectedPositions().isEmpty()) {
            selectedItems.setText(String.format("%d selected", event.getSelectedPositions().size()));
        } else selectedItems.setText(R.string.select_items);
    }
}
