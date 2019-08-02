package com.kozyrev.jotdown_room.DB;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "NOTES")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    private String name;

    private String description;

    private String imageResourceUri;

    public Note(String name, String description, String imageResourceUri){
        this.name = name;
        this.description = description;
        this.imageResourceUri = imageResourceUri;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageResourceUri() {
        return imageResourceUri;
    }

    public void setImageResourceUri(String imageResourceUri) {
        this.imageResourceUri = imageResourceUri;
    }
}
