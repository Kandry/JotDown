package com.kozyrev.jotdown_room.NoteDetail;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.kozyrev.jotdown_room.Adapter.RecordingAdapter;
import com.kozyrev.jotdown_room.Entities.Recording;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class NoteAudioRecord {

    private Context context;
    private RecyclerView recyclerViewRecordings;

    private MediaRecorder mediaRecorder;
    private RecordingAdapter recordingAdapter;
    private ArrayList<Recording> recordingArraylist;

    private int noteId;

    public NoteAudioRecord(Context context, RecyclerView recyclerViewRecordings, ArrayList<Recording> recordingArraylist, int noteId){
        this.context = context;
        this.recyclerViewRecordings = recyclerViewRecordings;
        this.recordingArraylist = recordingArraylist;
        this.noteId = noteId;
    }

    public void fetchRecordings(View view) {
        File root = getRoot();
        File[] files = getNoteRecordsDirectoryFiles(root);
        if (files != null){

            for (int i = 0; i < files.length; i++) {
                addRecordToRecordingArrayList(i, root, files);
            }
        }
        setAdapterToRecyclerView(view);
    }

    private void setAdapterToRecyclerView(View view){
        recordingAdapter = new RecordingAdapter(context,recordingArraylist);
        recyclerViewRecordings.setAdapter(recordingAdapter);
        recordingAdapter.setRecordsListener(position -> {

            Recording recording = recordingArraylist.get(position);

            recordingArraylist.remove(position);
            recordingAdapter.notifyUpdateRecordsList(recordingArraylist);

            Snackbar snackbar = Snackbar
                    .make(view, "Record deleted", Snackbar.LENGTH_LONG);
            snackbar.addCallback(new Snackbar.Callback(){
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    switch(event) {
                        case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                            File root = getRoot();
                            File[] files = getNoteRecordsDirectoryFiles(root);

                            File recordFile = new File(files[position].getAbsolutePath());
                            recordFile.delete();
                            break;
                    }
                }
            });
            snackbar.setAction("UNDO", v -> {
                recordingArraylist.add(position, recording);
                recordingAdapter.notifyUpdateRecordsList(recordingArraylist);
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        });
    }

    public void startRecording(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        File root = getRoot();
        File file = new File(root.getAbsolutePath() + "/VoiceRecords/Note" + noteId);
        if (!file.exists()){
            file.mkdirs();
        }

        String audioRecordFileName = root.getAbsolutePath() + "/VoiceRecords/Note" + noteId + "/" + String.valueOf(System.currentTimeMillis() + ".mp3");
        mediaRecorder.setOutputFile(audioRecordFileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        if (recordingAdapter != null) recordingAdapter.notifyStopPlaying();

        try{
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void stopRecording(){
        try {
            mediaRecorder.stop();
            mediaRecorder.release();

            File root = getRoot();
            File[] files = getNoteRecordsDirectoryFiles(root);
            addRecordToRecordingArrayList(files.length - 1, root, files);

            recordingAdapter.notifyUpdateRecordsList(recordingArraylist);
            Toast.makeText(context, "Record added", Toast.LENGTH_SHORT).show();

        } catch(Exception ex){
            ex.printStackTrace();
        }

        mediaRecorder = null;
    }

    private File getRoot(){
        return android.os.Environment.getExternalStorageDirectory();
    }

    private File[] getNoteRecordsDirectoryFiles(File root){
        String path = root.getAbsolutePath() + "/VoiceRecords/Note" + noteId;
        File directory = new File(path);
        return directory.listFiles();
    }

    private void addRecordToRecordingArrayList(int i, File root, File[] files){
        String fileName = files[i].getName();
        String recordingUri = root.getAbsolutePath() + "/VoiceRecords/Note" + noteId + "/" + fileName;
        Recording recording = new Recording(recordingUri, fileName, false);
        recordingArraylist.add(recording);
    }
}