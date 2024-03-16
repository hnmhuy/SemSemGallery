package com.example.semsemgallery.models;


import java.util.List;

public class Album {

    private String albumId;
    private String imgWall;
    private String name;
    private int size;



    public Album(String albumId, String imgWall, String name) {
        this.albumId = albumId;
        this.imgWall = imgWall;
        this.name = name;
        this.size = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }
    public String getImgWall() {
        return imgWall;
    }

    public void setImgWall(String imgWall) {
        this.imgWall = imgWall;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
