package com.kozyrev.jotdown_room.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kozyrev.jotdown_room.Fragments.NotesFileFragment;
import com.kozyrev.jotdown_room.Fragments.RecordingFragment;

public class DetailNotePagerAdapter extends FragmentPagerAdapter {

    private RecordingFragment recordingFragment;
    private NotesFileFragment notesFileFragment;
    private int recordsCount = 1, filesCount = 1;

    public void updatePagerAdapterRecordsCount(int recordsCount){
        this.recordsCount = recordsCount;
        this.notifyDataSetChanged();
    }

    public void updatePagerAdapterFilesCount(int filesCount){
        this.filesCount = filesCount;
        this.notifyDataSetChanged();
    }

    public DetailNotePagerAdapter(FragmentManager fragmentManager, int noteId, String fileUriString){
        super((fragmentManager));
        recordingFragment = new RecordingFragment(noteId, this);
        notesFileFragment = new NotesFileFragment(fileUriString, this);
    }

    public RecordingFragment getRecordingFragment(){
        return recordingFragment;
    }

    public NotesFileFragment getNotesFileFragment() {
        return notesFileFragment;
    }

    @Override
    public int getCount(){
        if (recordsCount > 0 || filesCount > 0) {
            if (recordsCount > 0 && filesCount > 0) return 2;
            else return 1;
        } else {
            return 0;
        }
    }

    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                if (recordsCount > 0) return recordingFragment;
                else return notesFileFragment;
            case 1:
                return notesFileFragment;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                if (recordsCount > 0) return "Records";
                else return "Files";
            case 1:
                return "Files";
            default:
                return "";
        }
    }
}
