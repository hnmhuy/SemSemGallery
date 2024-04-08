package com.example.semsemgallery.domain.Album;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import java.io.File;
import java.util.ArrayList;

public class AlbumHandler {

    public static boolean checkAlbumExists(Context context, String albumName) {
        // Get DCIM Folder in device
        File dcimDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        // Get Album with name as albumName
        File newAlbumDirectory = new File(dcimDirectory, albumName);

        // Check exists
        if (newAlbumDirectory.exists()) {
            Toast.makeText(context, "Album already exists", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


}
