package com.kozyrev.jotdown_room.NoteDetail;


import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.kozyrev.jotdown_room.Adapter.FileItemTouchHelper;
import com.kozyrev.jotdown_room.Adapter.NotesFileAdapter;
import com.kozyrev.jotdown_room.Entities.NotesFile;

import java.util.ArrayList;

public class NoteDetailFile implements FileItemTouchHelper.RecyclerItemTouchHelperListener {

    private Context context;
    private RecyclerView recyclerViewFiles;
    private View rootView;

    private NotesFileAdapter fileAdapter;
    private ArrayList<NotesFile> fileArraylist;

    private String fileUriString;
    private Uri fileUri;

    public NoteDetailFile(Context context, View rootView, RecyclerView recyclerViewFiles, ArrayList<NotesFile> fileArraylist, String fileUriString){
        this.context = context;
        this.rootView = rootView;
        this.recyclerViewFiles = recyclerViewFiles;
        this.fileArraylist = fileArraylist;
        this.fileUriString = fileUriString;

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new FileItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewFiles);
    }

    public void fetchFiles(){
        String[] filesUri = fileUriString.split(";");
        for(String fileUri : filesUri){
            addFileToFileArrayList(fileUri);
        }
        setAdapterToRecyclerView();
    }

    private void setAdapterToRecyclerView(){
        fileAdapter = new NotesFileAdapter(context, fileArraylist);
        recyclerViewFiles.setAdapter(fileAdapter);
    }

    private void addFileToFileArrayList(String fileUri){
        NotesFile notesFile = new NotesFile(fileUri);
        fileArraylist.add(notesFile);
    }

    public void addFileUri(String fileUri){
        fileUriString = fileUriString.concat(";" + fileUri);
    }

    public String getFileUriString() {
        return fileUriString;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof NotesFileAdapter.ViewHolder){
            NotesFile notesFile = fileArraylist.get(position);
            fileArraylist.remove(position);
            fileAdapter.notifyUpdateRecordsList(fileArraylist);

            Snackbar snackbar = Snackbar
                    .make(rootView, "File deleted from note", Snackbar.LENGTH_LONG);
            snackbar.addCallback(new Snackbar.Callback(){
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    switch (event) {
                        case DISMISS_EVENT_TIMEOUT:
                        case DISMISS_EVENT_CONSECUTIVE:
                        case DISMISS_EVENT_MANUAL:
                            fileUriString = fileUriString.replace(notesFile.getUri(), "");
                            fileUriString = fileUriString.replace(";;", ";");
                            fileUriString = fileUriString.replace("; ", "");
                            break;
                    }
                }
            });
            snackbar.setAction("UNDO", v -> {
                fileArraylist.add(position, notesFile);
                fileAdapter.notifyUpdateRecordsList(fileArraylist);
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
