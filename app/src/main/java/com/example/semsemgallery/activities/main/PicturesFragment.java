package com.example.semsemgallery.activities.main;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.cloudbackup.CloudActivity;
import com.example.semsemgallery.activities.main.adapter.PicturesByDateRecyclerAdapter;
import com.example.semsemgallery.activities.search.SearchViewActivity;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.domain.MediaRetriever;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PicturesFragment extends Fragment {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    Uri finalUri;
    private Context applicationContext;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getExtras() != null) {
                        try{
                            InputStream inputStream = getActivity().getContentResolver().openInputStream(finalUri);
                            Bitmap highQualityBitmap = BitmapFactory.decodeStream(inputStream);
                            createImage(getActivity().getApplicationContext(), highQualityBitmap, finalUri);
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                } else {
                    // Image capture failed or was canceled
                    Toast.makeText(getActivity(), "Image capture failed", Toast.LENGTH_SHORT).show();
                }
            });
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        applicationContext = context.getApplicationContext();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FirebaseApp.initializeApp(requireContext());

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        List<Picture> picturesRetriever = new MediaRetriever(appCompatActivity).getAllPictureList();

        ContentResolver contentResolver = applicationContext.getContentResolver();
        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        Map<LocalDate, List<Picture>> partitionedMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for(Picture picture: picturesRetriever) {
            LocalDate dateAdded = picture.getDateTaken().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String dateString = dateAdded.format(formatter);
            LocalDate formattedDate = LocalDate.parse(dateString, formatter);

            if(partitionedMap.containsKey(formattedDate)) {
                Objects.requireNonNull(partitionedMap.get(formattedDate)).add(picture);
            } else {
                List<Picture> newList = new ArrayList<>();
                newList.add(picture);
                partitionedMap.put(formattedDate, newList);
            }
        }
        List<List<Picture>> picturesByDateAdded = new ArrayList<>(partitionedMap.values());
        Collections.sort(picturesByDateAdded, (list1, list2) -> {
            LocalDate date1 = list1.get(0).getDateTaken().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate date2 = list2.get(0).getDateTaken().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return date2.compareTo(date1); // descending order
        });

        RecyclerView recyclerView = view.findViewById(R.id.picture_recycler_view);
        FloatingActionButton openCamera;
        openCamera = view.findViewById(R.id.add_fab);
        PicturesByDateRecyclerAdapter adapter = new PicturesByDateRecyclerAdapter(picturesByDateAdded, appCompatActivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(appCompatActivity));
        recyclerView.setAdapter(adapter);

        MaterialToolbar toolbar = (MaterialToolbar) view.findViewById(R.id.topAppBar);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId() == R.id.search){
                    startActivity(new Intent(getActivity().getApplicationContext(), SearchViewActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.cloud){
                    startActivity(new Intent(getActivity().getApplicationContext(), CloudActivity.class));
                    return true;
                }
                return false;
            }
        });
        if(auth.getCurrentUser() == null){
            Menu menu = toolbar.getMenu();
            menu.removeItem(R.id.cloud);
        }
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = null;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                } else {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }

                if(uri == null){
                    uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                }


                // Create an intent to capture an image
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String img = String.valueOf(Calendar.getInstance().getTimeInMillis());
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Images.Media.DISPLAY_NAME, img);
                cv.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/" + "Camera");
                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                finalUri = contentResolver.insert(uri, cv);

                // Add the URI as an extra to the intent
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, finalUri);

                // If there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Launch the camera activity
                    activityResultLauncher.launch(takePictureIntent);
                } else {
                    // Handle the case where the device does not have a camera
                    Toast.makeText(getActivity(), "No camera available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    public static void createImage(Context context, Bitmap bitmap, Uri finalUri){
        OutputStream outputStream;
        ContentResolver contentResolver = context.getContentResolver();
        try{
            outputStream = contentResolver.openOutputStream(Objects.requireNonNull(finalUri));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Objects.requireNonNull(outputStream);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
}

