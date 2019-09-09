package com.kozyrev.jotdown_room.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kozyrev.jotdown_room.Fragments.NotesFileFragment;
import com.kozyrev.jotdown_room.Fragments.RecordingFragment;

public class DetailNotePagerAdapter extends FragmentPagerAdapter {

    private int noteId;
    private RecordingFragment recordingFragment;

    public DetailNotePagerAdapter(FragmentManager fragmentManager, int noteId){
        super((fragmentManager));
        this.noteId = noteId;
        recordingFragment = new RecordingFragment(noteId);
    }

    public RecordingFragment getRecordingFragment(){
        return recordingFragment;
    }

    @Override
    public int getCount(){
        return 2;
    }

    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                return recordingFragment;//new RecordingFragment(noteId);
            case 1:
                return new NotesFileFragment(noteId);
            default:
                return null;
        }
    }
}
