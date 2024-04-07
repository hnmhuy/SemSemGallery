package com.example.semsemgallery.domain;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class PhotoActionsHandler {
    private static volatile PhotoActionsHandler instance;
    private Context context;

    private PhotoActionsHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    public static PhotoActionsHandler getInstance(Context context) {
        if (instance == null) {
            synchronized (PhotoActionsHandler.class) {
                if (instance == null) {
                    instance = new PhotoActionsHandler(context);
                }
            }
        }
        return instance;
    }

    public void copyToClipboard(Context context, String imagePath) {

    }

    public void copyToAlbum() {
        // Implement copy to album functionality here
        showToast("Copied to album");
    }

    public void moveToAlbum() {
        // Implement move to album functionality here
        showToast("Moved to album");
    }

    public void setAsHomeScreenAndLockScreen(String imagePath) throws IOException {
        // Implement set as wallpaper functionality here
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK | WallpaperManager.FLAG_SYSTEM);
    }

    public void setAsLockScreen(String imagePath) throws IOException {
        // Implement set as wallpaper functionality here
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
    }

    public void setAsHomeScreen(String imagePath) throws IOException {
        // Implement set as wallpaper functionality here
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
    }

    public void print() {
        // Implement print functionality here
        showToast("Printing");
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


}
