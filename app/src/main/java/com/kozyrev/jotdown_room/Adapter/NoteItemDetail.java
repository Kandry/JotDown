package com.kozyrev.jotdown_room.Adapter;

import androidx.recyclerview.selection.ItemDetailsLookup;

import com.kozyrev.jotdown_room.RowTypes.RowType;

import io.reactivex.annotations.Nullable;

public class NoteItemDetail extends ItemDetailsLookup.ItemDetails {
    private final int adapterPosition;
    private final RowType selectionKey;

    public NoteItemDetail(int adapterPosition, RowType selectionKey){
        this.adapterPosition = adapterPosition;
        this.selectionKey = selectionKey;
    }

    @Override
    public int getPosition(){
        return adapterPosition;
    }

    @Nullable
    @Override
    public Object getSelectionKey(){
        return selectionKey;
    }
}
