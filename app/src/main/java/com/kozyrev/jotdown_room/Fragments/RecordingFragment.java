package com.kozyrev.jotdown_room.Fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.kozyrev.jotdown_room.Entities.Recording;
import com.kozyrev.jotdown_room.NoteDetail.NoteAudioRecord;
import com.kozyrev.jotdown_room.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class RecordingFragment extends Fragment {

    private int noteId;
    private boolean isRecord = false;
    private ArrayList<Recording> recordingArraylist;
    public NoteAudioRecord noteAudioRecord;

    @SuppressLint("ValidFragment")
    public RecordingFragment(int noteId) {
        this. noteId = noteId;
    }

    // http://developer.alexanderklimov.ru/android/theory/fragment-view.php


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recording, container, false);

        RecyclerView recyclerViewRecordings = rootView.findViewById(R.id.recyclerViewRecordings);
        recyclerViewRecordings.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerViewRecordings.setHasFixedSize(false);

        recordingArraylist = new ArrayList<Recording>();
        noteAudioRecord = new NoteAudioRecord(getContext(), rootView, recyclerViewRecordings, recordingArraylist, noteId);
        noteAudioRecord.fetchRecordings();

        return rootView;
    }
}
