package com.kozyrev.jotdown_room.RowTypes;

import android.support.v7.widget.RecyclerView;

public interface RowType {
    int IMAGE_ROW_TYPE = 300;
    int TEXT_ROW_TYPE = 301;

    int getItemViewType();

    void onBindViewHolder(RecyclerView.ViewHolder viewHolder);
}
