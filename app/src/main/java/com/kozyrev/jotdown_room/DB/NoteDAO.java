package com.kozyrev.jotdown_room.DB;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface NoteDAO {

    @Insert
    long insert(Note note);

    @Insert
    void insertAll(Note... notes);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM NOTES where uid = :noteId")
    Note getNoteById(int noteId);

    @Query("SELECT * FROM NOTES where uid = :noteId")
    Single<Note> getSingleNoteById(int noteId);

    @Query("SELECT * FROM NOTES")
    List<Note> getAllNotes();

    @Query("SELECT * FROM NOTES")
    Flowable<List<Note>> getAllNotesFlowable();

    @Query("SELECT * FROM NOTES WHERE name LIKE '%' || :text || '%' OR description LIKE '%' || :text || '%'")
    Maybe<List<Note>> getAllNotesBySearchText(String text);

    @Query("SELECT COUNT(*) FROM NOTES")
    int getNotesCount();
}