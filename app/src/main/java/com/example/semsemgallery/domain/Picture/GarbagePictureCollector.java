package com.example.semsemgallery.domain.Picture;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.semsemgallery.domain.TaskBase;
import com.example.semsemgallery.models.TrashedPicture;

public class GarbagePictureCollector {

    private static GarbagePictureCollector instance;

    private GarbagePictureCollector() {
    }
    public static GarbagePictureCollector getInstance() {
        if (instance == null) {
            instance = new GarbagePictureCollector();
        }
        return instance;
    }

    //== Trash function in another thread
    public static abstract class TrashPictureHandler extends TaskBase<Long, Integer, Void> {
        private final Context context;
        private int isTrash = 1; // Default value
        public TrashPictureHandler(Context context) {
            this.context = context;
        }

        public TrashPictureHandler(Context context, int signal) {
            this.context = context;
            setIsTrash(signal);
        }

        public void setIsTrash(int isTrash) {
            if (isTrash != 0 && isTrash != 1) this.isTrash = 0;
            else this.isTrash = isTrash;
        }

        @Override
        public Void doInBackground(Long... pictures) {
            int complete = 0;
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.IS_TRASHED, isTrash);
            ContentResolver resolver = context.getContentResolver();
            for (Long p : pictures) {
                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p);
                resolver.update(uri, values, null, null);
                complete++;
                int finalComplete = complete;
                mHandler.post(() -> onProcessUpdate(finalComplete));
            }

            return null;
        }
    }

}
