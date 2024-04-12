package com.example.semsemgallery.models;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.io.File;
import java.util.Date;

public class Picture implements Parcelable, Comparable<Picture> {

    private static Calendar calendar = Calendar.getInstance();

    private String url;
    private long pictureId;
    private long dateInMillis;
    private String path;
    private String fileName;
    private Date dateTaken;
    private Date dateAdded;

    public long getDateInMillis() {
        return dateInMillis;
    }

    public void setDateInMillis(long dateInMillis) {
        this.dateInMillis = dateInMillis;
    }
    private String albumID;
    private boolean isFav;
    private long fileSize; // in byte
    private String albumName;
    private Bitmap bitmap;
    public Picture(String fileName, String url){
        this.url = url;
        this.fileName = fileName;
    }
    public Picture(Uri uri) {
        String path = uri.getPath();
        File file = new File(path);
        this.path = path;
        this.fileName = file.getName();
        this.dateTaken = new Date(file.lastModified());
        this.dateInMillis = this.dateTaken.getTime() / 1000;
    }
    public Picture(String path, String fileName, Date dateTaken, String albumID, boolean isFav) {
        this.path = path;
        this.fileName = fileName;
        this.dateTaken = dateTaken;
        this.albumID = albumID;
        this.isFav = isFav;
        dateInMillis = dateTaken.getTime() / 1000000;
    }
    protected Picture(Parcel in) {
        pictureId = in.readLong();
        dateInMillis = in.readLong();
        path = in.readString();
        fileName = in.readString();
        dateTaken = new Date(in.readLong());
        dateAdded = new Date(in.readLong());
        albumID = in.readString();
        albumName = in.readString();
        fileSize = in.readLong();
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

    public Picture(long pictureId, String path, String fileName, Date dateTaken, Date dateAdded, boolean isFav, long fileSize, String albumID, String albumName) {
        this.pictureId = pictureId;
        this.path = path;
        this.fileName = fileName;
        this.dateTaken = dateTaken;
        this.isFav = isFav;
        this.dateAdded = dateAdded;
        this.fileSize = fileSize;
        this.albumID = albumID;
        this.albumName = albumName;
        Date temp = dateTaken.getTime() == 0 ? dateAdded : dateTaken;
//        dateInMillis = Math.round((double) temp / 86400000);
        calendar.setTime(temp);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        dateInMillis = calendar.getTimeInMillis();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(pictureId);
        dest.writeLong(dateInMillis);
        dest.writeString(path);
        dest.writeString(fileName);
        dest.writeLong(dateTaken.getTime());
        dest.writeLong(dateAdded.getTime());
        dest.writeString(albumID);
        dest.writeString(albumName);
        dest.writeLong(fileSize);
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
    public String getUrl(){return url;}

    public void setDateAdded(Date value) {
        this.dateAdded = value;
    }

    public Date getDateAdded() {
        return dateAdded;
    }


    @Override
    public int compareTo(Picture other) {
        if (pictureId == other.pictureId) return 0;
        long time1 = dateTaken.getTime() == 0 ? dateAdded.getTime() : dateTaken.getTime();
        long time2 = other.getDateTaken().getTime() == 0 ? other.getDateAdded().getTime() : other.getDateTaken().getTime();
        int comparison = Long.compare(time1, time2);
        if (comparison == 0) {
            return 1;
        } else return comparison;
    }

    public void setBitmap(Bitmap input){
        this.bitmap = input.copy(input.getConfig(), true);
    }
    public Bitmap getBitmap(){return bitmap;}
}
