package com.kozyrev.jotdown_room.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.kozyrev.jotdown_room.Entities.Recording;
import com.kozyrev.jotdown_room.NoteDetail.NoteAudioRecord;
import com.kozyrev.jotdown_room.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordingFragment extends Fragment {

    private ArrayList<Recording> recordingArraylist;


    public RecordingFragment() {
        // Required empty public constructor
    }

    // Получить noteID, recordButton (c recordButton наоборот, получаем в активности местный список)
    // (не, всё здесь будет, так как работа с объектом noteAudioRecord)
    // http://developer.alexanderklimov.ru/android/theory/fragment-view.php


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_recording, container, false);

        RecyclerView recordingsRecyclerView = rootView.findViewById(R.id.recyclerViewRecordings);

        recordingArraylist = new ArrayList<Recording>();

        NoteAudioRecord noteAudioRecord = new NoteAudioRecord(getContext(), rootView, recordingsRecyclerView, recordingArraylist);
        noteAudioRecord.fetchRecordings();

        ImageButton recordButton = getActivity().findViewById(R.id.audioRecordingButton);

        return rootView;
    }

}
