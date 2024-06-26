package com.example.semsemgallery.domain.Album;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class AlbumHandler {
    private static boolean isHandling = false;
    public static void stopHandling() {
        isHandling = false;
    }

    public static boolean isDuplicateHandling = false;
    public static Uri currentDuplicateImageUri = null;
    public static String duplicateHandleChoice = "";
    public static boolean isApplyToAll = false;
    

    public interface OnLoadingListener {
        void onLoadingComplete();
        void onLoadingProgressUpdate(int progress);
        void onLoadingException();
    }

    // ====== Check if Album Exists
    public static boolean checkAlbumExists(Context context, String albumName) {
        if (albumName == null || albumName.isEmpty()) {
            Toast.makeText(context, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
            return true;
        }

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

    // ====== Copy Images to Album (If Album doesn't exist, create new album)
    public static void copyImagesToAlbum(Context context, ArrayList<Uri> imageUris, String albumName) {
        // Get DCIM Folder in device
        File dcimDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        // Get Album with name as albumName
        File albumDirectory = new File(dcimDirectory, albumName);

        // Create the album directory if it doesn't exist
        if (!albumDirectory.exists()) {
            albumDirectory.mkdirs();
        }

        // Copy images to the album directory
        for (Uri imageUri : imageUris) {
            try {
                String fileName = getFileName(context, imageUri);
                File destFile = new File(albumDirectory, fileName);

                // Check if the file already exists in the destination directory
                if (destFile.exists()) {
                    Log.d("Skipped", "File " + fileName + " already exists in the destination directory");
                    continue;
                }

                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream != null) {
                    copyFile(inputStream, destFile);
                    Log.d("Copied", "File " + fileName);
                    inputStream.close(); // Close the InputStream
                } else {
                    Log.d("Error", "Failed to open InputStream for " + fileName);
                }
            } catch (IOException e) {
                Toast.makeText(context, "Failed to copy images", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    // Copy Images to Album - but split Thread for show Loading Dialog
    public static void copyImagesToAlbumHandler(Context context, ArrayList<Uri> imageUris, String albumName, OnLoadingListener listener) {
        Handler handler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            int totalImages = imageUris.size();
            isHandling = true;
            isDuplicateHandling = false;
            duplicateHandleChoice = "";
            isApplyToAll = false;

            // Get DCIM Folder in device & Album with name as albumName
            File dcimDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File albumDirectory = new File(dcimDirectory, albumName);

            // Create the album directory if it doesn't exist
            if (!albumDirectory.exists()) {
                albumDirectory.mkdirs();
            }

            // Copy images to the album directory
            for (int i = 0; i < totalImages; i++) {
                if (!isHandling) break;

                Uri imageUri = imageUris.get(i);
                try {
                    String fileName = getFileName(context, imageUri);
                    File destFile = new File(albumDirectory, fileName);

                    // If exist -> Waiting for choosing SKIP or REPLACE
                    if (destFile.exists()) {
                        if (!isApplyToAll) {
                            isDuplicateHandling = true;
                            currentDuplicateImageUri = imageUri;

                            listener.onLoadingException();

                            while (true) {
                                if (!isDuplicateHandling) break;
                            }

                        }
                        if (duplicateHandleChoice == "skip") { continue;}
                        else if (duplicateHandleChoice == "replace") { destFile.delete(); }
                    }

                    InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                    if (inputStream != null) {
                        copyFile(inputStream, destFile);

                        int progress = (int) (((i + 1) / (float) totalImages) * 100);
                        listener.onLoadingProgressUpdate(progress);

                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Scan the directory
            AlbumHandler.scanAlbumForMediaStoreRegister(context, albumDirectory.getAbsolutePath(), handler);
            listener.onLoadingComplete();
        }).start();
    }


    // ====== Move Images to Album (If Album doesn't exist, create new album)
    public static void moveImagesToAlbum(Context context, ArrayList<Uri> imageUris, String albumName) {
        // Get DCIM Folder in device
        File dcimDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        // Get Album with name as albumName
        File albumDirectory = new File(dcimDirectory, albumName);

        // Create the album directory if it doesn't exist
        if (!albumDirectory.exists()) {
            albumDirectory.mkdirs();
        }

        // Move images to the album directory
        for (Uri imageUri : imageUris) {
            try {
                String fileName = getFileName(context, imageUri);
                File destFile = new File(albumDirectory, fileName);

                // Check if the file already exists in the destination directory
                if (destFile.exists()) {
                    Log.d("Skipped", "File " + fileName + " already exists in the destination directory");
                    continue;
                }

                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream != null) {
                    copyFile(inputStream, destFile);
                    Log.d("Moved", "File " + fileName);
                    inputStream.close(); // Close the InputStream

                    // Delete the original file
                    File originalFile = new File(imageUri.getPath());
                    String fileId = originalFile.getName();
                    imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.valueOf(fileId));
                    if (deleteFileByUri(context, imageUri)) {
                        Log.d("DeleteImage", "File " + fileName + " deleted successfully");
                    } else {
                        Log.d("DeleteImage", "Failed to delete file " + fileName);
                    }
                } else {
                    Log.d("Error", "Failed to open InputStream for " + fileName);
                }
            } catch (IOException e) {
                Toast.makeText(context, "Failed to move images", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }


        Toast.makeText(context, "Move successfully to " + albumName, Toast.LENGTH_SHORT).show();
    }

    // Move Images to Album - but split Thread for show Loading Dialog
    public static void moveImagesToAlbumHandler(Context context, ArrayList<Uri> imageUris, String albumName, OnLoadingListener listener) {
        Handler handler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            int totalImages = imageUris.size();
            isHandling = true;
            isDuplicateHandling = false;
            duplicateHandleChoice = "";
            isApplyToAll = false;

            // Get DCIM Folder in device & Album with name as albumName
            File dcimDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File albumDirectory = new File(dcimDirectory, albumName);

            // Create the album directory if it doesn't exist
            if (!albumDirectory.exists()) {
                albumDirectory.mkdirs();
            }

            // Copy images to the album directory
            for (int i = 0; i < totalImages; i++) {
                if (!isHandling) break;

                Uri imageUri = imageUris.get(i);
                try {
                    String fileName = getFileName(context, imageUri);
                    File destFile = new File(albumDirectory, fileName);

                    // If exist -> Waiting for choosing SKIP or REPLACE
                    if (destFile.exists()) {
                        if (!isApplyToAll) {
                            isDuplicateHandling = true;
                            currentDuplicateImageUri = imageUri;

                            listener.onLoadingException();

                            while (true) {
                                if (!isDuplicateHandling) break;
                            }

                        }
                        if (duplicateHandleChoice == "skip") { continue;}
                        else if (duplicateHandleChoice == "replace") { destFile.delete(); }
                    }

                    InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                    if (inputStream != null) {
                        copyFile(inputStream, destFile);

                        int progress = (int) (((i + 1) / (float) totalImages) * 100);
                        listener.onLoadingProgressUpdate(progress);

                        inputStream.close();

                        // Delete the original file
                        File originalFile = new File(imageUri.getPath());
                        String fileId = originalFile.getName();
                        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.valueOf(fileId));
                        if (deleteFileByUri(context, imageUri)) {
                            Log.d("DeleteImage", "File " + fileName + " deleted successfully");
                        } else {
                            Log.d("DeleteImage", "Failed to delete file " + fileName);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            AlbumHandler.scanAlbumForMediaStoreRegister(context, albumDirectory.getAbsolutePath(), handler);
            listener.onLoadingComplete();
        }).start();
    }

    public static boolean deleteFileByUri(Context context, Uri uri) {
        // Get the file path from the URI
        String filePath = getFilePathFromUri(context, uri);
        if (filePath == null) {
            return false; // Unable to retrieve file path
        }

        // Create a File object representing the file
        File file = new File(filePath);

        // Check if the file exists
        if (file.exists()) {
            return file.delete();
        } else {
            return false;
        }
    }

    private static String getFilePathFromUri(Context context, Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        ContentResolver contentResolver = context.getContentResolver();
        try {
            // Query the media store to get the file path
            android.database.Cursor cursor = contentResolver.query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }


    // ====== Rename album (Remembers to check if the album exists before renaming)
    public static void renameAlbum(Context context, String oldAlbumName, String newAlbumName) {
        File dcimDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        File oldAlbumDirectory = new File(dcimDirectory, oldAlbumName);
        File newAlbumDirectory = new File(dcimDirectory, newAlbumName);

        if (oldAlbumDirectory.exists()) {
            oldAlbumDirectory.renameTo(newAlbumDirectory);
        }
    }



    // ====== Get File Name from Uri
    public static String getFileName(Context context, Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();
        if (scheme != null && scheme.equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

    // ====== Copy File
    private static void copyFile(InputStream inputStream, File destFile) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close(); // Close the OutputStream
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void scanAlbumForMediaStoreRegister(Context context, String folderPath, Handler handler) {
        MediaScannerConnection.scanFile(
                context,
                new String[]{folderPath},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        handler.post(() -> {
                            Log.d("ScannerMediaStore", "Scanned " + path);
                        });
                    }
                }
        );
    }


}

