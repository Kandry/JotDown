package com.kozyrev.jotdown_room.Fragments;


import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kozyrev.jotdown_room.DetailNoteActivity;
import com.kozyrev.jotdown_room.Entities.NotesFile;
import com.kozyrev.jotdown_room.NoteDetail.NoteDetailFile;
import com.kozyrev.jotdown_room.R;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class NotesFileFragment extends Fragment {

    private String fileUriString;
    private PackageManager packageManager;
    public NoteDetailFile noteDetailFile;

    @SuppressLint("ValidFragment")
    public NotesFileFragment(String fileUriString, PackageManager packageManager) {
        this.fileUriString = fileUriString;
        this.packageManager = packageManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notes_file, container, false);

        RecyclerView recyclerViewFiles = rootView.findViewById(R.id.recyclerViewFiles);
        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerViewFiles.setHasFixedSize(false);

        ArrayList<NotesFile> filesArraylist = new ArrayList<>();
        noteDetailFile = new NoteDetailFile(getContext(), rootView, recyclerViewFiles, filesArraylist, fileUriString, packageManager);
        noteDetailFile.fetchFiles();

        return rootView;
    }
}
