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

    );

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i("AlbumFragment", "On attach");
        observedObj.setMaster(this);
        observedObj.addObserver(this);
        adapter = new AlbumRecyclerAdapter(context, observedObj);
        this.context = context;
        loader = new AlbumLoader(context) {
            @Override
            public void onProcessUpdate(Album... albums) {

            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void postExecute(Void res) {

            }

            @Override
            public void preExecute(String... strings) {

            }
        };
    }

    private OnBackPressedCallback backHandler = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Log.d("BackPressed", observedObj.getCurrentMode().toString());
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
        Log.i("AlbumFragment", "On create view");

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


}
