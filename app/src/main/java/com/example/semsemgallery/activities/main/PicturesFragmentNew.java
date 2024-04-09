package com.example.semsemgallery.activities.main;

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
import com.example.semsemgallery.activities.main.adapter.GalleryAdapter;
import com.example.semsemgallery.activities.main.viewholders.DateHeaderItem;
import com.example.semsemgallery.activities.main.viewholders.GalleryItem;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Picture;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public class PicturesFragmentNew extends Fragment {
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
                    // Image capture failed or was canceleds
                    Toast.makeText(getActivity(), "Image capture failed", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }

    private boolean isDateEqual(Date value1, Date value2) {
        // Create calendar instances for both dates
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(value1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(value2);

        // Clear time parts from both calendars
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        // Compare the dates
        return cal1.equals(cal2);
    }

    private final ObservableGridMode<GalleryItem> observedData = new ObservableGridMode<>(null, GridMode.NORMAL);
    private final List<GalleryItem> dataList = new ArrayList<>();
    private final List<Picture> pictureList = new ArrayList<>();
    private final List<DateHeaderItem> headerItemList = new ArrayList<>();

    private int AddData(GalleryItem data) {
        int pos = Collections.binarySearch(dataList, data, new GalleryItem.GalleryItemComparator().reversed());
        pos = pos < 0 ? -pos - 1 : pos;
        dataList.add(pos, data);
        observedData.addData(data, pos);
        return pos;
    }

    private DateHeaderItem AddDataHeaderList(Picture pic) {
        Optional<DateHeaderItem> findingHeader = headerItemList.stream().filter(dateHeaderItem -> isDateEqual(pic.getDateTaken(), dateHeaderItem.getDate())).findFirst();
        if (findingHeader.isPresent()) return null;
        else {
            DateHeaderItem newData = new DateHeaderItem(pic.getDateTaken());
            int insertPosition = Collections.binarySearch(headerItemList, newData, Comparator.comparing(DateHeaderItem::getDate).reversed());
            insertPosition = insertPosition < 0 ? -insertPosition - 1 : insertPosition;
            headerItemList.add(insertPosition, newData);
            return newData;
        }
    }

    private void AddDataPictureList(Picture pic) {
        int position = Collections.binarySearch(pictureList, pic, Comparator.comparing(Picture::getDateTaken).reversed());
        position = position < 0 ? -position - 1 : position;
        pictureList.add(position, pic);
    }

    private int AddData(Picture pic) {
        AddDataPictureList(pic);
        DateHeaderItem temp = AddDataHeaderList(pic);
        if (temp != null) {
            return AddData(new GalleryItem(temp));
        }
        return AddData(new GalleryItem(pic));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        GalleryAdapter adapter = new GalleryAdapter(context, observedData);
        RecyclerView recyclerView = view.findViewById(R.id.picture_recycler_view);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL); // Adjust the span count as needed
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        PictureLoader loader = new PictureLoader(context) {
            @Override
            public void onProcessUpdate(Picture... pictures) {
                AddDataPictureList(pictures[0]);
                DateHeaderItem temp = AddDataHeaderList(pictures[0]);
                if (temp != null) {
                    adapter.notifyItemInserted(AddData(new GalleryItem(temp)));
                }

                adapter.notifyItemInserted(AddData(new GalleryItem(pictures[0])));
            }

            @Override
            public void postExecute(Boolean res) {

            }
        };

        loader.execute(PictureLoadMode.ALL.toString());
//        PictureLoader loader = new PictureLoader(context) {
//            @Override
//            public void onProcessUpdate(Picture... pictures) {
//                 adapter.notifyItemInserted(addPicture(pictures[0]));
//            }
//
//            @Override
//            public void postExecute(Boolean res) {
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//                Log.d("Loader", "Completed with " + observedData.getDataSize() + "items");
//                for (ObservableGridMode<Object>.DataItem item : observedData.getObservedObjects()) {
//                    if (item.data instanceof DateHeaderItem) {
//                        DateHeaderItem data = ((DateHeaderItem) item.data);
//                        Log.e("Loader", "Header: " + data.getDate().toString() + " - " + data.getItemCount() + "-" + data.getIndex());
//                    } else if (item.data instanceof Picture) {
//                        Log.i("Loader", "Photo: " + ((Picture) item.data).getDateTaken().toString() + " - " + ((Picture) item.data).getPictureId() + ((Picture) item.data).getFileSize() );
//                    }
//                }
//            }
//        };
//        loader.execute(PictureLoadMode.ALL.toString());

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

