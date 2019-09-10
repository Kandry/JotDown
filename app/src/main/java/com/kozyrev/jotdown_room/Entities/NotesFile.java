package com.kozyrev.jotdown_room.Entities;

public class NotesFile {

    private String Uri, fileName, extension;

    public NotesFile(String uri) {
        Uri = uri;
    }

    public String getUri() {
        return Uri;
    }

    public String getFileName() {
        return fileName;
    }

    public String getExtension() {
        return extension;
    }
}
