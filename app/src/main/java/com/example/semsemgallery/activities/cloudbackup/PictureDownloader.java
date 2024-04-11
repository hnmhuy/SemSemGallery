package com.example.semsemgallery.activities.cloudbackup;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import com.example.semsemgallery.domain.TaskBase;
import com.example.semsemgallery.models.Picture;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import javax.xml.transform.Result;

public class PictureDownloader extends TaskBase<Picture, Void, Void> {
    private String urlString;
    private Handler handler;
    private Context contextRef;

    public PictureDownloader(Context context) {
        contextRef = context;
    }

    public static void createImage(Context context, Bitmap bitmap){
        OutputStream outputStream;
        Uri uri = null;
        ContentResolver contentResolver = context.getContentResolver();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        if(uri == null){
            uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        }

        String img = String.valueOf(Calendar.getInstance().getTimeInMillis());
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.DISPLAY_NAME, img + ".jpg");
        cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + "Cloud/");
        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        Uri finalUri = contentResolver.insert(uri, cv);
        try{
            outputStream = contentResolver.openOutputStream(Objects.requireNonNull(finalUri));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Objects.requireNonNull(outputStream);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }


    @Override
    public void preExecute(Picture... pictures) {

    }

    @Override
    public Void doInBackground(Picture... pictures) {
        Bitmap bitmap = null;
        try {
            for (Picture p: pictures
            ) {
                URL url = new URL(p.getUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                createImage(contextRef, bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onProcessUpdate(Void... voids) {

    }

    @Override
    public void postExecute(Void res) {

    }
}


