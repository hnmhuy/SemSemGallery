package com.example.semsemgallery.models;

import java.util.Date;

public class Picture {
    private String path;
    private String fileName;
    private Date dateAdded;
    private String albumID;

    public String getAlbumID() {
        return albumID;
    }

    public void setAlbumID(String albumID) {
        this.albumID = albumID;
    }

    public Picture(String path, String fileName, Date dateAdded, String albumID) {
        this.path = path;
        this.fileName = fileName;
        this.dateAdded = dateAdded;
        this.albumID = albumID;
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

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }
}
