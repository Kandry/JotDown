package com.kozyrev.jotdown_room.Fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kozyrev.jotdown_room.R;

@SuppressLint("ValidFragment")
public class NotesFileFragment extends Fragment {

    private int noteId;

    @SuppressLint("ValidFragment")
    public NotesFileFragment(int noteId) {
        this. noteId = noteId;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_notes_file, container, false);
        return rootView;
    }

}
