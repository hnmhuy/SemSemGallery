package com.example.semsemgallery.models;

public class Tag {
    private int tagId;
    private String name;
    private int type;
    private boolean isEditTag;
    // 1 - Default tag (Gallery)
    // 2 - Recent tag
    // 3 - Is picture tag
    // 4 - Temporary chosen tag

    public boolean isEditTag() {
        return isEditTag;
    }

    public void setEditTag(boolean editTag) {
        isEditTag = editTag;
    }

    public Tag(int tagId, String name) {
        this.tagId = tagId;
        this.name = name;
        this.type = 1; // SQLite TAG
        this.isEditTag = false; // default
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
