package com.example.semsemgallery.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Picture implements Parcelable {
    private String path;
    private String fileName;
    private Date dateAdded;
    private String albumID;
    private boolean isFav;

    public String getAlbumID() {
        return albumID;
    }

    public void setAlbumID(String albumID) {
        this.albumID = albumID;
    }

    public Picture(String path, String fileName, Date dateAdded, String albumID, boolean isFav) {
        this.path = path;
        this.fileName = fileName;
        this.dateAdded = dateAdded;
        this.albumID = albumID;
        this.isFav = isFav;
    }
    protected Picture(Parcel in) {
        path = in.readString();
        fileName = in.readString();
        long dateLong = in.readLong();
        dateAdded = new Date(dateLong);
        albumID = in.readString();
        isFav = in.readByte() != 0;
    }

    public static final Creator<Picture> CREATOR = new Creator<Picture>() {
        @Override
        public Picture createFromParcel(Parcel in) {
            return new Picture(in);
        }

        @Override
        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setFav(boolean fav) {
        isFav = fav;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(fileName);
        dest.writeLong(dateAdded.getTime());
        dest.writeString(albumID);
        dest.writeByte((byte) (isFav ? 1 : 0));
    }
}
