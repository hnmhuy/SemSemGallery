package com.example.semsemgallery.activities.main2.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.adapter.GalleryAdapter;
import com.example.semsemgallery.activities.main2.viewholder.DateHeaderItem;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItem;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Picture;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class PicturesFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri finalUri;
    private Context context;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getExtras() != null) {
                        try {
                            InputStream inputStream = getActivity().getContentResolver().openInputStream(finalUri);
                            Bitmap highQualityBitmap = BitmapFactory.decodeStream(inputStream);
                            createImage(getActivity().getApplicationContext(), highQualityBitmap, finalUri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "Image capture failed", Toast.LENGTH_SHORT).show();
                }
            });


    private final TreeSet<GalleryItem> galleryItems = new TreeSet<>(Comparator.reverseOrder());
    private final TreeSet<Picture> pictureList = new TreeSet<>(Comparator.reverseOrder());
    private final TreeSet<Long> header = new TreeSet<>(Comparator.reverseOrder());
    private List<GalleryItem> dataList = null;
    private ObservableGridMode<GalleryItem> observableGridMode = null;
    private GalleryAdapter adapter = null;
    private PictureLoader loader;
    private RecyclerView recyclerView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        loader = new PictureLoader(context) {
            @Override
            public void onProcessUpdate(Picture... pictures) {
                galleryItems.add(new GalleryItem(pictures[0]));

                header.add(pictures[0].getDateInMillis());
                pictureList.add(pictures[0]);
            }

            @Override
            public void postExecute(Boolean res) {
                List<Long> temp = new ArrayList<>(header);
                for (Long item : temp) {

                    DateHeaderItem i = new DateHeaderItem(new Date(item * (1000 * 86400)));
                    Log.d("Header", "Data: " + item);
                    galleryItems.add(new GalleryItem(i));
                }
                dataList = new ArrayList<>(galleryItems);
                observableGridMode = new ObservableGridMode<>(dataList, GridMode.NORMAL);
                adapter = new GalleryAdapter(context, observableGridMode, new ArrayList<>(pictureList));
                recyclerView.setAdapter(adapter);
            }
        };

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        recyclerView = view.findViewById(R.id.picture_recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL); // Adjust the span count as needed
        recyclerView.setLayoutManager(layoutManager);
        loader.execute(PictureLoadMode.ALL.toString());
        return view;
    }

    public static void createImage(Context context, Bitmap bitmap, Uri finalUri) {
        OutputStream outputStream;
        ContentResolver contentResolver = context.getContentResolver();
        try {
            outputStream = contentResolver.openOutputStream(Objects.requireNonNull(finalUri));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Objects.requireNonNull(outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

