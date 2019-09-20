package com.kozyrev.jotdown_room.Fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kozyrev.jotdown_room.DetailNoteActivity;
import com.kozyrev.jotdown_room.MainActivity;
import com.kozyrev.jotdown_room.R;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class NoteLightFragment extends Fragment {

    TextInputEditText textTitleLight;
    Button detailButton;

    int noteId;

    @SuppressLint("ValidFragment")
    public NoteLightFragment(int noteId) {
        this.noteId = noteId;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note_light, container, false);
        textTitleLight = rootView.findViewById(R.id.textTitleLight);
        textTitleLight.setText(String.valueOf(noteId));
        detailButton = rootView.findViewById(R.id.note_light_detail);
        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DetailNoteActivity.class);
                intent.putExtra(DetailNoteActivity.EXTRA_NOTE_ID, noteId);
                startActivity(intent);
            }
        });
        return rootView;

        /*
        Intent intent = new Intent(MainActivity.this, DetailNoteActivity.class);
                        intent.putExtra(DetailNoteActivity.EXTRA_NOTE_ID, noteId);
                        startActivity(intent);
         */
    }

}
