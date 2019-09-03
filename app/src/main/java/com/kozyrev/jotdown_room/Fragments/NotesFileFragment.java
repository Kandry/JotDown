package com.kozyrev.jotdown_room.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kozyrev.jotdown_room.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotesFileFragment extends Fragment {


    public NotesFileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_notes_file, container, false);
        return rootView;
    }

}
