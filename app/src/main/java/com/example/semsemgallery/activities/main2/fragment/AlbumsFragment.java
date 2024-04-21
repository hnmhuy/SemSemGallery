package com.example.semsemgallery.activities.main2.fragment;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.GridModeEvent;
import com.example.semsemgallery.activities.base.GridModeListener;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.MainActivity;
import com.example.semsemgallery.activities.main2.adapter.AlbumRecyclerAdapter;
import com.example.semsemgallery.activities.search.SearchViewActivity;
import com.example.semsemgallery.domain.Album.AlbumHandler;
import com.example.semsemgallery.domain.Album.AlbumLoader;
import com.example.semsemgallery.domain.MediaRetriever;
import com.example.semsemgallery.domain.Picture.GarbagePictureCollector;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Album;
import com.example.semsemgallery.models.Picture;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class AlbumsFragment extends Fragment implements GridModeListener {

    private MainActivity mainActivity;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isSelectingAll;
    private final ArrayList<Uri> selectedImages = new ArrayList<>();
    private String newAlbumName;
    private AlbumLoader loader = null;
    private final ArrayList<Album> albumArrayList = new ArrayList<>();
    private final ObservableGridMode<Album> observedObj = new ObservableGridMode<>(null, GridMode.NORMAL);
    private AlbumRecyclerAdapter adapter = null;
    private MaterialToolbar topBar;
    private MaterialToolbar selectingTopBar;
    private LinearLayout bottomAction;
    private Context context;
    private ProgressBar progressBar;

    // ====== Activity Result Launcher for Photo Picker
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<>() {
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
                        if (!selectedImages.isEmpty()) {
                            showAlbumHandlerDialog();
                        }
                        else {
                            Toast.makeText(context, "No images selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        observedObj.setMaster(this);
        observedObj.addObserver(this);
        adapter = new AlbumRecyclerAdapter(context, observedObj);
        this.context = context;
        loader = new AlbumLoader(context) {
            @Override
            public void onProcessUpdate(Album... albums) {
                albumArrayList.add(albums[0]);
                observedObj.addData(albums[0]);
                Log.d("AlbumLoader", albums[0].getAlbumId() + " - " + albums[0].getName() + " - " + albums[0].getWallPath());
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void postExecute(Void res) {
                super.postExecute(res);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void preExecute(String... strings) {
                super.preExecute(strings);
                observedObj.getObservedObjects().clear();
                albumArrayList.clear();
            }
        };
    }

    private OnBackPressedCallback backHandler = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (observedObj.getCurrentMode() == GridMode.SELECTING) {
                observedObj.fireSelectionChangeForAll(false);
                observedObj.setGridMode(GridMode.NORMAL);
                isSelectingAll = false;
            } else {
                // If not in selecting mode, finish the activity
                mainActivity.finish();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mainActivity = (MainActivity) getActivity();
        mainActivity.getOnBackPressedDispatcher().addCallback(backHandler);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // ====== Render View
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.album_recycler);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        progressBar = view.findViewById(R.id.progressBar);
        // ====== Get TopBar
        topBar = view.findViewById(R.id.fragment_albums_topAppBar);
        selectingTopBar = view.findViewById(R.id.fragment_albums_topAppBarSelecting);
        bottomAction = view.findViewById(R.id.bottomActions);
        SetActionBottomFunctions();
        SetActionTopFunctions();
        return view;
    }

    // ====== Set Listener for Icon in Top Bar right here
    @Override
    public void onResume() {
        super.onResume();
        if (auth.getCurrentUser() == null) {
            Menu menu = topBar.getMenu();
            menu.removeItem(R.id.cloud);
        }
        loader.execute();
    }


    // ====== Show Input Dialog
    private void showInputDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.component_input_dialog, null);

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        TextView cancelBtn = dialogView.findViewById(R.id.component_input_dialog_cancel);
        TextView createBtn = dialogView.findViewById(R.id.component_input_dialog_create);
        EditText inputName = dialogView.findViewById(R.id.component_input_dialog_name);

        // ====== Listener for CancelButton in InputDialog clicked
        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // ====== Listener for CreateButton in InputDialog clicked
        createBtn.setOnClickListener(v -> {
            newAlbumName = inputName.getText().toString();
            dialog.dismiss();

            // If album does not exist -> Open Photo Picker
            if (!AlbumHandler.checkAlbumExists(requireContext(), newAlbumName)) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                activityResultLauncher.launch(photoPickerIntent);
            }
        });
    }

    // ====== Show Album Handler Dialog
    private void showAlbumHandlerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.component_album_handler_dialog, null);

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setView(dialogView);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        TextView cancelBtn = dialogView.findViewById(R.id.component_album_handler_dialog_cancel);
        TextView copyBtn = dialogView.findViewById(R.id.component_album_handler_dialog_copy);
        TextView moveBtn = dialogView.findViewById(R.id.component_album_handler_dialog_move);

        // ====== Listener for CancelButton in AlbumHandlerDialog clicked
        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // ====== Listener for CopyButton in AlbumHandlerDialog clicked
        copyBtn.setOnClickListener(v -> {
            dialog.dismiss();

            AlertDialog loadingDialog = myLoadingDialog();
            loadingDialog.show(); // Show Loading Dialog
            TextView dialogTitle = loadingDialog.findViewById(R.id.component_loading_dialog_title);
            dialogTitle.setText("Copying to " + newAlbumName);

            // Create a listener for completion & set progress
            AlbumHandler.OnLoadingListener loadingListener = new AlbumHandler.OnLoadingListener() {
                @Override
                public void onLoadingComplete() {
                    Log.d("AlbumsFM", "Finish Copy");
                    loadingDialog.dismiss();
                    loader.execute();
                    mHandler.post(() -> {
                        if (AlbumHandler.duplicatedImages.size() != 0) {
                            showExceptionHandlerDialog();
                        }
                    });
                }

                @Override
                public void onLoadingProgressUpdate(int progress) {
                    Log.d("AlbumsFM", "Update Copy progres");
                    ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
                    mHandler.post(() -> {
                        progressBar.setProgress(progress);
                    });
                }
            };

            Log.d("AlbumsFM", "Begin Copy");
            AlbumHandler.copyImagesToAlbumHandler(context, selectedImages, newAlbumName, loadingListener);
        });

        // ====== Listener for MoveButton in AlbumHandlerDialog clicked
        moveBtn.setOnClickListener(v -> {
            dialog.dismiss();

            AlertDialog loadingDialog = myLoadingDialog();
            loadingDialog.show(); // Show Loading Dialog
            TextView dialogTitle = loadingDialog.findViewById(R.id.component_loading_dialog_title);
            dialogTitle.setText("Moving to " + newAlbumName);

            // Create a listener for completion & set progress
            AlbumHandler.OnLoadingListener loadingListener = new AlbumHandler.OnLoadingListener() {
                @Override
                public void onLoadingComplete() {
                    loadingDialog.dismiss();
                    loader.execute();
                }

                @Override
                public void onLoadingProgressUpdate(int progress) {
                    ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
                    mHandler.post(() -> {
                        progressBar.setProgress(progress);
                    });
                }
            };

            AlbumHandler.moveImagesToAlbumHandler(context, selectedImages, newAlbumName, loadingListener);
        });
    }

    private void showExceptionHandlerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.component_exception_handler_dialog, null);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView description = dialogView.findViewById(R.id.component_exception_handler_dialog_description);
        CheckBox applyCheckbox = dialogView.findViewById(R.id.component_exception_handler_dialog_checkbox);
        TextView skipBtn = dialogView.findViewById(R.id.component_exception_handler_dialog_skip);
        TextView replaceBtn = dialogView.findViewById(R.id.component_exception_handler_dialog_replace);

        applyCheckbox.setOnClickListener(v -> {
            if (applyCheckbox.isChecked()) {
                Log.d("AlbumsFM", "Checked");
            }
            else {
                Log.d("AlbumsFM", "UnChecked");
            }
        });

        // ====== Listener for CancelButton in Delete Confirm Dialog clicked
        skipBtn.setOnClickListener(v -> {
            // Log.d("AlbumsFM", "Skip");
            for (Uri imageUri : AlbumHandler.duplicatedImages) {
               Log.d("AlbumsFM", "Exist file : " + AlbumHandler.getFileName(context, imageUri));
            }
        });

        // ====== Listener for DeleteButton in Delete Confirm Dialog clicked
        replaceBtn.setOnClickListener(v -> {
            Log.d("AlbumsFM", "Replace");
        });


    }

    // ==== Just only able to init Dialog in Fragment/Activity
    private AlertDialog myLoadingDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.component_loading_dialog, null);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setView(dialogView);

        AlertDialog dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);

        Button cancelButton = dialogView.findViewById(R.id.component_loading_dialog_cancelButton);
        cancelButton.setOnClickListener(v -> {
            AlbumHandler.stopHandling();
        });

        return dialog;
    }

    @Override
    public void onModeChange(GridModeEvent event) {
        //== Send the signal to main activity
        mainActivity.sendMsgToMain("AlbumsFragment", event.getGridMode().toString());
        topBar.setVisibility(GridMode.SELECTING == event.getGridMode() ? View.INVISIBLE : View.VISIBLE);
        selectingTopBar.setVisibility(GridMode.SELECTING == event.getGridMode() ? View.VISIBLE : View.INVISIBLE);
        bottomAction.setVisibility(GridMode.SELECTING == event.getGridMode() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSelectionChange(GridModeEvent event) {
        long value = observedObj.getNumberOfSelected();
        selectingTopBar.setTitle(value == 0 ? getResources().getString(R.string.select_items) : String.valueOf(value) + " selected");
    }

    @Override
    public void onSelectingAll(GridModeEvent event) {
        selectingTopBar.setTitle(event.getNewSelectionForAll() ? String.valueOf(observedObj.getDataSize()) + " selected" : getString(R.string.select_items));
    }

    private void SetActionTopFunctions() {
        // ====== Listener for AddIcon in TopBar clicked
        topBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.add) {
                showInputDialog();
                return true;
            } else if (id == R.id.search) {
                startActivity(new Intent(context.getApplicationContext(), SearchViewActivity.class));
                return true;
            } else if (id == R.id.edit) {
                observedObj.setGridMode(GridMode.SELECTING);
                return true;
            } else if (id == R.id.select_all) {
                observedObj.setGridMode(GridMode.SELECTING);
                observedObj.fireSelectionChangeForAll(true);
                isSelectingAll = true;
                return true;
            } else return false;
        });
    }

    private void SetActionBottomFunctions() {
        Button btnDelete = bottomAction.findViewById(R.id.btnDelete);
        Button btnAll = bottomAction.findViewById(R.id.btnAll);

        btnAll.setOnClickListener((v) -> {
            observedObj.fireSelectionChangeForAll(!isSelectingAll);
            isSelectingAll = !isSelectingAll;
            btnAll.setText(isSelectingAll ? "Unselect All" : "Select All");
        });

        btnDelete.setOnClickListener((v) -> {
            if (observedObj.getNumberOfSelected() < 1) {
                Toast.makeText(context, "No albums selected", Toast.LENGTH_SHORT).show();
            } else {
                showDeleteConfirmDialog();
            }
        });
    }

    // ====== Show Delete Confirm Dialog
    private void showDeleteConfirmDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.component_confirm_dialog, null);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        TextView cancelBtn = dialogView.findViewById(R.id.component_confirm_dialog_cancel);
        TextView deleteBtn = dialogView.findViewById(R.id.component_confirm_dialog_confirm);
        TextView title = dialogView.findViewById(R.id.component_confirm_dialog_title);
        TextView description = dialogView.findViewById(R.id.component_confirm_dialog_description);
        long numberOfSelectedAlbums = observedObj.getNumberOfSelected();
        if (numberOfSelectedAlbums > 1) {
            title.setText("Move " + numberOfSelectedAlbums + " albums to Trash");
            description.setText("Confirm move selected albums to the Trash?");
        } else if (numberOfSelectedAlbums == 1) {
            title.setText("Move " + numberOfSelectedAlbums + " album to Trash");
            description.setText("Confirm move selected album to the Trash?");
        }

        // ====== Listener for CancelButton in Delete Confirm Dialog clicked
        cancelBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // ====== Listener for DeleteButton in Delete Confirm Dialog clicked
        deleteBtn.setOnClickListener(v -> {
            dialog.dismiss();

            // ======== Show Loading Dialog
            AlertDialog loadingDialog = myLoadingDialog();
            loadingDialog.show(); // Show Loading Dialog
            TextView dialogTitle = loadingDialog.findViewById(R.id.component_loading_dialog_title);
            dialogTitle.setText("Retrieving data");

            final ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
            final ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.IS_TRASHED, 1);

            List<Picture> deleteData = new ArrayList<>();
            List<Album> albumList = observedObj.getSelectedItems();

            PictureLoader pictureLoader = new PictureLoader(context) {
                @Override
                public void onProcessUpdate(Picture... pictures) {
                    deleteData.add(pictures[0]);
                }

                @Override
                public void postExecute(Boolean res) {
                    int current = progressBar.getProgress();
                    current += (int) (100 / albumList.size());
                    progressBar.setProgress(current);
                    if (current >= 100) {
                        // Loading complete
                        dialogTitle.setText("Trashing pictures....");
                        progressBar.setProgress(0);
                        new Thread(() -> {
                            for (Picture p : deleteData) {
                                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p.getPictureId());
                                if (GarbagePictureCollector.trashPicture(resolver, uri, values)) {
                                    mHandler.post(() -> {
                                        progressBar.setProgress(progressBar.getProgress() + (100 / deleteData.size()));
                                    });
                                }
                            }

                            mHandler.post(() -> {
                                loadingDialog.dismiss();
                                observedObj.setGridMode(GridMode.NORMAL);
                                loader.execute();
                            });

                        }).start();
                    }
                }
            };

            for (Album album : albumList) {
                pictureLoader.execute(PictureLoadMode.BY_ALBUM.toString(), album.getAlbumId());
            }

        });
    }

}
