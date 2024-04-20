package com.example.semsemgallery.activities.search;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.semsemgallery.activities.main2.adapter.FavoriteAdapter;
import com.example.semsemgallery.activities.main2.adapter.GalleryAdapter;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItem;
import com.example.semsemgallery.domain.Album.AlbumHandler;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.domain.TagUtils;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.models.Tag;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class SearchResultActivity extends AppCompatActivity implements GridModeListener {
    private ObservableGridMode<Picture> observedObj = new ObservableGridMode<>(null, GridMode.NORMAL);

    private FavoriteAdapter adapter = new FavoriteAdapter(this, observedObj);
    private int tagID;
    private String tagName;
    private com.google.android.material.appbar.MaterialToolbar topBar;
    private RecyclerView recyclerView;

    private PictureLoader loader = new PictureLoader(this) {
        @Override
        public void onProcessUpdate(Picture... pictures) {
            observedObj.addData(pictures[0]);
            adapter.notifyItemInserted(observedObj.getDataSize() - 1);
        }

        @Override
        public void postExecute(Boolean res) {

        }

        @Override
        public void preExecute(String... strings) {
            super.preExecute(strings);
            observedObj.reset();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_favorites);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ====== Get albumId & albumName from Intent
        Bundle bundle = getIntent().getExtras();
        tagID = bundle.getInt("tagId");
        tagName = bundle.getString("tagName");

        // Get UI component and set up
        recyclerView = findViewById(R.id.gallery_recycler);
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        // ====== Set title of Top bar
        topBar = (com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.topAppBar);
        topBar.setTitle(tagName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        topBar.setNavigationOnClickListener(v -> finish());
        // ====== Check and load picture from album
        loader.execute(PictureLoadMode.ID.toString(), tagName);

    }

    @Override
    public void onModeChange(GridModeEvent event) {

    }

    @Override
    public void onSelectionChange(GridModeEvent event) {

    }

    @Override
    public void onSelectingAll(GridModeEvent event) {

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