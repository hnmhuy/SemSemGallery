package com.example.semsemgallery.activities.main2.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
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
import com.example.semsemgallery.activities.pictureview.ChooseAlbumActivity;
import com.example.semsemgallery.activities.pictureview.fragment.AddTagBottomSheet;
import com.example.semsemgallery.activities.search.SearchViewActivity;
import com.example.semsemgallery.domain.Album.AlbumHandler;
import com.example.semsemgallery.domain.Picture.GarbagePictureCollector;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Picture;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.Callable;


public class PicturesFragment extends Fragment implements FragmentCallBack, GridModeListener {
    public static String TAG = "PicturesFragment";
    FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri finalUri;
    private boolean isSelectingAll = false;
    private Context context;
    private MainActivity mainActivity;
    private LinearLayout actionBar;
    private MaterialToolbar selectingTopBar;
    private MaterialToolbar topBar;
    private final TreeSet<GalleryItem> galleryItems = new TreeSet<>(Comparator.reverseOrder());
    private final TreeSet<Long> header = new TreeSet<>(Comparator.reverseOrder());
    private List<GalleryItem> dataList = null;
    private final ObservableGridMode<GalleryItem> observableGridMode = new ObservableGridMode<>(null, GridMode.NORMAL);
    private GalleryAdapter adapter = null;
    private PictureLoader loader;
    private RecyclerView recyclerView;
    private String choiceHandler = "";
    private final ArrayList<Uri> selectedImages = new ArrayList<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private ProgressBar progressBar;
    private final ActivityResultLauncher<Intent> activityCameraResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loader.execute(PictureLoadMode.ALL.toString());
                } else {
                    // Image capture failed or was canceled
                    PicturesFragment.this.context.getContentResolver().delete(finalUri, null, null);
                    Toast.makeText(getActivity(), "Image capture failed", Toast.LENGTH_SHORT).show();
                }
            });
    private final OnBackPressedCallback backHandler = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Log.d("BackPressed", observableGridMode.getCurrentMode().toString());
            if (observableGridMode.getCurrentMode() == GridMode.SELECTING) {
                observableGridMode.fireSelectionChangeForAll(false);
                observableGridMode.setGridMode(GridMode.NORMAL);
                isSelectingAll = false;
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
        loader = new PictureLoader(context) {
            @Override
            public void preExecute(String... strings) {
                super.preExecute(strings);
                observableGridMode.reset();
                galleryItems.clear();
                adapter.notifyDataSetChanged();
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
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
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

    @Override
    public void onResume() {
        super.onResume();
        if (auth.getCurrentUser() == null) {
            Menu menu = topBar.getMenu();
            menu.removeItem(R.id.cloud);
        }
//        recyclerView.setVisibility(View.INVISIBLE);
//        progressBar.setVisibility(View.VISIBLE);
//        loader.execute(PictureLoadMode.ALL.toString());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        adapter = new GalleryAdapter(context, observableGridMode, null);
        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        recyclerView = view.findViewById(R.id.picture_recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL); // Adjust the span count as needed
        recyclerView.setLayoutManager(layoutManager);
        actionBar = view.findViewById(R.id.action_bar);
        SetFunctionForActionBar();
        topBar = view.findViewById(R.id.topAppBar);
        selectingTopBar = view.findViewById(R.id.selecting_top_bar);
        progressBar = view.findViewById(R.id.progressBar);
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

        topBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.search) {
                    startActivity(new Intent(getActivity().getApplicationContext(), SearchViewActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.cloud) {
                    startActivity(new Intent(getActivity().getApplicationContext(), CloudActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.edit) {
                    observableGridMode.setGridMode(GridMode.SELECTING);
                    return true;
                } else if (item.getItemId() == R.id.select_all) {
                    observableGridMode.setGridMode(GridMode.SELECTING);
                    observableGridMode.fireSelectionChangeForAll(true);
                    isSelectingAll = false;
                    return true;
                } else return false;
            }
        });

        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        loader.execute(PictureLoadMode.ALL.toString());

        return view;
    }

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

    private final View.OnClickListener trashPicture = new View.OnClickListener() {
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
                isSelectingAll = false;
                loadingDialog.dismiss();
            }
        };
        if (canExecute[0]) collector.execute(deleteIds);
        else Toast.makeText(context, "Don't have permission", Toast.LENGTH_LONG);
    }



    private AlertDialog myLoadingDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.component_loading_dialog, null);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext()).setView(dialogView);

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

                            for (GalleryItem item : observableGridMode.getSelectedItems()) {
                                Picture pictureItem = ((Picture)item.getData());
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
                                            observableGridMode.fireSelectionChangeForAll(false);
                                            observableGridMode.setGridMode(GridMode.NORMAL);
                                            isSelectingAll = false;
                                            loader.execute(PictureLoadMode.ALL.toString());
                                        });
                                    }

                                    @Override
                                    public void onLoadingProgressUpdate(int progress) {
                                        ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
                                        mHandler.post(() -> {
                                            progressBar.setProgress(progress);
                                        });
                                    }
                                };

                                AlbumHandler.copyImagesToAlbumHandler(context, selectedImages, albumName, loadingListener);
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
                                            observableGridMode.fireSelectionChangeForAll(false);
                                            observableGridMode.setGridMode(GridMode.NORMAL);
                                            isSelectingAll = false;
                                            loader.execute(PictureLoadMode.ALL.toString());
                                        });
                                    }

                                    @Override
                                    public void onLoadingProgressUpdate(int progress) {
                                        ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
                                        mHandler.post(() -> {
                                            progressBar.setProgress(progress);
                                        });
                                    }
                                };

                                AlbumHandler.moveImagesToAlbumHandler(context, selectedImages, albumName, loadingListener);
                            }

                        }
                    }
                }
            }
    );

    private void renderMoreMenu(View v, int res) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.inflate(res);
        MenuItem btnSelectAll = popupMenu.getMenu().findItem(R.id.btnSelectAll);
        if (isSelectingAll) btnSelectAll.setTitle(getString(R.string.unselect_all));
        else btnSelectAll.setTitle(R.string.select_all);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.btnMoveToAlbum) {
                if (observableGridMode.getNumberOfSelected() < 1) {
                    Toast.makeText(context, "No images selected", Toast.LENGTH_SHORT).show();
                    return false;
                }
                choiceHandler = "move";
                Intent chooseAlbumIntent = new Intent(context, ChooseAlbumActivity.class);
                activityResultLauncher.launch(chooseAlbumIntent);
                return true;
            } else if (id == R.id.btnCopyToAlbum) {
                if (observableGridMode.getNumberOfSelected() < 1) {
                    Toast.makeText(context, "No images selected", Toast.LENGTH_SHORT).show();
                    return false;
                }
                choiceHandler = "copy";
                Intent chooseAlbumIntent = new Intent(context, ChooseAlbumActivity.class);
                activityResultLauncher.launch(chooseAlbumIntent);
                return true;
            } else if (id == R.id.btnSelectAll) {
                isSelectingAll = !isSelectingAll;
                observableGridMode.fireSelectionChangeForAll(isSelectingAll);
                return true;
            } else return false;
        });
        popupMenu.show();
    }


    private void SetFunctionForActionBar() {
        Button btnDelete = actionBar.findViewById(R.id.btnDelete);
        Button btnShare = actionBar.findViewById(R.id.btnShare);
        Button btnAddTag = actionBar.findViewById(R.id.btnAddTag);
        Button btnMore = actionBar.findViewById(R.id.btnMore);
        btnDelete.setOnClickListener(this.trashPicture);
        btnShare.setOnClickListener(v -> {
            if (observableGridMode.getNumberOfSelected() == 0) return;

            //== Retrieve the data for sharing
            List<GalleryItem> pictures = observableGridMode.getSelectedItems();
            ArrayList<Uri> shareFiles = new ArrayList<>();
            for (GalleryItem item : pictures) {
                Object data = item.getData();
                if (data instanceof Picture) {
                    File shareFile = new File(((Picture) data).getPath());
                    Uri shareUri = FileProvider.getUriForFile(
                            context.getApplicationContext(),
                            context.getApplicationContext().getPackageName() + ".provider",
                            shareFile
                    );
                    shareFiles.add(shareUri);
                }
            }

            //==Start the new activity
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareFiles);
            startActivity(Intent.createChooser(shareIntent, "Share images via..."));

        });

        btnAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ObservableGridMode<GalleryItem>.DataItem> temp = observableGridMode.getSelectedDataItem();
                ArrayList<Long> pictureIds = new ArrayList<>();
                for (int i = 0; i < temp.size(); i++) {
                    pictureIds.add((((Picture) temp.get(i).data.getData()).getPictureId()));
                }
                AddTagBottomSheet addTagBottomSheet = new AddTagBottomSheet(new ArrayList<>() ,pictureIds);
                addTagBottomSheet.setOnTagAddedListener(tags -> {
                    observableGridMode.setGridMode(GridMode.NORMAL);
                    observableGridMode.fireSelectionChangeForAll(false);
                });
                addTagBottomSheet.show(requireActivity().getSupportFragmentManager(), addTagBottomSheet.getTag());
            }
        });
        btnMore.setOnClickListener((v) -> {
            renderMoreMenu(v, R.menu.pictures_fragment_selecting_mode);
        });
    }

    @Override
    public void sendToFragment(String... data) {

    }

    @Override
    public void onModeChange(GridModeEvent event) {
        mainActivity.sendMsgToMain(TAG, event.getGridMode().toString());
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
        selectingTopBar.setTitle(quantity == 0 ? "Select items" : quantity + " selected");
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

