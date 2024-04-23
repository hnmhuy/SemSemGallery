package com.example.semsemgallery.activities.main2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.interfaces.MainCallBack;
import com.example.semsemgallery.activities.main2.fragment.AlbumsFragment;
import com.example.semsemgallery.activities.main2.fragment.FavoritesFragment;
import com.example.semsemgallery.activities.main2.fragment.MoreOptionsBottomSheet;
import com.example.semsemgallery.activities.main2.fragment.PicturesFragment;
import com.example.semsemgallery.domain.PermissionHandler;
import com.example.semsemgallery.domain.TagUtils;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements MainCallBack {

    private LinearLayout navbar;
    private LinearLayout highlighting;
    private View line1;
    private View line2;
    private View line3;
    private Class currentFragment;
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permission -> {
        boolean allGranted = true;
        for (Boolean isGranted : permission.values()) {
            if (!isGranted) {
                allGranted = false;
                break;
            }
        }
        boolean isStorageManager = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            isStorageManager = Environment.isExternalStorageManager();
        }

        if (!isStorageManager) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        PermissionHandler permissionHandler = new PermissionHandler(this, requestPermissionLauncher);
        permissionHandler.checkAndRequestPermissions();
        navbar = findViewById(R.id.navigation_bar);
        highlighting = findViewById(R.id.highlighting);
        setUpNavigationBar();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.main_content, PicturesFragment.class, null)
                    .commit();

            currentFragment = PicturesFragment.class;
        } else {
            String fragmentName = savedInstanceState.getString("currentFragment");
            line1.setVisibility(View.INVISIBLE);
            line2.setVisibility(View.INVISIBLE);
            line3.setVisibility(View.INVISIBLE);
            if (fragmentName.equals(PicturesFragment.class.getName())) {
                line1.setVisibility(View.VISIBLE);
            } else if (fragmentName.equals(AlbumsFragment.class.getName())) {
                line2.setVisibility(View.VISIBLE);
            } else if (fragmentName.equals((FavoritesFragment.class.getName()))) {
                line3.setVisibility(View.VISIBLE);
            } else {
                line1.setVisibility(View.VISIBLE);
            }
        }


        SharedPreferences sharedPreferences = getSharedPreferences("theme_preferences", Context.MODE_PRIVATE);
        boolean isDarkModeEnabled = sharedPreferences.getBoolean("isDarkModeEnabled", false);
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void selectNavButton(Button button) {
        line1.setVisibility(View.INVISIBLE);
        line2.setVisibility(View.INVISIBLE);
        line3.setVisibility(View.INVISIBLE);

        if (button.getId() == R.id.btnPictures) {
            line1.setVisibility(View.VISIBLE);
            NavigateTo(PicturesFragment.class);
        } else if (button.getId() == R.id.btnAlbums) {
            line2.setVisibility(View.VISIBLE);
            NavigateTo(AlbumsFragment.class);
        } else if (button.getId() == R.id.btnFavorite) {
            line3.setVisibility(View.VISIBLE);
            NavigateTo(FavoritesFragment.class);
        }

    }

    private void setUpNavigationBar() {
        Button btnPictures = navbar.findViewById(R.id.btnPictures);
        Button btnAlbums = navbar.findViewById(R.id.btnAlbums);
        Button btnFavorite = navbar.findViewById(R.id.btnFavorite);
        ImageButton btnMore = navbar.findViewById(R.id.btnMore);

        line1 = highlighting.findViewById(R.id.line1);
        line2 = highlighting.findViewById(R.id.line2);
        line3 = highlighting.findViewById(R.id.line3);

        btnPictures.setOnClickListener((v) -> selectNavButton(btnPictures));
        btnAlbums.setOnClickListener((v) -> selectNavButton(btnAlbums));
        btnFavorite.setOnClickListener((v) -> selectNavButton(btnFavorite));
        btnMore.setOnClickListener((v) -> {
            MoreOptionsBottomSheet botSheetFrag = new MoreOptionsBottomSheet();
            botSheetFrag.show(getSupportFragmentManager(), botSheetFrag.getTag());
        });
    }

    private void NavigateTo(Class fragment) {
        if (fragment == currentFragment) return;
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.main_content, fragment, null, "f" + fragment.getName())
                .commit();

        currentFragment = fragment;
    }

    @Override
    public void sendMsgToMain(String terminal, String... data) {
        Log.d("MainActivity", "Got message from fragment: " + data[0]);
        if (Objects.equals(data[0], GridMode.SELECTING.toString())) {
            navbar.setVisibility(View.GONE);
            highlighting.setVisibility(View.GONE);
        } else if (Objects.equals(data[0], GridMode.NORMAL.toString())) {
            navbar.setVisibility(View.VISIBLE);
            highlighting.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (currentFragment != null) {
            savedInstanceState.putString("currentFragment", currentFragment.getName());
        }
    }
}