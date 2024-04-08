package com.example.semsemgallery.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.example.semsemgallery.activities.album.AlbumViewActivity;
import com.example.semsemgallery.activities.main.adapter.AlbumRecyclerAdapter;
import com.example.semsemgallery.domain.Album.AlbumHandler;
import com.example.semsemgallery.domain.MediaRetriever;
import com.example.semsemgallery.models.Album;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class AlbumsFragment extends Fragment implements AlbumRecyclerAdapter.OnAlbumItemClickListener {

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Toast.makeText(requireContext(), "Go here", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private com.google.android.material.appbar.MaterialToolbar topBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ====== Render View
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        List<Album> albumsRetriever = new MediaRetriever(appCompatActivity).getAlbumList();
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.album_recycler);
        AlbumRecyclerAdapter adapter = new AlbumRecyclerAdapter(albumsRetriever, appCompatActivity);

        adapter.setOnAlbumItemClickListener(this); // Event Click

        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        // ====== Get TopBar
        topBar = view.findViewById(R.id.fragment_albums_topAppBar);

        return view;
    }

    @Override
    public void onAlbumItemClick(String albumId, String albumName) {
        // Move to AlbumViewActivity & provide albumId
        Intent intent = new Intent(requireContext(), AlbumViewActivity.class);
        intent.putExtra("albumId", albumId);
        intent.putExtra("albumName", albumName);
        startActivity(intent);
    }

    // ====== Set Listener for Icon in Top Bar right here
    @Override
    public void onResume() {
        super.onResume();

        // ====== Listener for AddIcon in TopBar clicked
        topBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.add) {
                showOptionDialog();
                return true;
            }
            return false;
        });
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
        // Inflate the custom input dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.component_input_dialog, null);

        // Create a MaterialAlertDialogBuilder with the dialog view
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView);

        // Show the dialog
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
            String albumName = inputName.getText().toString();

            // If not exists -> Open Photo Picker
            if (!AlbumHandler.checkAlbumExists(requireContext(), albumName)) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                activityResultLauncher.launch(photoPickerIntent);
            }

            dialog.dismiss();
        });
    }
}
