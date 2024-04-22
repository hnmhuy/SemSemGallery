package com.example.semsemgallery.models;

import java.util.Date;

public class TrashedPicture {
    private String path;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private long id;
    private String originPath;
    private Date expired;

    public TrashedPicture(long id, String path, Date expired) {
        this.id = id;
        this.path = path;
        this.expired = expired;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOriginPath() {
        return originPath;
    }

    public void setOriginPath(String originPath) {
        this.originPath = originPath;
    }

    public Date getExpired() {
        return expired;
    }

    public void setExpired(Date expired) {
        this.expired = expired;
    }
}
