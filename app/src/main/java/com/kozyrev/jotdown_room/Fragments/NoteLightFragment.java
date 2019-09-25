package com.kozyrev.jotdown_room.Fragments;


import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kozyrev.jotdown_room.DB.Note;
import com.kozyrev.jotdown_room.DB.NoteDB;
import com.kozyrev.jotdown_room.DetailNoteActivity;
import com.kozyrev.jotdown_room.MainActivity;
import com.kozyrev.jotdown_room.R;

@SuppressLint("ValidFragment")
public class NoteLightFragment extends Fragment {

    TextInputEditText textTitleLight, textDescriptionLight;
    Button detailButton;

    int noteId, position;
    String title, description;

    @SuppressLint("ValidFragment")
    public NoteLightFragment(int noteId, String title, String description, int position) {
        this.noteId = noteId;
        this.title = title;
        this.description = description;
        this.position = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note_light, container, false);

        initViews(rootView);
        textTitleLight.setText(String.valueOf(title));
        textDescriptionLight.setText(String.valueOf(description));

        detailButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), DetailNoteActivity.class);
            intent.putExtra(DetailNoteActivity.EXTRA_NOTE_ID, noteId);
            startActivity(intent);
        });
        return rootView;
    }

    @Override
    public void onPause(){
        super.onPause();

        String newTitle = textTitleLight.getText().toString();
        String newDescription = textDescriptionLight.getText().toString();

        if (!((title.equals(newTitle) && (description.equals(newDescription))))){
            NoteDB db = Room.databaseBuilder(getContext(), NoteDB.class, "notedatabase")
                    .allowMainThreadQueries()
                    .build();
            Note note = db.getNoteDAO().getNoteById(noteId);
            note.setName(textTitleLight.getText().toString());
            note.setDescription(textDescriptionLight.getText().toString());
            db.getNoteDAO().update(note);
            ((MainActivity) getActivity()).updateItem(position, note);
        }
    }

    public int getNoteId(){
        return noteId;
    }

    private void initViews(View rootView){
        textTitleLight = rootView.findViewById(R.id.textTitleLight);
        textDescriptionLight = rootView.findViewById(R.id.textDescriptionLight);
        detailButton = rootView.findViewById(R.id.note_light_detail);
    }

}
