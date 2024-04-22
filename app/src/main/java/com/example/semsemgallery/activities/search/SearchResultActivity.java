package com.example.semsemgallery.activities.search;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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
import com.example.semsemgallery.activities.cloudbackup.CloudActivity;
import com.example.semsemgallery.activities.main2.MainActivity;
import com.example.semsemgallery.activities.main2.adapter.FavoriteAdapter;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItem;
import com.example.semsemgallery.activities.pictureview.ChooseAlbumActivity;
import com.example.semsemgallery.activities.pictureview.fragment.AddTagBottomSheet;
import com.example.semsemgallery.domain.Album.AlbumHandler;
import com.example.semsemgallery.domain.Picture.GarbagePictureCollector;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Picture;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity implements GridModeListener {
    private RecyclerView recyclerView;
    private LinearLayout actionBar;
    private MaterialToolbar selectingTopBar;
    private MaterialToolbar topBar;
    private boolean isSelectingAll = false;
    private String choiceHandler = "";
    private final ArrayList<Uri> selectedImages = new ArrayList<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private ProgressBar myProgressBar;
    private ObservableGridMode<Picture> observedObj = new ObservableGridMode<>(null, GridMode.NORMAL);

    private SearchAdapter adapter;
    private int tagID;
    private String tagName;

    private PictureLoader loader = new PictureLoader(this) {
        @Override
        public void onProcessUpdate(Picture... pictures) {
            if (pictures != null && pictures.length > 0 && pictures[0] != null) {
                observedObj.addData(pictures[0]);
                adapter.notifyItemInserted(observedObj.getDataSize() - 1);
            }
//            observedObj.addData(pictures[0]);
//            adapter.notifyItemInserted(observedObj.getDataSize() - 1);
        }

        @Override
        public void postExecute(Boolean res) {

        }

        @Override
        public void preExecute(String... strings) {
            super.preExecute(strings);
            observedObj.reset();
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        observedObj.addObserver(this);
        observedObj.setMaster(this);

        setContentView(R.layout.fragment_search_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getOnBackPressedDispatcher().addCallback(SearchResultActivity.this, backHandler);
        // ====== Get albumId & albumName from Intent
        Bundle bundle = getIntent().getExtras();
        tagID = bundle.getInt("tagId");
        tagName = bundle.getString("tagName");

        adapter = new SearchAdapter(this, observedObj, tagName);
        actionBar = findViewById(R.id.action_bar);
        selectingTopBar = findViewById(R.id.selecting_top_bar);
        // Get UI component and set up
        recyclerView = findViewById(R.id.gallery_recycler);
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        // ====== Set title of Top bar
        topBar = (com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.topAppBar);
        topBar.setTitle(tagName);
        topBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.search) {
                startActivity(new Intent(getApplicationContext(), SearchViewActivity.class));
                return true;
            } else if (item.getItemId() == R.id.cloud) {
                startActivity(new Intent(getApplicationContext(), CloudActivity.class));
                return true;
            } else if (item.getItemId() == R.id.edit) {
                observedObj.setGridMode(GridMode.SELECTING);
                return true;
            } else if (item.getItemId() == R.id.select_all) {
                observedObj.setGridMode(GridMode.SELECTING);
                observedObj.fireSelectionChangeForAll(true);
                isSelectingAll = false;
                return true;
            } else return false;
        });
        SetFunctionForActionBar();
    }

    private OnBackPressedCallback backHandler = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Log.d("BackPressed", observedObj.getCurrentMode().toString());
            if (observedObj.getCurrentMode() == GridMode.SELECTING) {
                observedObj.fireSelectionChangeForAll(false);
                observedObj.setGridMode(GridMode.NORMAL);
                isSelectingAll = false;
            } else {
                // If not in selecting mode, finish the activity
                finish();
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        topBar.setNavigationOnClickListener(v -> finish());
        observedObj.fireSelectionChangeForAll(false);
        // ====== Check and load picture from album
        if (!isFinishing()) {
            loader.execute(PictureLoadMode.ID.toString(), tagName);
        }
    }

    @Override
    public void onModeChange(GridModeEvent event) {
        if (event.getGridMode() == GridMode.NORMAL) {
            actionBar.setVisibility(View.GONE);
            topBar.setVisibility(View.VISIBLE);
            selectingTopBar.setVisibility(View.INVISIBLE);
        } else if (event.getGridMode() == GridMode.SELECTING) {
            actionBar.setVisibility(View.VISIBLE);
            topBar.setVisibility(View.INVISIBLE);
            selectingTopBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSelectionChange(GridModeEvent event) {
        long quantity = observedObj.getNumberOfSelected();
        selectingTopBar.setTitle(quantity == 0 ? "Select items" : quantity + " selected");
    }

    @Override
    public void onSelectingAll(GridModeEvent event) {
        if (event.getNewSelectionForAll()) {
            selectingTopBar.setTitle(observedObj.getDataSize() + " selected");
        } else {
            selectingTopBar.setTitle(String.valueOf(0) + " selected");
        }
    }
    private AlertDialog myLoadingDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.component_loading_dialog, null);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this).setView(dialogView);

        AlertDialog dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);

        Button cancelButton = dialogView.findViewById(R.id.component_loading_dialog_cancelButton);
        cancelButton.setOnClickListener(v -> {
            AlbumHandler.stopHandling();
        });

        return dialog;
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intentResult = result.getData();
                        if (intentResult != null) {
                            String albumName = intentResult.getStringExtra("key");

                            selectedImages.clear();

                            for (Picture pictureItem : observedObj.getSelectedItems()) {
                                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, pictureItem.getPictureId());
                                selectedImages.add(imageUri);
                            }

                            if (choiceHandler == "copy" & !selectedImages.isEmpty()) {
                                AlertDialog loadingDialog = myLoadingDialog();
                                loadingDialog.show(); // Show Loading Dialog
                                TextView dialogTitle = loadingDialog.findViewById(R.id.component_loading_dialog_title);
                                dialogTitle.setText("Copying to " + albumName);

                                AlbumHandler.OnLoadingListener loadingListener = new AlbumHandler.OnLoadingListener() {
                                    @Override
                                    public void onLoadingComplete() {
                                        mHandler.post(()-> {
                                            loadingDialog.dismiss();
                                            observedObj.fireSelectionChangeForAll(false);
                                            observedObj.setGridMode(GridMode.NORMAL);
                                            isSelectingAll = false;
                                        });
                                    }

                                    @Override
                                    public void onLoadingProgressUpdate(int progress) {
                                        ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
                                        mHandler.post(() -> {
                                            progressBar.setProgress(progress);
                                        });
                                    }

                                    @Override
                                    public void onLoadingException() {
                                        mHandler.post(() -> {
                                            showExceptionHandlerDialog();
                                        });
                                    }
                                };

                                AlbumHandler.copyImagesToAlbumHandler(getApplicationContext(), selectedImages, albumName, loadingListener);
                            } else if (choiceHandler == "move") {
                                AlertDialog loadingDialog = myLoadingDialog();
                                loadingDialog.show(); // Show Loading Dialog
                                TextView dialogTitle = loadingDialog.findViewById(R.id.component_loading_dialog_title);
                                dialogTitle.setText("Moving to " + albumName);

                                AlbumHandler.OnLoadingListener loadingListener = new AlbumHandler.OnLoadingListener() {
                                    @Override
                                    public void onLoadingComplete() {
                                        mHandler.post(()-> {
                                            loadingDialog.dismiss();
                                            observedObj.fireSelectionChangeForAll(false);
                                            observedObj.setGridMode(GridMode.NORMAL);
                                            isSelectingAll = false;
                                        });
                                    }

                                    @Override
                                    public void onLoadingProgressUpdate(int progress) {
                                        ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
                                        mHandler.post(() -> {
                                            progressBar.setProgress(progress);
                                        });
                                    }

                                    @Override
                                    public void onLoadingException() {
                                        mHandler.post(() -> {
                                            showExceptionHandlerDialog();
                                        });
                                    }
                                };

                                AlbumHandler.moveImagesToAlbumHandler(getApplicationContext(), selectedImages, albumName, loadingListener);
                            }

                        }
                    }
                }
            }
    );

    private void showExceptionHandlerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.component_exception_handler_dialog, null);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this).setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        CheckBox applyCheckbox = dialogView.findViewById(R.id.component_exception_handler_dialog_checkbox);
        TextView skipBtn = dialogView.findViewById(R.id.component_exception_handler_dialog_skip);
        TextView replaceBtn = dialogView.findViewById(R.id.component_exception_handler_dialog_replace);
        TextView description = dialogView.findViewById(R.id.component_exception_handler_dialog_description);
        description.setText("There is already an item name " + AlbumHandler.getFileName(this, AlbumHandler.currentDuplicateImageUri) + " in the selected album");

        applyCheckbox.setOnClickListener(v -> {
            AlbumHandler.isApplyToAll = applyCheckbox.isChecked();
        });

        // ====== Listener for CancelButton in Delete Confirm Dialog clicked
        skipBtn.setOnClickListener(v -> {
            Log.d("AlbumsFM", "Skip " + AlbumHandler.getFileName(this, AlbumHandler.currentDuplicateImageUri));
            dialog.dismiss();
            AlbumHandler.duplicateHandleChoice = "skip";
            AlbumHandler.isDuplicateHandling = false;
        });

        // ====== Listener for DeleteButton in Delete Confirm Dialog clicked
        replaceBtn.setOnClickListener(v -> {
            Log.d("AlbumsFM", "Replace " + AlbumHandler.getFileName(this, AlbumHandler.currentDuplicateImageUri));
            dialog.dismiss();
            AlbumHandler.duplicateHandleChoice = "replace";
            AlbumHandler.isDuplicateHandling = false;
        });
    }

    private void renderMoreMenu(View v, int res) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(res);
        MenuItem btnSelectAll = popupMenu.getMenu().findItem(R.id.btnSelectAll);
        if (isSelectingAll) btnSelectAll.setTitle(getString(R.string.unselect_all));
        else btnSelectAll.setTitle(R.string.select_all);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.btnMoveToAlbum) {
                //#TODO
                if (observedObj.getNumberOfSelected() < 1) {
                    Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show();
                    return false;
                }

                choiceHandler = "move";
                Intent chooseAlbumIntent = new Intent(this, ChooseAlbumActivity.class);
                activityResultLauncher.launch(chooseAlbumIntent);
                return true;
            } else if (id == R.id.btnCopyToAlbum) {
                //#TODO
                if (observedObj.getNumberOfSelected() < 1) {
                    Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show();
                    return false;
                }

                choiceHandler = "copy";
                Intent chooseAlbumIntent = new Intent(this, ChooseAlbumActivity.class);
                activityResultLauncher.launch(chooseAlbumIntent);
                return true;
            } else if (id == R.id.btnSelectAll) {
                isSelectingAll = !isSelectingAll;
                observedObj.fireSelectionChangeForAll(isSelectingAll);
                return true;
            } else return false;
        });
        popupMenu.show();
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

    private void ProcessTrashPicture() {
        AlertDialog loadingDialog = createDialog("Moving images to Trash", false, null);
        //== Prepare data and handler
        List<ObservableGridMode<Picture>.DataItem> temp = observedObj.getSelectedDataItem();
        Long[] deleteIds = new Long[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            deleteIds[i] = temp.get(i).data.getPictureId();
        }
        final boolean[] canExecute = {false};
        boolean isStorageManager = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            isStorageManager = Environment.isExternalStorageManager();
        }

        if (isStorageManager) {
            // Your app already has storage management permissions
            // You can proceed with file operations
            canExecute[0] = true;
        } else {
            // Your app does not have storage management permissions
            // Guide the user to the system settings page to grant permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);

            startActivity(intent);
        }

        GarbagePictureCollector.TrashPictureHandler collector = new GarbagePictureCollector.TrashPictureHandler(this) {
            @Override
            public void preExecute(Long... longs) {
                Log.i("TrashImage", "Prepare trash");
                loadingDialog.show();
            }

            @Override
            public void onProcessUpdate(Integer... integers) {
                if (integers == null) return;
                Log.i("TrashImage", "Trashed " + integers[0] + " / " + temp.size());
                int index = observedObj.getObservedObjects().indexOf(temp.get(integers[0] - 1));
                observedObj.getObservedObjects().remove(temp.get(integers[0] - 1));
                adapter.notifyItemRemoved(index);
                ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
                assert progressBar != null;
                progressBar.setProgress((integers[0] * 100) / temp.size());

            }

            @Override
            public void postExecute(Void res) {
                Log.i("TrashImage", "Completely trashed");
                observedObj.setGridMode(GridMode.NORMAL);
                isSelectingAll = false;
                loadingDialog.dismiss();
            }
        };
        if (canExecute[0]) collector.execute(deleteIds);
        else Toast.makeText(this, "Don't have permission", Toast.LENGTH_LONG);
    }

    private final View.OnClickListener trashPicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (observedObj.getNumberOfSelected() == 0) return;
            //= Loading dialog (without cancellation)
            MaterialAlertDialogBuilder confirmDialog = new MaterialAlertDialogBuilder(SearchResultActivity.this)
                    .setTitle("Move " + observedObj.getNumberOfSelected() + " to Trash?")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("Move to Trash", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ProcessTrashPicture();
                        }
                    });
            confirmDialog.show();
        }
    };

    private void SetFunctionForActionBar() {
        Button btnDelete = actionBar.findViewById(R.id.btnDelete);
        Button btnShare = actionBar.findViewById(R.id.btnShare);
        Button btnMore = actionBar.findViewById(R.id.btnMore);
        btnDelete.setOnClickListener(this.trashPicture);
        btnShare.setOnClickListener(v -> {
            if (observedObj.getNumberOfSelected() == 0) return;

            //== Retrieve the data for sharing
            List<Picture> pictures = observedObj.getSelectedItems();
            ArrayList<Uri> shareFiles = new ArrayList<>();
            for (Picture item : pictures) {
                File shareFile = new File(item.getPath());
                Uri shareUri = FileProvider.getUriForFile(
                        getApplicationContext(),
                        getApplicationContext().getPackageName() + ".provider",
                        shareFile
                );
                shareFiles.add(shareUri);
            }

            //==Start the new activity
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareFiles);
            startActivity(Intent.createChooser(shareIntent, "Share images via..."));

        });

        btnMore.setOnClickListener((v) -> {
            renderMoreMenu(v, R.menu.pictures_fragment_selecting_mode);
        });
    }

//    private void showPhotoPicker() {
//        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        photoPickerIntent.setType("image/*");
//        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//        activityResultLauncher.launch(photoPickerIntent);
//    }
//
//    // ====== Show Images Handler Dialog
//    private void showImagesHandlerDialog() {
//        View dialogView = getLayoutInflater().inflate(R.layout.component_album_handler_dialog, null);
//        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setView(dialogView);
//
//        AlertDialog dialog = dialogBuilder.create();
//        dialog.show();
//
//        TextView titleDialog = dialogView.findViewById(R.id.component_album_handler_dialog_title);
//        TextView cancelBtn = dialogView.findViewById(R.id.component_album_handler_dialog_cancel);
//        TextView copyBtn = dialogView.findViewById(R.id.component_album_handler_dialog_copy);
//        TextView moveBtn = dialogView.findViewById(R.id.component_album_handler_dialog_move);
//
//        titleDialog.setText("Handle options");
//
//        // ====== Listener for CancelButton in AlbumHandlerDialog clicked
//        cancelBtn.setOnClickListener(v -> {
//            dialog.dismiss();
//        });
//
//        // ====== Listener for CopyButton in AlbumHandlerDialog clicked
//        copyBtn.setOnClickListener(v -> {
//            dialog.dismiss();
//
//            AlertDialog loadingDialog = myLoadingDialog();
//            loadingDialog.show(); // Show Loading Dialog
//            TextView dialogTitle = loadingDialog.findViewById(R.id.component_loading_dialog_title);
//            dialogTitle.setText("Copying items to " + albumName);
//
//            // Create a listener for completion & set progress
//            AlbumHandler.OnLoadingListener loadingListener = new AlbumHandler.OnLoadingListener() {
//                @Override
//                public void onLoadingComplete() {
//                    loadingDialog.dismiss();
//                    // ====== Check and load picture from album
//                    if (albumId != null) {
//                        loader.execute(PictureLoadMode.BY_ALBUM.toString(), albumId);
//                    }
//                }
//
//                @Override
//                public void onLoadingProgressUpdate(int progress) {
//                    ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
//                    mHandler.post(() -> {
//                        progressBar.setProgress(progress);
//                    });
//                }
//            };
//
//            AlbumHandler.copyImagesToAlbumHandler(context, selectedImages, albumName, loadingListener);
//        });
//
//
//        // ====== Listener for MoveButton in AlbumHandlerDialog clicked
//        moveBtn.setOnClickListener(v -> {
//            dialog.dismiss();
//
//            AlertDialog loadingDialog = myLoadingDialog();
//            loadingDialog.show(); // Show Loading Dialog
//            TextView dialogTitle = loadingDialog.findViewById(R.id.component_loading_dialog_title);
//            dialogTitle.setText("Moving items to " + albumName);
//
//            // Create a listener for completion & set progress
//            AlbumHandler.OnLoadingListener loadingListener = new AlbumHandler.OnLoadingListener() {
//                @Override
//                public void onLoadingComplete() {
//                    loadingDialog.dismiss();
//                    // ====== Check and load picture from album
//                    if (albumId != null) {
//                        loader.execute(PictureLoadMode.BY_ALBUM.toString(), albumId);
//                    }
//                }
//
//                @Override
//                public void onLoadingProgressUpdate(int progress) {
//                    ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
//                    mHandler.post(() -> {
//                        progressBar.setProgress(progress);
//                    });
//                }
//            };
//
//            AlbumHandler.moveImagesToAlbumHandler(context, selectedImages, albumName, loadingListener);
//        });
//    }
//
//
//    // ==== Just only able to init Dialog in Fragment/Activity
//    private AlertDialog myLoadingDialog() {
//        View dialogView = getLayoutInflater().inflate(R.layout.component_loading_dialog, null);
//        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setView(dialogView);
//
//        AlertDialog dialog = dialogBuilder.create();
//        dialog.setCanceledOnTouchOutside(false);
//
//        Button cancelButton = dialogView.findViewById(R.id.component_loading_dialog_cancelButton);
//        cancelButton.setOnClickListener(v -> {
//            AlbumHandler.stopHandling();
//        });
//
//        return dialog;
//    }
//
//
//    private void showRenameDialog() {
//        View dialogView = getLayoutInflater().inflate(R.layout.component_input_dialog, null);
//        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setView(dialogView);
//
//        AlertDialog dialog = dialogBuilder.create();
//        dialog.show();
//
//        TextView titleDialog = dialogView.findViewById(R.id.component_input_dialog_title);
//        EditText inputName = dialogView.findViewById(R.id.component_input_dialog_name);
//        MaterialButton cancelBtn = dialogView.findViewById(R.id.component_input_dialog_cancel);
//        MaterialButton renameBtn = dialogView.findViewById(R.id.component_input_dialog_create);
//
//        titleDialog.setText("Rename album");
//        renameBtn.setText("Rename");
//
//        // ====== Listener for CancelButton in InputDialog clicked
//        cancelBtn.setOnClickListener(v -> {
//            dialog.dismiss();
//        });
//
//        // ====== Listener for CreateButton in InputDialog clicked
//        renameBtn.setOnClickListener(v -> {
//            String newAlbumName = inputName.getText().toString();
//            dialog.dismiss();
//
//            if (!AlbumHandler.checkAlbumExists(context, newAlbumName)) {
//                AlbumHandler.renameAlbum(context, albumName, newAlbumName);
//            }
//            albumName = newAlbumName;
//            topBar.setTitle(albumName);
//        });
//    }

}