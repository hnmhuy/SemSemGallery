package com.example.semsemgallery.domain;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionHandler {
    private final AppCompatActivity activity;
    private final ActivityResultLauncher<String[]> requestPermissionLauncher;

    public PermissionHandler(AppCompatActivity activity, ActivityResultLauncher<String[]> launcher) {
        this.activity = activity;
        this.requestPermissionLauncher = launcher;
    }

    public void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String[] permissions = new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
            };

            List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }

            if (!permissionsToRequest.isEmpty()) {
                String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
                boolean shouldShowRationale = false;

                for (String permission : permissionsArray) {
                    if (activity.shouldShowRequestPermissionRationale(permission)) {
                        shouldShowRationale = true;
                        break;
                    }
                }

                if (shouldShowRationale) {
                    showPermissionRationaleDialog(permissionsArray);
                } else {
                    requestPermissionLauncher.launch(permissionsArray);
                }
            }
        }
    }

    private void showPermissionRationaleDialog(final String[] permissions) {
        new AlertDialog.Builder(activity)
                .setMessage("Please allow all permissions")
                .setCancelable(false)
                .setPositiveButton("YES", (dialogInterface, i) -> requestPermissionLauncher.launch(permissions))
                .setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }
}
