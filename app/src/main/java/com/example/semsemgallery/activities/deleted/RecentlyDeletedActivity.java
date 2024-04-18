package com.example.semsemgallery.activities.deleted;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.example.semsemgallery.domain.Picture.GarbagePictureCollector;
import com.example.semsemgallery.domain.Picture.TrashedPictureLoader;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.models.TrashedPicture;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

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

            //==Restore the pending file to original file
            Log.d("PendingHandler", "Start restore");
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            for (TrashedPicture p : pictureList) {
                Log.d("TrashedPicture", "TP - " + p.getId() + p.getPath());
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p.getId());
                //getApplicationContext().getContentResolver().update(imageUri, values, null, null);
            }
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
        toolbar.setNavigationOnClickListener((v) -> {
            finish();
        });
        selectingToolbar = findViewById(R.id.topAppBar_SelectingMode);
        selectedCounts = findViewById(R.id.select_items);
        selectAll = findViewById(R.id.select_all);
        bottomAction = findViewById(R.id.bottomActions);
        ActionBarHandlers();
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

    private AlertDialog createDialog(String titleText, boolean isCancel, View.OnClickListener cancelCallback) {
        //=Prepare dialog
        View dialogView = getLayoutInflater().inflate(R.layout.component_loading_dialog, null);
        TextView title = dialogView.findViewById(R.id.component_loading_dialog_title);
        title.setText(titleText);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this).setView(dialogView);

        AlertDialog loadingDialog = dialogBuilder.create();
        loadingDialog.setCanceledOnTouchOutside(false);

        Button cancelButton = dialogView.findViewById(R.id.component_loading_dialog_cancelButton);
        cancelButton.setVisibility(isCancel ? View.VISIBLE : View.INVISIBLE);
        if (cancelCallback != null) {
            cancelButton.setOnClickListener(cancelCallback);
        }
        return loadingDialog;
    }

    ;

    private void DeleteImage() {
        AlertDialog loadingDialog = createDialog("Deleting image", false, null);
        long numberOfDeleted = observedObj.getNumberOfSelected();
        List<TrashedPicture> deletePictures = observedObj.getAllSelectedItems();
        List<ObservableGridMode<TrashedPicture>.DataItem> dataItems = observedObj.getSelectedDataItem();
        GarbagePictureCollector.DeletePicture deleter = new GarbagePictureCollector.DeletePicture(this) {
            @Override
            public void preExecute(List<TrashedPicture>... lists) {
                loadingDialog.show();
            }

            @Override
            public void onProcessUpdate(Long... longs) {
                ProgressBar bar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
                bar.setProgress((int) ((longs[0] * 100) / numberOfDeleted));
            }

            @Override
            public void postExecute(Void res) {
                loadingDialog.dismiss();
                for (TrashedPicture tp : deletePictures) {
                    int indexTP = deletePictures.indexOf(tp);
                    int index = observedObj.getObservedObjects().indexOf(dataItems.get(indexTP));
                    observedObj.getObservedObjects().remove(index);
                    adapter.notifyItemRemoved(index);
                }
                observedObj.setGridMode(GridMode.NORMAL);
                toolbar.setSubtitle(String.valueOf(observedObj.getDataSize()));
            }
        };
        deleter.execute(deletePictures);
    }

    private boolean requireAllFileAccess() {
        boolean isStorageManager = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            isStorageManager = Environment.isExternalStorageManager();
        }

        if (isStorageManager) {
            // Your app already has storage management permissions
            // You can proceed with file operations
            return isStorageManager;
        } else {
            // Your app does not have storage management permissions
            // Guide the user to the system settings page to grant permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }
        return false;
    }

    private void ActionBarHandlers() {
        Button btnRestore = bottomAction.findViewById(R.id.btnRestore);
        btnRestore.setOnClickListener((v) -> {
            //=Prepare dialog
            AlertDialog loadingDialog = createDialog("Restoring image", false, null);
            List<ObservableGridMode<TrashedPicture>.DataItem> deletedItems = observedObj.getSelectedDataItem();
            Long[] transferData = new Long[deletedItems.size()];
            for (int i = 0; i < deletedItems.size(); i++) {
                transferData[i] = deletedItems.get(i).data.getId();
            }
            final boolean[] canExecute = {requireAllFileAccess()};
            GarbagePictureCollector.TrashPictureHandler trashPictureHandler = new GarbagePictureCollector.TrashPictureHandler(this, 0) {
                @Override
                public void preExecute(Long... longs) {
                    loadingDialog.show();
                }

                @Override
                public void onProcessUpdate(Integer... integers) {
                    int index = observedObj.getObservedObjects().indexOf(deletedItems.get(integers[0] - 1));
                    observedObj.getObservedObjects().remove(index);
                    adapter.notifyItemRemoved(index);
                    ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
                    assert progressBar != null;
                    progressBar.setProgress((integers[0] * 100) / deletedItems.size());
                }

                @Override
                public void postExecute(Void res) {
                    loadingDialog.dismiss();
                    observedObj.setGridMode(GridMode.NORMAL);
                    int size = observedObj.getDataSize();
                    toolbar.setSubtitle(String.valueOf(size) + (size <= 1 ? " picture" : " pictures"));
                }
            };
            if (canExecute[0]) trashPictureHandler.execute(transferData);
            else Toast.makeText(this, "Don't have permission!", Toast.LENGTH_LONG).show();
        });

        Button btnDelete = bottomAction.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener((v) -> {
            long numberOfDeleted = observedObj.getNumberOfSelected();

            MaterialAlertDialogBuilder confirmDialog = new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete " + String.valueOf(numberOfDeleted) + (numberOfDeleted > 1 ? " pictures " : " picture ") + "permanently?")
                    .setNegativeButton("Cancel", (dialog, which) -> {
                    })
                    .setPositiveButton("Delete", (dialog, which) -> {
                        DeleteImage();
                    });
            confirmDialog.show();
        });

    }

    @Override
    public void onModeChange(GridModeEvent event) {
        selectedCounts.setText(R.string.select_items);
        if (event.getGridMode() == GridMode.NORMAL) {
            toolbar.setVisibility(View.VISIBLE);
            selectingToolbar.setVisibility(View.INVISIBLE);
            bottomAction.setVisibility(View.GONE);
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
        bottomAction.setVisibility(event.getNewSelectionForAll() ? View.VISIBLE : View.GONE);
        selectedCounts.setText(String.valueOf(observedObj.getNumberOfSelected()) + " selected");
    }
}
