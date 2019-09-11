package com.kozyrev.jotdown_room.Adapter;

import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kozyrev.jotdown_room.Fragments.NotesFileFragment;
import com.kozyrev.jotdown_room.Fragments.RecordingFragment;

public class DetailNotePagerAdapter extends FragmentPagerAdapter {

    private RecordingFragment recordingFragment;
    private NotesFileFragment notesFileFragment;

    public DetailNotePagerAdapter(FragmentManager fragmentManager, int noteId, String fileUriString, PackageManager packageManager){
        super((fragmentManager));
        recordingFragment = new RecordingFragment(noteId);
        notesFileFragment = new NotesFileFragment(fileUriString, packageManager);
    }

    public RecordingFragment getRecordingFragment(){
        return recordingFragment;
    }

    public NotesFileFragment getNotesFileFragment() {
        return notesFileFragment;
    }

    @Override
    public int getCount(){
        return 2;
    }

    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                return recordingFragment;
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
                return "Records";
            case 1:
                return "Files";
            default:
                return "";
        }
    }
}
