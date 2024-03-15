package com.example.semsemgallery.models;


import java.util.List;

public class Album {
    private String pathFolder;
    private Picture img;
    private String name;
    private List<Picture> listName;

    public Album(Picture img, String name, List<Picture> listName) {
        this.img = img;
        this.name = name;
        this.listName = listName;
    }

    public String getPathFolder() {
        return pathFolder;
    }

    public void setPathFolder(String pathHolder) {
        this.pathFolder = pathHolder;
    }

    public Picture getImg() {
        return img;
    }

    public void setImg(Picture img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Picture> getListName() {
        return listName;
    }

    public void setListName(List<Picture> listName) {
        this.listName = listName;
    }
}
