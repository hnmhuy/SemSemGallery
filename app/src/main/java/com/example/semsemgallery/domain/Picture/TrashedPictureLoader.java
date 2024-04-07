package com.example.semsemgallery.domain.Picture;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.example.semsemgallery.domain.TaskBase;
import com.example.semsemgallery.models.MetaData;
import com.example.semsemgallery.models.TrashedPicture;

import java.util.Date;

public abstract class TrashedPictureLoader extends TaskBase<Void, TrashedPicture, Boolean> {

    private Context context;
    private static String[] PROJECTION = {
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_EXPIRES,
            MediaStore.Files.FileColumns._ID
    };

    // Construction
    public TrashedPictureLoader(Context _context) {
        this.context = _context;
    }

    private static Bundle queryBundle;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void preExecute(Void... voids) {
        queryBundle = new Bundle();
        queryBundle.putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE);
        queryBundle.putString("android:query-arg-sql-selection", MediaStore.MediaColumns.IS_TRASHED + " = 1");
        queryBundle.putString("android:query-arg-sql-sort-order", MediaStore.MediaColumns.DATE_MODIFIED + " DESC");
    }

    @Override
    public Boolean doInBackground(Void... voids) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                PROJECTION,
                queryBundle,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                long dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_EXPIRES));
                Date expired = new Date(dateLong * 1000L);
                mHandler.post(() -> onProcessUpdate(new TrashedPicture(id, path, expired)));
            }
            cursor.close();
        }

        return null;
    }

}
