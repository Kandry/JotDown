package com.kozyrev.jotdown_room.Entities;

public class NotesFile {

    String Uri, fileName;

    public NotesFile(String uri) {
        Uri = uri;
    }

    public String getUri() {
        return Uri;
    }

    public String getFileName() {
        return fileName;
    }
}
