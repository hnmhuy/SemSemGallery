package com.example.semsemgallery.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Picture implements Parcelable {
    private long pictureId;
    private String path;
    private String fileName;
    private Date dateTaken;
    private String albumID;
    private boolean isFav;
    private long fileSize;
    private String albumName;

    public Picture(String path, String fileName, Date dateAdded, String albumID, boolean isFav) {
        this.path = path;
        this.fileName = fileName;
        this.dateTaken = dateAdded;
        this.albumID = albumID;
        this.isFav = isFav;
    }
    protected Picture(Parcel in) {
        path = in.readString();
        fileName = in.readString();
        long dateLong = in.readLong();
        dateTaken = new Date(dateLong);
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

    public Picture(long pictureId, String path, String fileName, Date dateTaken, boolean isFav, long fileSize, String albumID, String albumName) {
        this.pictureId = pictureId;
        this.path = path;
        this.fileName = fileName;
        this.dateTaken = dateTaken;
        this.isFav = isFav;
        this.fileSize = fileSize;
        this.albumID = albumID;
        this.albumName = albumName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(fileName);
        dest.writeLong(dateTaken.getTime());
        dest.writeString(albumID);
        dest.writeByte((byte) (isFav ? 1 : 0));
    }

    public long getPictureId() {
        return pictureId;
    }

    public void setPictureId(long pictureId) {
        this.pictureId = pictureId;
    }

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

    public Date getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(Date dateTaken) {
        this.dateTaken = dateTaken;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setFav(boolean fav) {
        isFav = fav;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getAlbumID() {
        return albumID;
    }

    public void setAlbumID(String albumID) {
        this.albumID = albumID;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }
}
