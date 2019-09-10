package com.kozyrev.jotdown_room.NoteDetail;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.kozyrev.jotdown_room.Adapter.FileItemTouchHelper;
import com.kozyrev.jotdown_room.Adapter.NotesFileAdapter;
import com.kozyrev.jotdown_room.Adapter.RecordItemTouchHelper;
import com.kozyrev.jotdown_room.Entities.NotesFile;
import com.kozyrev.jotdown_room.Entities.Recording;

import java.util.ArrayList;

public class NoteDetailFile implements FileItemTouchHelper.RecyclerItemTouchHelperListener {

    private Context context;
    private RecyclerView recyclerViewFiles;
    private View rootView;

    private NotesFileAdapter recordingAdapter;
    private ArrayList<NotesFile> fileArraylist;

    private int noteId;
    private String fullPath, fileName;

    public NoteDetailFile(Context context, View rootView, RecyclerView recyclerViewFiles, ArrayList<NotesFile> fileArraylist, int noteId){
        this.context = context;
        this.rootView = rootView;
        this.recyclerViewFiles = recyclerViewFiles;
        this.fileArraylist = fileArraylist;
        this.noteId = noteId;

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new FileItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewFiles);
    }



    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

    }
}
