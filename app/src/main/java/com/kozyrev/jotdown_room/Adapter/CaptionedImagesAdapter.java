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
        return ViewHolderFactory.create(parent, viewType, dataSet);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position){
        RowType item = dataSet.get(position);
        boolean isSelected = selectionTracker.isSelected(item);
        holder.bind(isSelected);
        if (isSelected) {positions.add(position);}
        item.onBindViewHolder(holder);
    }

    public void updateNoteItem(int position){
        this.notifyItemChanged(position);
    }

    public void updateNotesList(List<RowType> dataSet){
        positions.clear();
        this.dataSet = dataSet;
        this.notifyDataSetChanged();
    }

    public void setSelectionTracker(SelectionTracker selectionTracker){
        this.selectionTracker = selectionTracker;
    }
}