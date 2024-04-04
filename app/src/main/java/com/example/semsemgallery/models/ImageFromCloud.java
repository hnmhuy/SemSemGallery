package com.example.semsemgallery.models;

public class ImageFromCloud {
    String name, url;
    public ImageFromCloud(){

    }
    public void setName(String value){
        name = value;
    }
    public void setUrl(String value){
        url = value;
    }
    public String getName(){
        return name;
    }
    public String getUrl(){
        return url;
    }
}
