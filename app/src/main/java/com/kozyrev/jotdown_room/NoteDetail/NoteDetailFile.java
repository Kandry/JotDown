package com.kozyrev.jotdown_room.NoteDetail;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.kozyrev.jotdown_room.Adapter.FileItemTouchHelper;
import com.kozyrev.jotdown_room.Adapter.NotesFileAdapter;
import com.kozyrev.jotdown_room.DetailNoteActivity;
import com.kozyrev.jotdown_room.Entities.NotesFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NoteDetailFile implements FileItemTouchHelper.RecyclerItemTouchHelperListener {

    private Context context;
    private RecyclerView recyclerViewFiles;
    private View rootView;

    private NotesFileAdapter fileAdapter;
    private ArrayList<NotesFile> fileArraylist;
    private NotesFileAdapter.FilesListener filesListener;

    private String fileUriString;

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
        if (!fileUriString.equals("")) {
            String[] filesUriAndName = fileUriString.split(";");
            for (String fileUriAndName : filesUriAndName) {
                if (!fileUriAndName.equals("")) {
                    String[] fileUriName = fileUriAndName.split("<");
                    String fileUri = fileUriName[0];
                    String fileName = fileUriName[1];
                    if (!fileUri.equals("")) addFileToFileArrayList(fileUri, fileName);
                }
            }
        }
        setAdapterToRecyclerView();
    }

    public void setPackageManager(PackageManager packageManager){
        filesListener = new NotesFileAdapter.FilesListener() {
            @Override
            public void onClick(int position) {
                NotesFile notesFile = fileArraylist.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String intentType = "application/" + notesFile.getExtension();
                intent.setType(intentType);
                List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                //File file = new File(notesFile.getUri());

                if (list.size() > 0 /*&& file.isFile()*/){
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse(notesFile.getUri()); //Uri.fromFile(file);//
                    intent.setDataAndType(uri, intentType);
                    context.startActivity(intent);
                }
            }
        };

        fileAdapter.setFilesListener(filesListener);
    }

    private void setAdapterToRecyclerView(){
        fileAdapter = new NotesFileAdapter(context, fileArraylist);
        recyclerViewFiles.setAdapter(fileAdapter);
    }

    private void addFileToFileArrayList(String fileUri, String fileName){
        NotesFile notesFile = new NotesFile(fileUri);
       // String[] fileNameParts = fileName.split(".");
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);//fileNameParts[fileNameParts.length - 1];
        notesFile.setFileName(fileName);
        notesFile.setExtension(fileExtension);
        fileArraylist.add(notesFile);
    }

    public void addFileUri(String fileUri, String fileName){
        fileUriString = fileUriString.concat(";" + fileUri + "<" + fileName);
        addFileToFileArrayList(fileUri, fileName);
        fileAdapter.notifyUpdateFilesList(fileArraylist);
    }

    public String getFileUriString() {
        return fileUriString;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof NotesFileAdapter.ViewHolder){
            NotesFile notesFile = fileArraylist.get(position);
            fileArraylist.remove(position);
            fileAdapter.notifyUpdateFilesList(fileArraylist);

            Snackbar snackbar = Snackbar
                    .make(rootView, "File deleted from note", Snackbar.LENGTH_LONG);
            snackbar.addCallback(new Snackbar.Callback(){
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    switch (event) {
                        case DISMISS_EVENT_TIMEOUT:
                        case DISMISS_EVENT_CONSECUTIVE:
                        case DISMISS_EVENT_MANUAL:
                            fileUriString = fileUriString.replace(notesFile.getUri() + "<" + notesFile.getFileName(), "");
                            fileUriString = fileUriString.replace(";;", ";");
                            fileUriString = fileUriString.replace("; ", "");
                            break;
                    }
                }
            });
            snackbar.setAction("UNDO", v -> {
                fileArraylist.add(position, notesFile);
                fileAdapter.notifyUpdateFilesList(fileArraylist);
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
