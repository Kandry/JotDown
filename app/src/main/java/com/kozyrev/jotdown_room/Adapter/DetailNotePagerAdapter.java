package com.kozyrev.jotdown_room.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kozyrev.jotdown_room.Fragments.NotesFileFragment;
import com.kozyrev.jotdown_room.Fragments.RecordingFragment;

public class DetailNotePagerAdapter extends FragmentPagerAdapter {

    public DetailNotePagerAdapter(FragmentManager fragmentManager){
        super((fragmentManager));
    }

    @Override
    public int getCount(){
        return 2;
    }

    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                return new NotesFileFragment();
            case 1:
                return new RecordingFragment();
            default:
                return null;
        }
    }
}
