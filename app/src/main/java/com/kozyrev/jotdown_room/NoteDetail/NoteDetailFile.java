package com.kozyrev.jotdown_room.NoteDetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.kozyrev.jotdown_room.Adapter.DetailNotePagerAdapter;
import com.kozyrev.jotdown_room.Adapter.FileItemTouchHelper;
import com.kozyrev.jotdown_room.Adapter.NotesFileAdapter;
import com.kozyrev.jotdown_room.Entities.NotesFile;

import java.io.File;
import java.util.ArrayList;

public class NoteDetailFile implements FileItemTouchHelper.RecyclerItemTouchHelperListener {

    private Context context;
    private RecyclerView recyclerViewFiles;
    private View rootView;
    private DetailNotePagerAdapter detailNotePagerAdapter;

    private NotesFileAdapter fileAdapter;
    private ArrayList<NotesFile> fileArraylist;
    private NotesFileAdapter.FilesListener filesListener;

    private String fileUriString;

    public NoteDetailFile(Context context, View rootView, RecyclerView recyclerViewFiles, ArrayList<NotesFile> fileArraylist, String fileUriString, DetailNotePagerAdapter detailNotePagerAdapter){
        this.context = context;
        this.rootView = rootView;
        this.recyclerViewFiles = recyclerViewFiles;
        this.fileArraylist = fileArraylist;
        this.fileUriString = fileUriString;
        this.detailNotePagerAdapter = detailNotePagerAdapter;

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new FileItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewFiles);
    }

    public void fetchFiles(){
        if (!fileUriString.equals("")) {
            String[] filesUriAndName = fileUriString.split(";");
            for (String fileUriAndName : filesUriAndName) {
                if (!fileUriAndName.equals("")) {
                    String[] fileUriPath = fileUriAndName.split("<");
                    String fileUri = fileUriPath[0];
                    String filePath = fileUriPath[1];
                    if (!fileUri.equals("")) addFileToFileArrayList(fileUri, filePath);
                }
            }
        }
        setAdapterToRecyclerView();
        setListener();
    }

    private void setListener(){
        filesListener = new NotesFileAdapter.FilesListener() {
            @Override
            public void onClick(int position) {
                NotesFile notesFile = fileArraylist.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                File file = new File(notesFile.getFullPath());

                MimeTypeMap map = MimeTypeMap.getSingleton();
                String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
                String type = map.getMimeTypeFromExtension(ext);
                if (type == null) type = "*/*";

                Uri uri = FileProvider.getUriForFile(context, "com.kozyrev.jotdown_room.fileprovider", file);
                intent.setDataAndType(uri, type);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            }
        };

        fileAdapter.setFilesListener(filesListener);
    }

    private void setAdapterToRecyclerView(){
        fileAdapter = new NotesFileAdapter(context, fileArraylist);
        recyclerViewFiles.setAdapter(fileAdapter);
        if (fileArraylist.size() == 0) detailNotePagerAdapter.updatePagerAdapterFilesCount(fileArraylist.size());
    }

    private void addFileToFileArrayList(String fileUri, String filePath){
        NotesFile notesFile = new NotesFile(fileUri);
        notesFile.setFileName(getFileName(filePath));
        notesFile.setFullPath(filePath);
        fileArraylist.add(notesFile);
    }

    public void addFileUri(String fileUri, String filePath){
        fileUriString = fileUriString.concat(";" + fileUri + "<" + filePath);
        addFileToFileArrayList(fileUri, filePath);
        fileAdapter.notifyUpdateFilesList(fileArraylist);
        if (fileArraylist.size() == 1) detailNotePagerAdapter.updatePagerAdapterFilesCount(fileArraylist.size());
    }

    private String getFileName(String filePath){
        String fileName = "";
        try{
            fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return fileName;
    }

    public String getFileUriString() {
        return fileUriString;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof NotesFileAdapter.ViewHolder){
            NotesFile notesFile = fileArraylist.get(position);
            fileArraylist.remove(position);
            if (fileArraylist.size() < 1) detailNotePagerAdapter.updatePagerAdapterFilesCount(fileArraylist.size());
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
                if (fileArraylist.size() == 1) detailNotePagerAdapter.updatePagerAdapterFilesCount(fileArraylist.size());
                fileAdapter.notifyUpdateFilesList(fileArraylist);
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
