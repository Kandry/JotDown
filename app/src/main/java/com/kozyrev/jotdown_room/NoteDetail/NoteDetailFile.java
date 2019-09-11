package com.kozyrev.jotdown_room.NoteDetail;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.kozyrev.jotdown_room.Adapter.FileItemTouchHelper;
import com.kozyrev.jotdown_room.Adapter.NotesFileAdapter;
import com.kozyrev.jotdown_room.BuildConfig;
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
    private PackageManager packageManager;

    public NoteDetailFile(Context context, View rootView, RecyclerView recyclerViewFiles, ArrayList<NotesFile> fileArraylist, String fileUriString, PackageManager packageManager){
        this.context = context;
        this.rootView = rootView;
        this.recyclerViewFiles = recyclerViewFiles;
        this.fileArraylist = fileArraylist;
        this.fileUriString = fileUriString;
        this.packageManager = packageManager;

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

    public void setListener(){
        filesListener = new NotesFileAdapter.FilesListener() {
            @Override
            public void onClick(int position) {
                NotesFile notesFile = fileArraylist.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String extension = notesFile.getExtension();
                switch (extension){
                    case ("jpg"):
                        extension = "jpeg";
                        break;
                    case ("doc"):
                    case ("docs"):
                        extension = "msword";
                        break;
                }

                String intentType = "application/" + extension;
                intent.setType(intentType);
                List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                File file = new File(notesFile.getFullPath());

                if (list.size() > 0 && file.isFile()){
                    Uri uri = FileProvider.getUriForFile(context, "com.kozyrev.jotdown_room.fileprovider", file);
                    intent.setDataAndType(uri, intentType);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Intent chooser = Intent.createChooser(intent, "Open file");
                    context.startActivity(chooser);
                }
            }
        };

        fileAdapter.setFilesListener(filesListener);
    }

    private void setAdapterToRecyclerView(){
        fileAdapter = new NotesFileAdapter(context, fileArraylist);
        recyclerViewFiles.setAdapter(fileAdapter);
    }

    private void addFileToFileArrayList(String fileUri, String filePath){
        NotesFile notesFile = new NotesFile(fileUri);
        notesFile.setFileName(getFileName(filePath));
        notesFile.setExtension(getFileExtension(filePath));
        notesFile.setFullPath(filePath);
        fileArraylist.add(notesFile);
    }

    public void addFileUri(String fileUri, String filePath){
        fileUriString = fileUriString.concat(";" + fileUri + "<" + filePath);
        addFileToFileArrayList(fileUri, filePath);
        fileAdapter.notifyUpdateFilesList(fileArraylist);
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

    private String getFileExtension(String filePath){
        String extension = "";
        try{
            extension = filePath.substring(filePath.lastIndexOf(".") + 1);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return  extension;
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
