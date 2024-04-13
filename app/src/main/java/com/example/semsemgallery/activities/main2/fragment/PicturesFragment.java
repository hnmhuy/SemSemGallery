package com.example.semsemgallery.activities.main2.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.GridModeEvent;
import com.example.semsemgallery.activities.base.GridModeListener;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.interfaces.FragmentCallBack;
import com.example.semsemgallery.activities.main2.MainActivity;
import com.example.semsemgallery.activities.main2.adapter.GalleryAdapter;
import com.example.semsemgallery.activities.main2.viewholder.DateHeaderItem;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItem;
import com.example.semsemgallery.domain.Picture.GarbagePictureCollector;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Picture;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class PicturesFragment extends Fragment implements FragmentCallBack, GridModeListener {
    public static String TAG = "PicturesFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri finalUri;
    private Context context;
    private MainActivity mainActivity;
    private LinearLayout actionBar;
    private MaterialToolbar selectingTopBar;
    private MaterialToolbar topBar;

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
    private final TreeSet<Long> header = new TreeSet<>(Comparator.reverseOrder());
    private List<GalleryItem> dataList = null;
    private final ObservableGridMode<GalleryItem> observableGridMode = new ObservableGridMode<>(null, GridMode.NORMAL);
    ;
    private GalleryAdapter adapter = null;
    private PictureLoader loader;
    private RecyclerView recyclerView;

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
                for (GalleryItem i : dataList) {
                    observableGridMode.addData(i);
                }
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
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
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "Resuming");

        loader.execute(PictureLoadMode.ALL.toString());
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

    private void ProcessTrashPicture() {
        View dialogView = getLayoutInflater().inflate(R.layout.component_loading_dialog, null);
        TextView title = dialogView.findViewById(R.id.component_loading_dialog_title);
        title.setText("Trashing selected pictures");
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setView(dialogView);

        AlertDialog loadingDialog = dialogBuilder.create();
        loadingDialog.setCanceledOnTouchOutside(false);

        Button cancelButton = dialogView.findViewById(R.id.component_loading_dialog_cancelButton);
        cancelButton.setVisibility(View.INVISIBLE);

        //== Prepare data and handler
        List<ObservableGridMode<GalleryItem>.DataItem> temp = observableGridMode.getSelectedDataItem();
        Long[] deleteIds = new Long[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            deleteIds[i] = ((Picture) temp.get(i).data.getData()).getPictureId();
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
        collector.execute(deleteIds);
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
        if (event.getGridMode() == GridMode.NORMAL) {
            mainActivity.sendToMain(TAG, "false");
            actionBar.setVisibility(View.GONE);
            topBar.setVisibility(View.VISIBLE);
            selectingTopBar.setVisibility(View.INVISIBLE);
        } else if (event.getGridMode() == GridMode.SELECTING) {
            mainActivity.sendToMain(TAG, "true");
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

    }


}

