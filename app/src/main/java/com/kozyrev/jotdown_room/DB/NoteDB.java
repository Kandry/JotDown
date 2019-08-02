package com.kozyrev.jotdown_room.DB;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Note.class}, version = 1)
public abstract class NoteDB extends RoomDatabase {
    public abstract NoteDAO getNoteDAO();
}
