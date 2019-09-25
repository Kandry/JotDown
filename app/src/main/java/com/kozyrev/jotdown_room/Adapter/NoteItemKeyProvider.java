package com.kozyrev.jotdown_room.Adapter;

import androidx.recyclerview.selection.ItemKeyProvider;

import com.kozyrev.jotdown_room.RowTypes.RowType;

import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

public class NoteItemKeyProvider extends ItemKeyProvider {
    private final List<RowType> itemList;

    public NoteItemKeyProvider(int scope, List<RowType> itemList){
        super(scope);
        this.itemList = itemList;
    }

    @Nullable
    @Override
    public Object getKey(int position) {return itemList.get(position);}

    @Override
    public int getPosition(@android.support.annotation.NonNull @NonNull Object key) {return itemList.indexOf(key);}
}
