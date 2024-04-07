package com.example.semsemgallery.models;


public class Album {

    private String albumId;
    private String wallId;
    private String name;
    private int count;


    public Album(String albumId, String wallId, String name) {
        this.albumId = albumId;
        this.wallId = wallId;
        this.name = name;
        this.count = 0;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getWallId() {
        return wallId;
    }

    public void setWallId(String wallId) {
        this.wallId = wallId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
