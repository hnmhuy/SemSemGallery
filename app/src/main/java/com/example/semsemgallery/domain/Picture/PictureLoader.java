package com.example.semsemgallery.domain.Picture;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.semsemgallery.domain.TaskBase;
import com.example.semsemgallery.models.Picture;

import java.util.Date;
import java.util.Objects;

public abstract class PictureLoader extends TaskBase<String, Picture, Boolean> {
    private final Context context;

    private final static String[] PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.IS_FAVORITE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.IS_TRASHED
    };

    private final static String[] PROJECTION_TRASHED = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.IS_FAVORITE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.IS_TRASHED,
            MediaStore.Images.Media.DATE_EXPIRES,
    };


    private static final String TRASH_SELECTOR = MediaStore.Images.Media.IS_TRASHED + "=? ";
    private static final String BY_ALBUM = MediaStore.Images.Media.BUCKET_ID + "=?";
    private static final String ORDER_DEFAULT = MediaStore.Images.Media.DATE_TAKEN + " DESC, " + MediaStore.Images.Media.DATE_ADDED + " DESC";

    public PictureLoader(Context _context) {
        this.context = _context;
    }


    @Override
    public void preExecute(String... strings) {

    }

    @Override
    public Boolean doInBackground(String... strings) {
        String mode = strings[0];
        if (Objects.equals(mode, PictureLoadMode.ALL.toString())) {
            LoadAll();
        } else if (Objects.equals(mode, PictureLoadMode.BY_ALBUM.toString())) {
            LoadByAlbum(strings[1]);
        } else if (Objects.equals(mode, PictureLoadMode.FAVORITE.toString())) {
            LoadFavorite();
        }
        return false;
    }

    private void CoreLoader(Cursor cursor) {
        if (cursor != null) {
            try (cursor) {
                while (cursor.moveToNext()) {
                    long _id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    long dateTakenInMillis = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                    long dateAddInSecond = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    boolean isFav = Objects.equals(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.IS_FAVORITE)), "1");
                    long fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                    String bucketId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                    String bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    Picture pic = new Picture(_id, path, fileName, new Date(dateTakenInMillis), new Date(dateAddInSecond * 1000L), isFav, fileSize, bucketId, bucketName);
                    mHandler.post(() -> onProcessUpdate(pic));
                }
            }
        }
    }

    protected void LoadFavorite() {
        String[] args = {"1"};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION,
                MediaStore.Images.Media.IS_FAVORITE + "= ?",
                args,
                ORDER_DEFAULT
        );
        CoreLoader(cursor);
    }

    protected void LoadAll() {
        String[] args = {"0"};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION,
                TRASH_SELECTOR,
                args,
                ORDER_DEFAULT
        );
        CoreLoader(cursor);
    }

    protected void LoadByAlbum(String bucketId) {
        String[] args = {bucketId};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_TRASHED,
                BY_ALBUM,
                args,
                ORDER_DEFAULT
        );
        CoreLoader(cursor);
    }


}
