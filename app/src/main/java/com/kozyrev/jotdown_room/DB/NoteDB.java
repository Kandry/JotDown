package com.kozyrev.jotdown_room.DB;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {Note.class}, version = 1)
//@TypeConverters({DateConverter.class})
public abstract class NoteDB extends RoomDatabase {
    public abstract NoteDAO getNoteDAO();
}
