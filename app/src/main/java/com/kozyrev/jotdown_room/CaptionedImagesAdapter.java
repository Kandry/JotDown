package com.kozyrev.jotdown_room;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.kozyrev.jotdown_room.Factory.ViewHolderFactory;
import com.kozyrev.jotdown_room.RowTypes.RowType;

import java.util.List;

public class CaptionedImagesAdapter extends RecyclerView.Adapter {

    private Listener listener;
    private List<RowType> dataSet;

    public CaptionedImagesAdapter(List<RowType> dataSet){
        this.dataSet = dataSet;
    }

    @Override
    public int getItemViewType(int position){
        return dataSet.get(position).getItemViewType();
    }

    @Override
    public int getItemCount(){
        return dataSet == null ? 0 : dataSet.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        RecyclerView.ViewHolder viewHolder = ViewHolderFactory.create(parent, viewType);
        CardView cardView = (CardView) viewHolder.itemView;

        cardView.setOnClickListener((v) -> {
            int adapterPosition = viewHolder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onClick(adapterPosition);
            }
        });

        cardView.setOnLongClickListener(v -> {
            int adapterPosition = viewHolder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onLongClick(adapterPosition);
            }
            return true;
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position){
        dataSet.get(position).onBindViewHolder(holder);
    }

    public void updateNotesList(List<RowType> dataSet){
        this.dataSet = dataSet;
        this.notifyDataSetChanged();
    }

    void setListener(Listener listener){
        this.listener = listener;
    }

    interface Listener{
        void onClick(int position);
        void onLongClick(int position);
    }
}