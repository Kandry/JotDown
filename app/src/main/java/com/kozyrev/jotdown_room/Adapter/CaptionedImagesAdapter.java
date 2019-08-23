package com.kozyrev.jotdown_room.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import androidx.recyclerview.selection.SelectionTracker;

import com.kozyrev.jotdown_room.Factory.ViewHolderFactory;
import com.kozyrev.jotdown_room.RowTypes.RowType;

import java.util.ArrayList;
import java.util.List;

public class CaptionedImagesAdapter extends RecyclerView.Adapter<AppViewHolder> {

    private Listener listener;
    private List<RowType> dataSet;
    private SelectionTracker selectionTracker;

    public List<Integer> positions = new ArrayList<>();

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
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        AppViewHolder viewHolder = ViewHolderFactory.create(parent, viewType, dataSet);
        CardView cardView = (CardView) viewHolder.itemView;

        cardView.setOnClickListener((v) -> {
            int adapterPosition = viewHolder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onClick(adapterPosition);
            }
        });

        /*cardView.setOnLongClickListener(v -> {
            int adapterPosition = viewHolder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onLongClick(adapterPosition);
            }
            return true;
        });*/

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position){
        RowType item = dataSet.get(position);
        boolean isSelected = selectionTracker.isSelected(item);
        holder.bind(isSelected);
        if (isSelected) {positions.add(position);}
        item.onBindViewHolder(holder);
    }

    public void updateNotesList(List<RowType> dataSet){
        positions.clear();
        this.dataSet = dataSet;
        this.notifyDataSetChanged();
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public void setSelectionTracker(SelectionTracker selectionTracker){
        this.selectionTracker = selectionTracker;
    }

    public interface Listener{
        void onClick(int position);
       // void onLongClick(int position);
    }
}