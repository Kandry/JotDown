package com.kozyrev.jotdown_room.Adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kozyrev.jotdown_room.Entities.Recording;
import com.kozyrev.jotdown_room.R;

import java.io.IOException;
import java.util.ArrayList;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {

    private Context context;
    private MediaPlayer mediaPlayer;

    private ArrayList<Recording> recordingArrayList;

    private boolean isPlaying = false;
    private boolean isHaveToStop = false;
    private int last_index = -1;

    public void notifyUpdateRecordsList(ArrayList<Recording> recordingArrayList){
        this.recordingArrayList = recordingArrayList;
        this.notifyDataSetChanged();
    }

    public void notifyStopPlaying(){
        isHaveToStop = true;
        this.notifyDataSetChanged();
    }

    public RecordingAdapter(Context context, ArrayList<Recording> recordingArrayList){
        this.context = context;
        this.recordingArrayList = recordingArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recording_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        setUpData(holder, position);
        isHaveToStop = false;
    }

    @Override
    public int getItemCount() {
        return recordingArrayList == null ? 0 : recordingArrayList.size();
    }

    private void setUpData(ViewHolder holder, int position){
        Recording recording = recordingArrayList.get(position);
        holder.textViewName.setText(recording.getFileName());

        if(recording.isPlaying() && !isHaveToStop){
            holder.imageViewPlay.setImageResource(R.drawable.ic_add_white_24dp);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.seekUpdation(holder);
        } else {
            holder.imageViewPlay.setImageResource(R.drawable.ic_done_white_24dp);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            holder.seekBar.setVisibility(View.GONE);
        }

        holder.manageSeekBar(holder);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private RelativeLayout backgroundLayout;
        public RelativeLayout foregroundLayout;

        ImageView imageViewPlay;
        SeekBar seekBar;
        TextView textViewName;

        ViewHolder holder;
        private Handler handler = new Handler();

        private String recordingUri;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.view_background);
            foregroundLayout = itemView.findViewById(R.id.view_foreground);

            imageViewPlay = itemView.findViewById(R.id.imageViewPlay);
            seekBar = itemView.findViewById(R.id.seekBar);

            textViewName = itemView.findViewById(R.id.textViewRecordingName);

            imageViewPlay.setOnClickListener(view -> {
                int position = getAdapterPosition();
                Recording recording = recordingArrayList.get(position);

                recordingUri = recording.getUri();

                if(isPlaying){
                    stopPlaying();
                    if(position == last_index){
                        recording.setPlaying(false);
                        stopPlaying();
                        notifyItemChanged(position);
                    } else {
                        markAllPaused();
                        recording.setPlaying(true);
                        notifyItemChanged(position);
                        startPlaying(recording, position);
                        last_index = position;
                    }
                } else {
                    startPlaying(recording, position);
                    recording.setPlaying(true);
                    seekBar.setMax(mediaPlayer.getDuration());
                    notifyItemChanged(position);
                    last_index = position;
                }
            });
        }

        void manageSeekBar(ViewHolder holder){
            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(mediaPlayer != null && fromUser){
                        mediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        private void markAllPaused() {
            for (Recording record : recordingArrayList) {
                record.setPlaying(false);
            }
            notifyDataSetChanged();
        }

        Runnable runnable = (() -> seekUpdation(holder));

        private void seekUpdation(ViewHolder holder){
            this.holder = holder;
            if(mediaPlayer != null){
                int currentPosition = mediaPlayer.getCurrentPosition();
                holder.seekBar.setMax(mediaPlayer.getDuration());
                holder.seekBar.setProgress(currentPosition);
            }
            handler.postDelayed(runnable, 100);
        }

        private void stopPlaying(){
            try{
                mediaPlayer.release();
            } catch (Exception ex){
                ex.printStackTrace();
            }
            mediaPlayer = null;
            isPlaying = false;
        }

        private void startPlaying(Recording audioRec, int position){
            mediaPlayer = new MediaPlayer();
            try{
                mediaPlayer.setDataSource(recordingUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException ex){
                ex.printStackTrace();
                ex.fillInStackTrace();
            }

            seekBar.setMax(mediaPlayer.getDuration());
            isPlaying = true;

            mediaPlayer.setOnCompletionListener((mp) -> {
                audioRec.setPlaying(false);
                notifyItemChanged(position);
            });
        }
    }
}