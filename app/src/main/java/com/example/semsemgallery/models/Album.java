package com.example.semsemgallery.models;


import java.util.List;

public class Album {

    private String albumId;
    private String name;

    public Album(String albumId, String name) {
        this.albumId = albumId;
        this.name = name;
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

}
