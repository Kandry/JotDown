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

    private LinearLayout buttonsLayout;
    private ImageButton recordButton;

    @SuppressLint("ValidFragment")
    public RecordingFragment(int noteId) {
        this. noteId = noteId;
        // Required empty public constructor
    }

    // Получить noteID, recordButton (c recordButton наоборот, получаем в активности местный список)
    // (не, всё здесь будет, так как работа с объектом noteAudioRecord)
    // http://developer.alexanderklimov.ru/android/theory/fragment-view.php

    // Но чтобы обеспечить модульность, мы будем передавать id кнопки из активности


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_recording, container, false);

        RecyclerView recyclerViewRecordings = rootView.findViewById(R.id.recyclerViewRecordings);
        recyclerViewRecordings.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerViewRecordings.setHasFixedSize(true);

        recordingArraylist = new ArrayList<Recording>();

        noteAudioRecord = new NoteAudioRecord(getContext(), rootView, recyclerViewRecordings, recordingArraylist, noteId);
        noteAudioRecord.fetchRecordings();

        recordButton = getActivity().findViewById(R.id.audioRecordingButton);
        buttonsLayout = getActivity().findViewById(R.id.buttonsLayout);

        return rootView;
    }
/*
    public void addRecord(View view){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getNeedPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO)){
                recordButtonClick();
            }
        } else {
            recordButtonClick();
        }
    }

    private void recordButtonClick(){
        isRecord = !isRecord;
        if (isRecord) {
             prepareForRecording();
             noteAudioRecord.startRecording();
        } else {
             prepareForStop();
             noteAudioRecord.stopRecording();
        }
    }

    private void prepareForRecording(){
        TransitionManager.beginDelayedTransition(buttonsLayout);
        int audioButtonColor = getResources().getColor(R.color.colorAccent);
        recordButton.setBackgroundColor(audioButtonColor);
        // Ограничить доступность остальных действий
    }

    private void prepareForStop(){
        TransitionManager.beginDelayedTransition(buttonsLayout);
        int audioButtonColor = getResources().getColor(0);
        recordButton.setBackgroundColor(audioButtonColor);
        // Снять ограничение доступности остальных действий
    }
*/
}
