package com.kozyrev.jotdown_room.NoteDetail;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import com.kozyrev.jotdown_room.Adapter.DetailNotePagerAdapter;
import com.kozyrev.jotdown_room.Adapter.RecordItemTouchHelper;
import com.kozyrev.jotdown_room.Adapter.RecordingAdapter;
import com.kozyrev.jotdown_room.Entities.Recording;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class NoteAudioRecord implements RecordItemTouchHelper.RecyclerItemTouchHelperListener {

    private Context context;
    private RecyclerView recyclerViewRecordings;
    private View rootView;
    private DetailNotePagerAdapter detailNotePagerAdapter;

    private MediaRecorder mediaRecorder;
    private RecordingAdapter recordingAdapter;
    private ArrayList<Recording> recordingArraylist;

    private int noteId;
    private String fullPath, fileName;

    public NoteAudioRecord(Context context, View rootView, RecyclerView recyclerViewRecordings, ArrayList<Recording> recordingArraylist, int noteId, DetailNotePagerAdapter detailNotePagerAdapter){
        this.context = context;
        this.rootView = rootView;
        this.recyclerViewRecordings = recyclerViewRecordings;
        this.recordingArraylist = recordingArraylist;
        this.noteId = noteId;
        this.detailNotePagerAdapter = detailNotePagerAdapter;

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecordItemTouchHelper(0, ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewRecordings);
    }

    public void fetchRecordings() {
        File root = getRoot();
        File[] files = getNoteRecordsDirectoryFiles(root);
        if (files != null){
            Arrays.sort(files, (file1, file2) -> file1.toString().compareTo(file2.toString()));

            for (int i = 0; i < files.length; i++) {
                addRecordToRecordingArrayList(i, root, files);
            }
        }
        setAdapterToRecyclerView();
    }

    private void setAdapterToRecyclerView(){
        recordingAdapter = new RecordingAdapter(context, recordingArraylist);
        recyclerViewRecordings.setAdapter(recordingAdapter);
        if (recordingArraylist.size() == 0) detailNotePagerAdapter.updatePagerAdapterRecordsCount(recordingArraylist.size());
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
        Calendar calendar = Calendar.getInstance();
        Date date = Calendar.getInstance().getTime();
        fileName = String.valueOf(calendar.get(Calendar.YEAR) + "" +
                calendar.get(Calendar.MONTH) + "" +
                calendar.get(Calendar.DAY_OF_MONTH) + "_" +
                calendar.get(Calendar.HOUR_OF_DAY) + "" +
                calendar.get(Calendar.MINUTE) + "" +
                calendar.get(Calendar.SECOND) +
                ".mp3");
        fullPath = root.getAbsolutePath() + "/VoiceRecords/Note" + noteId + "/" + fileName;
        mediaRecorder.setOutputFile(fullPath);
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

            addRecordToRecordingArrayList(fullPath, fileName);
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

    private void addRecordToRecordingArrayList(String fullPath, String fileName){
        Recording recording = new Recording(fullPath, fileName, false);
        recordingArraylist.add(recording);
        if (recordingArraylist.size() == 1) detailNotePagerAdapter.updatePagerAdapterRecordsCount(recordingArraylist.size());
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RecordingAdapter.ViewHolder){
            Recording recording = recordingArraylist.get(position);

            recordingArraylist.remove(position);
            recordingAdapter.notifyUpdateRecordsList(recordingArraylist);
            if (recordingArraylist.size() < 1) detailNotePagerAdapter.updatePagerAdapterRecordsCount(recordingArraylist.size());

            Snackbar snackbar = Snackbar
                    .make(rootView, "Record deleted", Snackbar.LENGTH_LONG);
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    switch (event) {
                        case DISMISS_EVENT_TIMEOUT:
                        case DISMISS_EVENT_CONSECUTIVE:
                        case DISMISS_EVENT_MANUAL:
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
                if (recordingArraylist.size() == 1) detailNotePagerAdapter.updatePagerAdapterRecordsCount(recordingArraylist.size());
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}