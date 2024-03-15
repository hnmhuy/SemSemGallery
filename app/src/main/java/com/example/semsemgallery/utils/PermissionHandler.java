package com.example.semsemgallery.utils;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

            if (permissionsToRequest.isEmpty()) {
                retrieveImagePaths();
            } else {
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
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissionLauncher.launch(permissions);
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    public void retrieveImagePaths() {
        List<String> imagePathList = new ArrayList<>();
        // Query images using MediaStore
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));

                Date date = new Date(timestamp * 1000L);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String dateString = dateFormat.format(date);

                String fullPath = path + " " + dateString;
                imagePathList.add(fullPath);
            }
            cursor.close();
        }

        for (String path : imagePathList) {
            Log.d("Image Path: ", path);
        }
    }
}
