package com.kozyrev.jotdown_room.NoteDetail;

import android.content.Context;
import android.media.MediaRecorder;
import android.support.v7.widget.RecyclerView;
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

    public NoteAudioRecord(Context context, RecyclerView recyclerViewRecordings, MediaRecorder mediaRecorder, ArrayList<Recording> recordingArraylist, int noteId){
        this.context = context;
        this.recyclerViewRecordings = recyclerViewRecordings;
        this.mediaRecorder = mediaRecorder;
        this.recordingArraylist = recordingArraylist;
        this.noteId = noteId;
    }

    public void fetchRecordings() {
        File root = getRoot();
        File[] files = getNoteRecordsDirectoryFiles(root);
        if (files != null){

            for (int i = 0; i < files.length; i++) {
                addRecordToRecordingArrayList(i, root, files);
            }
        }
        setAdapterToRecyclerView();
    }

    private void setAdapterToRecyclerView(){
        recordingAdapter = new RecordingAdapter(context,recordingArraylist);
        recyclerViewRecordings.setAdapter(recordingAdapter);
        recordingAdapter.setRecordsListener(new RecordingAdapter.RecordsListener() {
            @Override
            public void onLongClick(int position) {
                File root = getRoot();
                File[] files = getNoteRecordsDirectoryFiles(root);

                File recordFile = new File(files[position].getAbsolutePath());
                recordFile.delete();

                recordingArraylist.remove(position);
                recordingAdapter.notifyUpdateRecordsList(recordingArraylist);
                Toast.makeText(context, "Record deleted", Toast.LENGTH_SHORT).show();
            }
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

    public File getRoot(){
        return android.os.Environment.getExternalStorageDirectory();
    }

    public File[] getNoteRecordsDirectoryFiles(File root){
        String path = root.getAbsolutePath() + "/VoiceRecords/Note" + noteId;
        File directory = new File(path);
        return directory.listFiles();
    }

    public void addRecordToRecordingArrayList(int i, File root, File[] files){
        String fileName = files[i].getName();
        String recordingUri = root.getAbsolutePath() + "/VoiceRecords/Note" + noteId + "/" + fileName;
        Recording recording = new Recording(recordingUri, fileName, false);
        recordingArraylist.add(recording);
    }
}
