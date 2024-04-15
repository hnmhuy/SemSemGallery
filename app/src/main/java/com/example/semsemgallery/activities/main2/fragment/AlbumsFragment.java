package com.example.semsemgallery.activities.main2.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.adapter.AlbumRecyclerAdapter;
import com.example.semsemgallery.domain.Album.AlbumHandler;
import com.example.semsemgallery.domain.Album.AlbumLoader;
import com.example.semsemgallery.domain.MediaRetriever;
import com.example.semsemgallery.models.Album;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AlbumsFragment extends Fragment {

    private ArrayList<Uri> selectedImages;
    private String newAlbumName;
    private AlbumLoader loader = null;
    private final ArrayList<Album> albumArrayList = new ArrayList<>();
    private final ObservableGridMode<Album> observedObj = new ObservableGridMode<>(null, GridMode.NORMAL);
    private AlbumRecyclerAdapter adapter = null;
    private MaterialToolbar topBar;
    private Context applicationContext;

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
                            showAlbumHandlerDialog();
                        }
                        else {
                            Toast.makeText(applicationContext, "No images selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i("AlbumFragment", "On attach");

        if (adapter == null) {
            observedObj.getObservedObjects().clear();
            albumArrayList.clear();
            adapter = new AlbumRecyclerAdapter(context, observedObj);
        }
        loader = new AlbumLoader(context) {
            @Override
            public void onProcessUpdate(Album... albums) {
                albumArrayList.add(albums[0]);
                observedObj.addData(albums[0]);
                Log.d("AlbumLoader", albums[0].getAlbumId() + " - " + albums[0].getName() + " - " + albums[0].getWallPath());
                adapter.notifyItemInserted(albumArrayList.size() - 1);
            }

            @Override
            public void postExecute(Void res) {
                super.postExecute(res);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void preExecute(String... strings) {
                super.preExecute(strings);
                if (observedObj != null) {
                    observedObj.reset();
                }
                albumArrayList.clear();
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("AlbumFragment", "On create view");

        // ====== Render View
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        List<Album> albumsRetriever = new MediaRetriever(appCompatActivity).getAlbumList();
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.album_recycler);
        // adapter.setOnAlbumItemClickListener(this); // Event Click

        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        // loader.execute();

        // ====== Get TopBar
        topBar = view.findViewById(R.id.fragment_albums_topAppBar);
        selectedImages = new ArrayList<>();
        applicationContext = requireContext();

        return view;
    }

    // ====== Set Listener for Icon in Top Bar right here
    @Override
    public void onResume() {
        super.onResume();
        Log.i("AlbumFragment", "On resume");

        // ====== Listener for AddIcon in TopBar clicked
        topBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.add) {
                showOptionDialog();
                return true;
            }
            return false;
        });

        loader.execute();
    }

    // ====== Show Option Dialog
    private void showOptionDialog() {
        // Inflate the custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.component_option_dialog, null);

        // Create a MaterialAlertDialogBuilder with the dialog view
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView);

        // Show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // ====== Listener for AlbumOption in OptionDialog clicked
        LinearLayout albumOption = dialogView.findViewById(R.id.component_option_dialog_album);
        albumOption.setOnClickListener(v -> {
            dialog.dismiss();
            showInputDialog();
        });

        // ====== Listener for AutoUpdatingOption in OptionDialog clicked
        LinearLayout autoUpdatingOption = dialogView.findViewById(R.id.component_option_dialog_auto_updating);
        autoUpdatingOption.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Auto-Updating Album", Toast.LENGTH_SHORT).show();
        });
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

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(applicationContext).setView(dialogView);

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
            dialogTitle.setText("Copying items to " + newAlbumName);

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
                    progressBar.setProgress(progress);
                }
            };

            AlbumHandler.copyImagesToAlbumHandler(applicationContext, selectedImages, newAlbumName, loadingListener);
        });


        // ====== Listener for MoveButton in AlbumHandlerDialog clicked
        moveBtn.setOnClickListener(v -> {
            dialog.dismiss();

            AlertDialog loadingDialog = myLoadingDialog();
            loadingDialog.show(); // Show Loading Dialog
            TextView dialogTitle = loadingDialog.findViewById(R.id.component_loading_dialog_title);
            dialogTitle.setText("Moving items to " + newAlbumName);

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
                    progressBar.setProgress(progress);
                }
            };

            AlbumHandler.moveImagesToAlbumHandler(applicationContext, selectedImages, newAlbumName, loadingListener);
        });
    }


    // ==== Just only able to init Dialog in Fragment/Activity
    private AlertDialog myLoadingDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.component_loading_dialog, null);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(applicationContext).setView(dialogView);

        AlertDialog dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);

        Button cancelButton = dialogView.findViewById(R.id.component_loading_dialog_cancelButton);
        cancelButton.setOnClickListener(v -> {
            AlbumHandler.stopHandling();
        });

        return dialog;
    }

}
