package com.kozyrev.jotdown_room.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kozyrev.jotdown_room.CustomViews.WrapContentHeightViewPager;
import com.kozyrev.jotdown_room.Fragments.NotesFileFragment;
import com.kozyrev.jotdown_room.Fragments.RecordingFragment;

public class DetailNotePagerAdapter extends FragmentPagerAdapter {

    private RecordingFragment recordingFragment;
    private NotesFileFragment notesFileFragment;
    private WrapContentHeightViewPager wrapContentViewPager;

    public void updatePagerAdapterRecordsCount(int recordsCount){
        if (recordsCount > 0) wrapContentViewPager.setCurrentItem(0);
        else wrapContentViewPager.setCurrentItem(1);

        this.notifyDataSetChanged();
    }

    public void updatePagerAdapterFilesCount(int filesCount){
        if (filesCount > 0) wrapContentViewPager.setCurrentItem(1);
        else wrapContentViewPager.setCurrentItem(0);

        this.notifyDataSetChanged();
    }

    public DetailNotePagerAdapter(FragmentManager fragmentManager, int noteId, String fileUriString, WrapContentHeightViewPager wrapContentViewPager){
        super((fragmentManager));
        recordingFragment = new RecordingFragment(noteId, this);
        notesFileFragment = new NotesFileFragment(fileUriString, this);
        this.wrapContentViewPager = wrapContentViewPager;
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
