package com.example.semsemgallery.domain;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.semsemgallery.models.Album;
import com.example.semsemgallery.models.Picture;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MediaRetriever {
    private final AppCompatActivity activity;
    private List<Picture> pictureList;
    private List<Album> albumList;

    public MediaRetriever(AppCompatActivity activity) {
        this.activity = activity;
    }

    public List<Picture> getAllPictureList() {
        List<Picture> picturesList = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Images.Media.DATA, // đường dẫn tới ảnh đó
                MediaStore.Images.Media.DISPLAY_NAME, // tên file ảnh
                MediaStore.Images.Media.BUCKET_ID, // id album chứa
                MediaStore.Images.Media.DATE_ADDED, // ngay
                MediaStore.Images.Media.IS_FAVORITE
        };
        Cursor cursor = activity.getContentResolver().query(uri,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC");

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String path = cursor.getString((cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                    String name = cursor.getString((cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                    String albumID = cursor.getString((cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)));
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    Date dateAdded = new Date(timestamp * 1000L);
                    String isFavStr = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.IS_FAVORITE));
                    boolean isFav = Objects.equals(isFavStr, "1");
                    //picturesList.add(new Picture(path, name, dateAdded, albumID, isFav));
                }
            } finally {
                cursor.close();
            }

        }
        this.pictureList = picturesList;
        return pictureList;
    }

    public List<Picture> getPicturesByAlbumId(String albumID) {
        List<Picture> picturesByAlbum = new ArrayList<>();

        // Assuming getAllPictureList() method retrieves all pictures
        List<Picture> allPictures = getAllPictureList();

        // Iterate through all pictures and filter those with matching album ID
        for (Picture picture : allPictures) {
            if (picture.getAlbumID().equals(albumID)) {
                picturesByAlbum.add(picture);
            }
        }
        return picturesByAlbum;
    }

    public void setPictureList(List<Picture> pictureList) {
        this.pictureList = pictureList;
    }

    public List<Album> getAlbumList() {
        List<Album> albumListTmp = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_ID, // id album chứa
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME // tên album
        };
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    boolean checkPresent = false;
                    String imgWall = cursor.getString((cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                    String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                    String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    // Create an Album object and add it to the list if it's not already present
                    if (!albumListTmp.isEmpty()) {
                        for (int i = 0; i < albumListTmp.size(); i++) {
                            if (Objects.equals(albumListTmp.get(i).getAlbumId(), albumId)) {
                                checkPresent = true;
                                albumListTmp.get(i).setCount(albumListTmp.get(i).getCount() + 1);
                                break;
                            }
                        }
                    }

                    if (!checkPresent) {
                        Log.d("Image path", imgWall);
                        Album album = new Album(albumId, imgWall, albumName);
                        album.setCount(1);
                        albumListTmp.add(album);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        this.albumList = albumListTmp;

        return albumList;
    }

    public void setAlbumList(List<Album> albumList) {
        this.albumList = albumList;
    }

    public List<Picture> getFavoriteAlbumList() {
        List<Picture> picturesList = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media.DATA, // đường dẫn tới ảnh đó
                MediaStore.Images.Media.DISPLAY_NAME, // tên file ảnh
                MediaStore.Images.Media.BUCKET_ID, // id album chứa
                MediaStore.Images.Media.DATE_ADDED // ngay
        };
        Cursor cursor = activity.getContentResolver().query(uri,
                projection,
                MediaStore.Images.Media.IS_FAVORITE,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC");

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String path = cursor.getString((cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                    String name = cursor.getString((cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                    String albumID = cursor.getString((cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)));
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    Date dateAdded = new Date(timestamp * 1000L);
                    //picturesList.add(new Picture(path, name, dateAdded, albumID, true));
                }
            } finally {
                cursor.close();
            }

        }
        this.pictureList = picturesList;
        return pictureList;
    }

}
