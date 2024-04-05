package com.example.semsemgallery.domain.Picture;

import android.provider.MediaStore;

import com.example.semsemgallery.domain.TaskBase;
import com.example.semsemgallery.models.Picture;

public class PictureLoader extends TaskBase<String, Picture, Void> {
    private static String[] PROJECTION_ALL = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED
    };

    @Override
    public void preExecute(String... strings) {

    }

    @Override
    public Void doInBackground(String... strings) {
        return null;
    }

    @Override
    public void onProcessUpdate(Picture... pictures) {

    }

    @Override
    public void postExecute(Void res) {

    }
}
