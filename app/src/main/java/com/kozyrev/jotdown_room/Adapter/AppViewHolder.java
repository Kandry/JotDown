package com.kozyrev.jotdown_room.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class AppViewHolder extends RecyclerView.ViewHolder {

    public AppViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public final void bind(boolean isActive){
        itemView.setActivated(isActive);
    }
}
