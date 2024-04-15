    package com.example.semsemgallery.domain.Album;

    import android.content.Context;
    import android.database.Cursor;
    import android.net.Uri;
    import android.os.Environment;
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

        public interface OnLoadingListener {
            void onLoadingComplete();
            void onLoadingProgressUpdate(int progress);
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
            new Thread(() -> {
                int totalImages = imageUris.size();
                isHandling = true;

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
                        if (destFile.exists()) { continue; }

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
                        if (originalFile.delete()) {
                            Log.d("Deleted", "File " + fileName + " deleted successfully");
                        } else {
                            Log.d("Deleted", "Failed to delete file " + fileName);
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
            new Thread(() -> {
                int totalImages = imageUris.size();
                isHandling = true;

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
                        if (destFile.exists()) { continue; }

                        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                        if (inputStream != null) {
                            copyFile(inputStream, destFile);

                            int progress = (int) (((i + 1) / (float) totalImages) * 100);
                            listener.onLoadingProgressUpdate(progress);

                            inputStream.close();

                            // Delete the original file
                            File originalFile = new File(imageUri.getPath());
                            if (originalFile.delete()) {
                                Log.d("Deleted", "File " + fileName + " deleted successfully");
                            } else {
                                Log.d("Deleted", "Failed to delete file " + fileName);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                listener.onLoadingComplete();
            }).start();
        }

        // ====== Delete Album (Remembers to check if the album exists before deleting)
        public static void deleteAlbumHandler(Context context, String albumName, OnLoadingListener listener) {
            new Thread(() -> {
                isHandling = true;

                File dcimDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                File albumDirectory = new File(dcimDirectory, albumName);

                if (albumDirectory.exists()) {
                    File[] files = albumDirectory.listFiles();
                    int totalFiles = files != null ? files.length : 0;
                    int deletedFiles = 0;

                    if (files != null) {
                        for (File file : files) {
                            if (!isHandling) break;
                            file.delete();
                            deletedFiles++;

                            int progress = (int) (((float) deletedFiles / totalFiles) * 100);
                            listener.onLoadingProgressUpdate(progress);
                        }
                    }
                    albumDirectory.delete();
                }

                listener.onLoadingComplete();
            }).start();
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
        private static String getFileName(Context context, Uri uri) {
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


    }
