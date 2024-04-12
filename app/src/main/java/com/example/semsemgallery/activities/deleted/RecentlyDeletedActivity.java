package com.example.semsemgallery.activities.deleted;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.GridModeEvent;
import com.example.semsemgallery.activities.base.GridModeListener;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.base.RecylerViewItemDecoration;
import com.example.semsemgallery.domain.Picture.TrashedPictureLoader;
import com.example.semsemgallery.models.TrashedPicture;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecentlyDeletedActivity extends AppCompatActivity implements GridModeListener {
    private final List<TrashedPicture> pictureList = new ArrayList<>();
    private final ObservableGridMode<TrashedPicture> observedObj = new ObservableGridMode<>(pictureList, GridMode.NORMAL);
    private final DeletedItemAdapter adapter = new DeletedItemAdapter(observedObj, this);

    // UI variable
    private MaterialToolbar toolbar;
    private MaterialToolbar selectingToolbar;
    private TextView selectedCounts;
    private MaterialCheckBox selectAll;
    private Menu actionMenu;
    private LinearLayout bottomAction;


    private final TrashedPictureLoader loader = new TrashedPictureLoader(this) {
        @Override
        public void onProcessUpdate(TrashedPicture... trashedPictures) {
            pictureList.add(trashedPictures[0]);
            observedObj.addData(trashedPictures[0]);
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        }

        @Override
        public void postExecute(Boolean res) {
            Log.i("RecentlyDeletedActivity", "Loaded trash picture");
            int size = observedObj.getDataSize();
            String postFix = size == 1 ? " picture" : " pictures";
            toolbar.setSubtitle(size + postFix);

            for (TrashedPicture p : pictureList) {
                Log.d("TrashedPicture", "TP - " + p.getId() + p.getPath());
            }

//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//                PendingIntent trashRequest;
//                List<Uri> uris = new ArrayList<>();
//                for (int i=0; i < 10; i++) {
//                    Uri temp = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, observedObj.getDataAt(i).data.getId());
//                    //Uri temp = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, observedObj.getDataAt(0).data.getPath());
//                    uris.add(temp);
//                }
//                trashRequest = MediaStore.createTrashRequest(getContentResolver(), uris, true);
//
//                try {
//                    startIntentSenderForResult(trashRequest.getIntentSender(), 101, null, 0, 0, 0, null);
//                } catch (IntentSender.SendIntentException e) {
//                    throw new RuntimeException(e);
//                }
//            }
        }
    };

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
        observedObj.addObserver(this);
        observedObj.setMaster(this);
        RecyclerView recyclerView = findViewById(R.id.deleted_item_recycler_view);

        GridLayoutManager manager = new GridLayoutManager(this, 3);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        int spacing = 8;
        recyclerView.addItemDecoration(new RecylerViewItemDecoration(spacing));
        loader.execute();
        toolbar = findViewById(R.id.topAppBarNormal);
        selectingToolbar = findViewById(R.id.topAppBar_SelectingMode);
        selectedCounts = findViewById(R.id.select_items);
        selectAll = findViewById(R.id.select_all);
        bottomAction = findViewById(R.id.bottomActions);
        //setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.trash_menu_edit) {
                observedObj.setGridMode(GridMode.SELECTING);
                observedObj.fireSelectionChangeForAll(false);
                return true;
            } else if (id == R.id.trash_menu_empty) {
                Toast.makeText(this, "Empty clicked", Toast.LENGTH_LONG).show();
                return true;
            } else return false;
        });

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = selectAll.isChecked();
                observedObj.fireSelectionChangeForAll(isCheck);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (observedObj.getCurrentMode() == GridMode.NORMAL)
                    finish();
                else {
                    observedObj.setGridMode(GridMode.NORMAL);
                }
            }
        });
    }

    @Override
    public void onModeChange(GridModeEvent event) {
        selectedCounts.setText(R.string.select_items);
        if (event.getGridMode() == GridMode.NORMAL) {
            toolbar.setVisibility(View.VISIBLE);
            selectingToolbar.setVisibility(View.INVISIBLE);
            bottomAction.setVisibility(View.INVISIBLE);
        } else if (event.getGridMode() == GridMode.SELECTING) {
            toolbar.setVisibility(View.INVISIBLE);
            selectingToolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSelectionChange(GridModeEvent event) {
        long selectedCount = observedObj.getNumberOfSelected();
        selectAll.setChecked(selectedCount == observedObj.getDataSize());
        if (selectedCount != 0) {
            selectedCounts.setText(String.valueOf(selectedCount) + " selected");
            bottomAction.setVisibility(View.VISIBLE);
        } else {
            bottomAction.setVisibility(View.INVISIBLE);
            selectedCounts.setText(R.string.select_items);
        }
    }
    @Override
    public void onSelectingAll(GridModeEvent event) {

        selectAll.setChecked(event.getNewSelectionForAll());
        selectedCounts.setText(String.valueOf(observedObj.getNumberOfSelected()) + " selected");
    }
//    @Override
//    public void onBackPressed() {
//        if (observedObj.getCurrentMode() == GridMode.NORMAL)
//            super.onBackPressed();
//        else {
//            observedObj.setGridMode(GridMode.NORMAL);
//        }
//    }
}
