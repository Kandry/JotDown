package com.kozyrev.jotdown_room.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kozyrev.jotdown_room.Entities.NotesFile;
import com.kozyrev.jotdown_room.R;

import java.util.ArrayList;

public class NotesFileAdapter extends RecyclerView.Adapter<NotesFileAdapter.ViewHolder>{

    private Context context;
    private ArrayList<NotesFile> fileArrayList;
    private FilesListener filesListener;

    public NotesFileAdapter(Context context, ArrayList<NotesFile> fileArrayList){
        this.context = context;
        this.fileArrayList = fileArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_item_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        view.setOnClickListener(v -> {
            int adapterPosition = viewHolder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && filesListener != null) {
                filesListener.onClick(adapterPosition);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotesFile notesFile = fileArrayList.get(position);
        holder.textViewName.setText(notesFile.getFileName());
    }

    @Override
    public int getItemCount() {
        return fileArrayList == null ? 0 : fileArrayList.size();
    }

    public void notifyUpdateFilesList(ArrayList<NotesFile> fileArrayList){
        this.fileArrayList = fileArrayList;
        this.notifyDataSetChanged();
    }

    public void setFilesListener(FilesListener filesListener){
        this.filesListener = filesListener;
    }

    public interface FilesListener{
        void onClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private RelativeLayout backgroundLayout;
        public RelativeLayout foregroundLayout;

        ImageView imageViewFile;
        TextView textViewName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.view_background_file);
            foregroundLayout = itemView.findViewById(R.id.view_foreground_file);

            imageViewFile = itemView.findViewById(R.id.imageViewFile);
            textViewName = itemView.findViewById(R.id.textViewFileName);
        }
    }
}
