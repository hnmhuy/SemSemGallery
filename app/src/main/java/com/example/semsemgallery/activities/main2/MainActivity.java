package com.example.semsemgallery.activities.main2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.interfaces.MainCallBack;
import com.example.semsemgallery.activities.main2.fragment.AlbumsFragment;
import com.example.semsemgallery.activities.main2.fragment.FavoritesFragment;
import com.example.semsemgallery.activities.main2.fragment.MoreOptionsBottomSheet;
import com.example.semsemgallery.activities.main2.fragment.PicturesFragment;
import com.example.semsemgallery.domain.PermissionHandler;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements MainCallBack {

    private NavigationBarView navbar;
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

        if (isStorageManager) {
            // Your app already has storage management permissions
            // You can proceed with file operations
        } else {
            // Your app does not have storage management permissions
            // Guide the user to the system settings page to grant permission
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
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.main_content, PicturesFragment.class, null)
                    .commit();
        }
        navbar = findViewById(R.id.navigation_bar);
        navbar.setOnItemSelectedListener(
                item -> {
                    int id = item.getItemId();
                    if (id == R.id.pictures_page) {
                        NavigateTo(PicturesFragment.class);
                        return true;
                    } else if (id == R.id.albums_page) {
                        NavigateTo(AlbumsFragment.class);
                        return true;
                    } else if (id == R.id.favorite_page) {
                        NavigateTo(FavoritesFragment.class);
                        return true;
                    } else if (id == R.id.more_option) {
                        MoreOptionsBottomSheet botSheetFrag = new MoreOptionsBottomSheet();
                        botSheetFrag.show(getSupportFragmentManager(), botSheetFrag.getTag());
                        return false;
                    } else return false;
                }
        );
    }

    private void NavigateTo(Class fragment) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.main_content, fragment, null)
                .commit();
    }

    @Override
    public void sendToMain(String terminal, String... data) {
        Log.d("MainActivity", "Got message from fragment: " + data[0]);
        if (Objects.equals(data[0], "true")) {
            navbar.setVisibility(View.GONE);
        } else if (Objects.equals(data[0], "false")) {
            navbar.setVisibility(View.VISIBLE);
        }
    }
}