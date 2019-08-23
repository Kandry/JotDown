package com.kozyrev.jotdown_room.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;

import com.kozyrev.jotdown_room.Factory.ViewHolderFactory;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

public class NoteItemLookup extends ItemDetailsLookup {

    private final RecyclerView recyclerView;

    public NoteItemLookup(RecyclerView recyclerView) {this.recyclerView = recyclerView;}

    @Nullable
    @Override
    public ItemDetails getItemDetails(@NonNull MotionEvent event){
        View view = recyclerView.findChildViewUnder(event.getX(), event.getY());
        if (view != null){
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (viewHolder instanceof ViewHolderFactory.ImageViewHolder){
                return ((ViewHolderFactory.ImageViewHolder) viewHolder).getItemDetails();
            } else if (viewHolder instanceof ViewHolderFactory.TextViewHolder){
                return ((ViewHolderFactory.TextViewHolder) viewHolder).getItemDetails();
            }
        }
        return null;
    }
}
