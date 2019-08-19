package com.kozyrev.jotdown_room.RowTypes;

import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.kozyrev.jotdown_room.DB.Note;
import com.kozyrev.jotdown_room.Factory.ViewHolderFactory;

public class TextRowType implements RowType {

    private Note note;

    public TextRowType(Note note){
        this.note = note;
    }


    @Override
    public int getItemViewType() {
        return RowType.TEXT_ROW_TYPE;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        ViewHolderFactory.TextViewHolder textViewHolder = (ViewHolderFactory.TextViewHolder) viewHolder;

        textViewHolder.textView_title.setText(note.getName());
        textViewHolder.textView_description.setText(note.getDescription());
    }
}
