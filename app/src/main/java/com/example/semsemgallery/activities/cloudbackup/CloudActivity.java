package com.example.semsemgallery.activities.cloudbackup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.semsemgallery.models.Picture;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class CloudActivity extends AppCompatActivity implements GridModeListener {
    private final List<Picture> pictureList = new ArrayList<>();
    private final ObservableGridMode<Picture> observedObj = new ObservableGridMode<>(pictureList, GridMode.NORMAL);
    private final ItemAdapter adapter = new ItemAdapter(observedObj, this);
    // UI variable
    private MaterialToolbar toolbar;
    private MaterialToolbar selectingToolbar;
    private TextView selectedCounts;
    LinearProgressIndicator progressIndicator;
    private MaterialCheckBox selectAll;
    private Menu actionMenu;
    private LinearLayout bottomAction;
    private Button download, delete;
    private long size;
    private long uploadFileSize;

    private ArrayList<Uri> arrayList;
    private PictureDownloader pictureDownloader = new PictureDownloader(CloudActivity.this);

    FirebaseAuth auth = FirebaseAuth.getInstance();
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == RESULT_OK) {
                arrayList.clear();
                size = 0;
                if (o.getData() != null && o.getData().getClipData() != null) {
                    int count = o.getData().getClipData().getItemCount();

                    for (int i = 0; i < count; i++) {
                        Uri imageUri = o.getData().getClipData().getItemAt(i).getUri();
                        arrayList.add(imageUri);

                        // Calculate size using ContentResolver
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            if (inputStream != null) {
                                size += inputStream.available();
                                inputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!arrayList.isEmpty()) {
                        uploadImages(new ArrayList<>(), arrayList);
                    }
                }

            }
        }
    });
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cloud);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recently_delete_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in before using this feature", Toast.LENGTH_LONG);
            finish();
        }

        observedObj.addObserver(this);
        observedObj.setMaster(this);
        RecyclerView recyclerView = findViewById(R.id.deleted_item_recycler_view);

        arrayList = new ArrayList<>();
        FirebaseApp.initializeApp(CloudActivity.this);

        GridLayoutManager manager = new GridLayoutManager(this, 3);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        int spacing = 8;
        recyclerView.addItemDecoration(new RecylerViewItemDecoration(spacing));

        toolbar = findViewById(R.id.topAppBar);
        selectingToolbar = findViewById(R.id.topAppBar_SelectingMode);
        selectedCounts = findViewById(R.id.select_items);
        selectAll = findViewById(R.id.select_all);
        bottomAction = findViewById(R.id.bottomActions);
        download = findViewById(R.id.download_selected);
        delete = findViewById(R.id.delete_selected);

        StorageReference ref = FirebaseStorage.getInstance().getReference();
        FirebaseStorage.getInstance().getReference().child("images/" + auth.getCurrentUser().getUid()).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                size = 0;
                ArrayList<Picture> arrayList = new ArrayList<>();
                listResult.getItems().forEach(new Consumer<StorageReference>() {
                    @Override
                    public void accept(StorageReference storageReference) {
                        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String url = task.getResult().toString();
                                Picture image = new Picture(storageReference.getName(), url);
                                pictureList.add(image);
                                observedObj.addData(image);
                                adapter.notifyItemInserted(adapter.getItemCount() - 1);
                            }
                        });

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CloudActivity.this, "Failed to retrieve images", Toast.LENGTH_SHORT).show();
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Picture> pictures = observedObj.getSelectedItems();

                for (Picture p: pictures
                     ) {
                    pictureDownloader.execute(p);
                }

                observedObj.setGridMode(GridMode.NORMAL);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Picture> pictures = observedObj.getSelectedItems();
                for (Picture p :
                        pictures) {
                    StorageReference reference = ref.child("images/" + auth.getCurrentUser().getUid() + "/" + p.getFileName());
                    reference.delete();
                }
                recreate();
            }
        });

        toolbar.setOnMenuItemClickListener(menuItem -> {
            if(menuItem.getItemId() == R.id.cloudUpload){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(intent);
                recreate();
            }
            return false;
        });

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = selectAll.isChecked();
                observedObj.fireSelectionChangeForAll(isCheck);
            }
        });

        progressIndicator = findViewById(R.id.progress);

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

    private void uploadImages(@NonNull ArrayList<String> imagesUrl, ArrayList<Uri> imageUriList) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("images/" + auth.getCurrentUser().getUid());

        for (Uri uri : imageUriList) {
            StorageReference fileReference = storageReference.child(UUID.randomUUID().toString());

            fileReference.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnCompleteListener(task -> {
                        String url = Objects.requireNonNull(task.getResult()).toString();
                        imagesUrl.add(url);

                        if (imagesUrl.size() == imageUriList.size()) {
                            // All images uploaded
                            recreate();
                            Toast.makeText(this, "Images uploaded successfully!", Toast.LENGTH_SHORT).show();
                            progressIndicator.setProgress(0);
                        }
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            if (inputStream != null) {
                                uploadFileSize += inputStream.available();
                                inputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

//                        recreate();
                        Picture picture = new Picture(uri);
                        size += new File(uri.getPath()).length();
                        pictureList.add(picture);
                        observedObj.addData(picture);
                        adapter.notifyItemInserted(adapter.getItemCount() - 1);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to upload images", Toast.LENGTH_SHORT).show();
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            progressIndicator.setMax(Math.toIntExact(size));
                            progressIndicator.setProgress(Math.toIntExact(uploadFileSize + snapshot.getBytesTransferred()));
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        if (observedObj.getCurrentMode() == GridMode.NORMAL)
            super.onBackPressed();
        else {
            observedObj.setGridMode(GridMode.NORMAL);
        }
    }
}
