package com.example.semsemgallery.activities.main2.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.GridModeEvent;
import com.example.semsemgallery.activities.base.GridModeListener;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.cloudbackup.CloudActivity;
import com.example.semsemgallery.activities.interfaces.FragmentCallBack;
import com.example.semsemgallery.activities.main2.MainActivity;
import com.example.semsemgallery.activities.main2.adapter.GalleryAdapter;
import com.example.semsemgallery.activities.main2.viewholder.DateHeaderItem;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItem;
import com.example.semsemgallery.activities.search.SearchViewActivity;
import com.example.semsemgallery.domain.Picture.GarbagePictureCollector;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Picture;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;


public class PicturesFragment extends Fragment implements FragmentCallBack, GridModeListener {
    public static String TAG = "PicturesFragment";
    FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri finalUri;
    private Context context;
    private MainActivity mainActivity;
    private LinearLayout actionBar;
    private MaterialToolbar selectingTopBar;
    private MaterialToolbar topBar;
    private final TreeSet<GalleryItem> galleryItems = new TreeSet<>(Comparator.reverseOrder());
    private final TreeSet<Long> header = new TreeSet<>(Comparator.reverseOrder());
    private List<GalleryItem> dataList = null;
    private final ObservableGridMode<GalleryItem> observableGridMode = new ObservableGridMode<>(null, GridMode.NORMAL);
    ;
    private GalleryAdapter adapter = null;
    private PictureLoader loader;
    private RecyclerView recyclerView;

    private final ActivityResultLauncher<Intent> activityCameraResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loader.execute(PictureLoadMode.ALL.toString());
//
//                    Intent data = result.getData();
//                    if (data != null && data.getExtras() != null) {
//                        try {
//                            InputStream inputStream = getActivity().getContentResolver().openInputStream(finalUri);
//                            Bitmap highQualityBitmap = BitmapFactory.decodeStream(inputStream);
//                            createImage(getActivity().getApplicationContext(), highQualityBitmap, finalUri);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                    }
                } else {
                    // Image capture failed or was canceled
                    PicturesFragment.this.context.getContentResolver().delete(finalUri, null, null);
                    Toast.makeText(getActivity(), "Image capture failed", Toast.LENGTH_SHORT).show();
                }
            });

    private OnBackPressedCallback backHandler = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Log.d("BackPressed", observableGridMode.getCurrentMode().toString());
            if (observableGridMode.getCurrentMode() == GridMode.SELECTING) {
                observableGridMode.fireSelectionChangeForAll(false);
                observableGridMode.setGridMode(GridMode.NORMAL);
            } else {
                // If not in selecting mode, finish the activity
                mainActivity.finish();
            }
        }
    };


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        adapter = new GalleryAdapter(context, observableGridMode);
        loader = new PictureLoader(context) {
            @Override
            public void preExecute(String... strings) {
                super.preExecute(strings);
                observableGridMode.reset();
                galleryItems.clear();
            }

            @Override
            public void onProcessUpdate(Picture... pictures) {
                galleryItems.add(new GalleryItem(pictures[0]));
                header.add(pictures[0].getDateInMillis());
            }

            @Override
            public void postExecute(Boolean res) {
                List<Long> temp = new ArrayList<>(header);
                for (Long item : temp) {

                    DateHeaderItem i = new DateHeaderItem(new Date(item));
                    galleryItems.add(new GalleryItem(i));
                }
                dataList = new ArrayList<>(galleryItems);
                for (int i = 0; i < dataList.size(); i++) {
                    observableGridMode.addData(dataList.get(i));
                }
                adapter.notifyDataSetChanged();
            }
        };

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mainActivity = (MainActivity) getActivity();
        this.observableGridMode.addObserver(this);
        this.observableGridMode.setMaster(this);
        mainActivity.getOnBackPressedDispatcher().addCallback(
                mainActivity, backHandler
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        recyclerView = view.findViewById(R.id.picture_recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL); // Adjust the span count as needed
        recyclerView.setLayoutManager(layoutManager);
        actionBar = view.findViewById(R.id.action_bar);
        SetFunctionForActionBar();
        topBar = view.findViewById(R.id.topAppBar);
        selectingTopBar = view.findViewById(R.id.selecting_top_bar);
        recyclerView.setAdapter(adapter);
        FloatingActionButton openCamera;
        openCamera = view.findViewById(R.id.add_fab);

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                } else {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }

                if (uri == null) {
                    uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                }


                // Create an intent to capture an image
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String img = String.valueOf(Calendar.getInstance().getTimeInMillis());
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Images.Media.DISPLAY_NAME, img);
                cv.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/" + "Camera");
                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                finalUri = context.getContentResolver().insert(uri, cv);

                // Add the URI as an extra to the intent
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, finalUri);

                // If there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Launch the camera activity
                    activityCameraResult.launch(takePictureIntent);
                } else {
                    // Handle the case where the device does not have a camera
                    Toast.makeText(getActivity(), "No camera available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (auth.getCurrentUser() == null) {
            Menu menu = topBar.getMenu();
            menu.removeItem(R.id.cloud);
        }

        topBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.search) {
                    startActivity(new Intent(getActivity().getApplicationContext(), SearchViewActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.cloud) {
                    startActivity(new Intent(getActivity().getApplicationContext(), CloudActivity.class));
                    return true;
                } else

                if (item.getItemId() == R.id.edit) {
                    observableGridMode.setGridMode(GridMode.SELECTING);
                    return true;
                } else if (item.getItemId() == R.id.select_all) {
                    observableGridMode.setGridMode(GridMode.SELECTING);
                    observableGridMode.fireSelectionChangeForAll(true);
                    return true;
                } else return false;
            }
        });
        loader.execute(PictureLoadMode.ALL.toString());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "Resuming");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "Paused");
    }


    private final View.OnClickListener TrashPicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (observableGridMode.getNumberOfSelected() == 0) return;
            //= Loading dialog (without cancellation)
            MaterialAlertDialogBuilder confirmDialog = new MaterialAlertDialogBuilder(context)
                    .setTitle("Move " + observableGridMode.getNumberOfSelected() + " to Trash?")
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

    private AlertDialog createDialog(String titleText, boolean isCancel, View.OnClickListener cancelCallback) {
        //=Prepare dialog
        View dialogView = getLayoutInflater().inflate(R.layout.component_loading_dialog, null);
        TextView title = dialogView.findViewById(R.id.component_loading_dialog_title);
        title.setText(titleText);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setView(dialogView);

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


    private void ProcessTrashPicture() {
        AlertDialog loadingDialog = createDialog("Moving images to Trash", false, null);
        //== Prepare data and handler
        List<ObservableGridMode<GalleryItem>.DataItem> temp = observableGridMode.getSelectedDataItem();
        Long[] deleteIds = new Long[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            deleteIds[i] = ((Picture) temp.get(i).data.getData()).getPictureId();
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

        GarbagePictureCollector.TrashPictureHandler collector = new GarbagePictureCollector.TrashPictureHandler(getContext()) {
            @Override
            public void preExecute(Long... longs) {
                Log.i("TrashImage", "Prepare trash");
                loadingDialog.show();
            }

            @Override
            public void onProcessUpdate(Integer... integers) {
                if (integers == null) return;
                Log.i("TrashImage", "Trashed " + integers[0] + " / " + temp.size());
                int index = observableGridMode.getObservedObjects().indexOf(temp.get(integers[0] - 1));
                observableGridMode.getObservedObjects().remove(temp.get(integers[0] - 1));
                adapter.notifyItemRemoved(index);
                ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
                assert progressBar != null;
                progressBar.setProgress((integers[0] * 100) / temp.size());

            }

            @Override
            public void postExecute(Void res) {
                Log.i("TrashImage", "Completely trashed");
                observableGridMode.setGridMode(GridMode.NORMAL);
                loadingDialog.dismiss();
            }
        };
        if (canExecute[0]) collector.execute(deleteIds);
        else Toast.makeText(context, "Don't have permission", Toast.LENGTH_LONG);
    }

    private void SetFunctionForActionBar() {
        Button btnDelete = actionBar.findViewById(R.id.btnDelete);
        Button btnShare = actionBar.findViewById(R.id.btnShare);
        Button btnAddTag = actionBar.findViewById(R.id.btnAddTag);
        Button btnMore = actionBar.findViewById(R.id.btnMore);
        btnDelete.setOnClickListener(this.TrashPicture);
        btnShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        btnAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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


    @Override
    public void sendToFragment(String... data) {

    }

    @Override
    public void onModeChange(GridModeEvent event) {
        mainActivity.sendToMain(TAG, event.getGridMode().toString());
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
        long quantity = observableGridMode.getNumberOfSelected();
        selectingTopBar.setTitle(quantity == 0 ? "Select items" : quantity + "selected");
    }

    @Override
    public void onSelectingAll(GridModeEvent event) {
        if (event.getNewSelectionForAll()) {
            selectingTopBar.setTitle(dataList.size() + " selected");
        } else {
            selectingTopBar.setTitle(String.valueOf(0) + " selected");
        }
    }


}

