package com.example.semsemgallery.activities.album;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.adapter.GalleryAdapter;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItem;
import com.example.semsemgallery.domain.Album.AlbumHandler;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Picture;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class AlbumViewActivity extends AppCompatActivity {
    private final Context context = this;
    private ObservableGridMode<GalleryItem> observedObj = null;
    private TreeSet<GalleryItem> galleryItems = new TreeSet<>(Comparator.reverseOrder());
    private GalleryAdapter adapter = null;
    private String albumId;
    private String albumName;
    private int albumQuantity;
    private com.google.android.material.appbar.MaterialToolbar topBar;
    private List<Picture> pictureList;
    private RecyclerView recyclerView;
    private ArrayList<Uri> selectedImages;

    // ====== Activity Result Launcher for Photo Picker
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        selectedImages.clear();

                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            selectedImages.add(imageUri);
                        }

                        // ====== Open AlbumHandler Dialog after picking images
                        if (selectedImages.size() > 0) {
                            Log.d("AlbumViewActivity", "Select images from photo picker");
                        } else {
                            Toast.makeText(context, "No images selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private PictureLoader loader = new PictureLoader(this) {
        @Override
        public void onProcessUpdate(Picture... pictures) {
            galleryItems.add(new GalleryItem(pictures[0]));
        }

        @Override
        public void postExecute(Boolean res) {
            List<GalleryItem> pictures = new ArrayList<>(galleryItems);
            observedObj = new ObservableGridMode<>(pictures, GridMode.NORMAL);
            adapter = new GalleryAdapter(context, observedObj);
            recyclerView.setAdapter(adapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_album_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_album_view_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ====== Get albumId & albumName from Intent
        albumId = getIntent().getStringExtra("albumId");
        albumName = getIntent().getStringExtra("albumName");
        albumQuantity = getIntent().getIntExtra("albumQuantity", 0);


        // Get UI component and set up
        recyclerView = findViewById(R.id.activity_album_view_recycler);
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(manager);
        // ====== Check and load picture from album
        if (albumId != null) {
            loader.execute(PictureLoadMode.BY_ALBUM.toString(), albumId);
        }

        // ====== Set title of Top bar
        topBar = (com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.activity_album_view_topAppBar);
        topBar.setTitle(albumName);
        topBar.setSubtitle(String.valueOf(albumQuantity) + (albumQuantity == 0 ? " image" : " images"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        topBar.setNavigationOnClickListener(v -> finish());

        topBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.add) { // === Add images to this Album
                showPhotoPicker();
                return true;
            }
            else if (item.getItemId() == R.id.edit) { // === Change to selecting mode
                Log.d("AlbumViewActivity", "Edit");
                return true;
            }
            else if (item.getItemId() == R.id.select_all) { // === Select all images in this Album
                Log.d("AlbumViewActivity", "Select all");
                return true;
            }
            else if (item.getItemId() == R.id.rename) { // === Rename this Album
                showRenameDialog();
                return true;
            }

            return false;
        });
    }

    private void showPhotoPicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        activityResultLauncher.launch(photoPickerIntent);
    }

    private void showRenameDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.component_input_dialog, null);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setView(dialogView);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        TextView titleDialog = dialogView.findViewById(R.id.component_input_dialog_title);
        EditText inputName = dialogView.findViewById(R.id.component_input_dialog_name);
        MaterialButton cancelBtn = dialogView.findViewById(R.id.component_input_dialog_cancel);
        MaterialButton renameBtn = dialogView.findViewById(R.id.component_input_dialog_create);

        titleDialog.setText("Rename album");
        renameBtn.setText("Rename");

        // ====== Listener for CancelButton in InputDialog clicked
        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // ====== Listener for CreateButton in InputDialog clicked
        renameBtn.setOnClickListener(v -> {
            String newAlbumName = inputName.getText().toString();
            dialog.dismiss();

            // Log.d("AlbumViewActivity", albumName + " --- " + newAlbumName);
            if (!AlbumHandler.checkAlbumExists(context, newAlbumName)) {
                AlbumHandler.renameAlbum(context, albumName, newAlbumName);
            }
        });
    }

}