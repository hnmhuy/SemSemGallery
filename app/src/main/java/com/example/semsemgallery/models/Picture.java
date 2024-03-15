package com.example.semsemgallery.models;

public class Picture {
    private String path;
    private String thumbnail;
    private String dateAdded;

    public Picture(String path, String thumbnail, String dateAdded) {
        this.path = path;
        this.thumbnail = thumbnail;
        this.dateAdded = dateAdded;
    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}
