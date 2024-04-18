package com.example.semsemgallery.domain.Album;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.semsemgallery.domain.TaskBase;
import com.example.semsemgallery.models.Album;

public abstract class AlbumLoader extends TaskBase<String, Album, Void> {

    private final Context context;
    private static String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.DATE_ADDED
    };

    private static String order =
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC, "
                    + MediaStore.Images.Media.BUCKET_ID + " DESC, "
                    + MediaStore.Images.Media.DATE_ADDED + " DESC";


    public AlbumLoader(Context context) {
        this.context = context;
    }

    @Override
    public void preExecute(String... strings) {

    }

    @Override
    public Void doInBackground(String... strings) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                order
        );

        if (cursor != null) {
            try {
                String currId = "";
                Album currAlbum = null;
                while (cursor.moveToNext()) {
                    String bucketId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                    // Found new bucket
                    if (!currId.equals(bucketId) || cursor.isLast()) {
                        if (currAlbum != null) {
                            Album transferAlbum = currAlbum;
                            mHandler.post(() -> onProcessUpdate(transferAlbum));
                        }
                        currId = bucketId;
                        String bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                        String wallPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        String wallId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        currAlbum = new Album(bucketId, wallId, wallPath, bucketName);
                        currAlbum.setCount(currAlbum.getCount() + 1);
                        Album finalCurrAlbum = currAlbum;
                    } else {
                        if (currAlbum != null) currAlbum.setCount(currAlbum.getCount() + 1);
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return null;
    }

    @Override
    public void postExecute(Void res) {

    }
}
