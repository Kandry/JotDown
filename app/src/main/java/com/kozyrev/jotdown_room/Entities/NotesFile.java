package com.kozyrev.jotdown_room.Entities;

public class NotesFile {

    private String Uri, fullPath, fileName, extension;

    public NotesFile(String uri) {
        Uri = uri;
    }

    public String getUri() {
        return Uri;
    }

    public void setUri(String uri) {
        Uri = uri;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /*public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }*/
}
