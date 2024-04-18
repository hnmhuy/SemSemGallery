package com.example.semsemgallery.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.cloudbackup.ImageAdapter;
import com.example.semsemgallery.models.ImageFromCloud;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.function.Consumer;

public class DownloadFromCloudActivity extends AppCompatActivity {
    StorageReference storageReference;
    LinearProgressIndicator progress;
    Uri image;
    Button selectImage, uploadImage;
    ImageView imageView;

    private final ActivityResultLauncher<Intent> activityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK){
                if(result.getData()!=null){
                    uploadImage.setEnabled(true);
                    image = result.getData().getData();
                    Glide.with(getApplicationContext()).load(image).into(imageView);
                }
            } else {
                Toast.makeText(DownloadFromCloudActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_download_from_cloud);
        FirebaseApp.initializeApp(DownloadFromCloudActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView recyclerView = findViewById(R.id.recyclerOfCloud);
        setSupportActionBar(toolbar);

        FirebaseStorage.getInstance().getReference().child("images").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                ArrayList<ImageFromCloud> arrayList = new ArrayList<>();
                ImageAdapter imageAdapter = new ImageAdapter(DownloadFromCloudActivity.this, arrayList);
                imageAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(ImageFromCloud image) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(image.getUrl()));
                        intent.setDataAndType(Uri.parse(image.getUrl()), "image/*");
                        startActivity(intent);
                    }
                });
                recyclerView.setAdapter(imageAdapter);
                listResult.getItems().forEach(new Consumer<StorageReference>() {
                    @Override
                    public void accept(StorageReference storageReference) {
                        ImageFromCloud image = new ImageFromCloud();
                        image.setName(storageReference.getName());
                        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String url = "https://" + task.getResult().getEncodedAuthority() + task.getResult().getEncodedPath()
                                        + "?alt=media&token=" + task.getResult().getQueryParameters("token").get(0);
                                image.setUrl(url);
                                arrayList.add(image);
                                imageAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });

//        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        progress = findViewById(R.id.progress);
//        imageView = findViewById(R.id.imageView);
//        uploadImage = findViewById(R.id.uploadImage);
//        selectImage = findViewById(R.id.selectImage);
//        selectImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                activityResult.launch(intent);
//            }
//        });
//
//        uploadImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                uploadImage(image);
//            }
//        });
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
//    private void uploadImage(Uri image) {
//        StorageReference reference = storageReference.child("images/" + UUID.randomUUID().toString());
//
//        // Create metadata for the image
//        StorageMetadata metadata = new StorageMetadata.Builder()
//                .setCustomMetadata("path", path) // Set your custom metadata properties here
//                .setCustomMetadata("fileName", fileName)
//                .setCustomMetadata("dateAdded", String.valueOf(dateAdded.getTime())) // Convert Date to string
//                .setCustomMetadata("albumID", albumID)
//                .setCustomMetadata("isFav", String.valueOf(isFav))
//                .build();
//
//        // Upload the image with metadata
//        reference.putFile(image, metadata)
//                .addOnSuccessListener(taskSnapshot -> {
//                    Toast.makeText(MainActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(MainActivity.this, "Error while uploading image", Toast.LENGTH_SHORT).show();
//                })
//                .addOnProgressListener(snapshot -> {
//                    progress.setMax(Math.toIntExact(snapshot.getTotalByteCount()));
//                    progress.setProgress(Math.toIntExact(snapshot.getBytesTransferred()));
//                });
//    }

}