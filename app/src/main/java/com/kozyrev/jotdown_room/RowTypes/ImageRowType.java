package com.kozyrev.jotdown_room.RowTypes;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.kozyrev.jotdown_room.DB.Note;
import com.kozyrev.jotdown_room.Factory.ViewHolderFactory;
import com.squareup.picasso.Picasso;

public class ImageRowType implements RowType {

    private Note note;

    public ImageRowType(Note note){
        this.note = note;
    }

    @Override
    public int getItemViewType() {
        return RowType.IMAGE_ROW_TYPE;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        ViewHolderFactory.ImageViewHolder imageViewHolder = (ViewHolderFactory.ImageViewHolder) viewHolder;

        imageViewHolder.textView_title.setText(note.getName());
        imageViewHolder.textView_description.setText(note.getDescription());

        ImageView imageView = imageViewHolder.imageView;
        Uri imageUri = Uri.parse(note.getImageResourceUri());
        Picasso.get()
                .load(imageUri)
                .resize(800, 450)
                .centerCrop()
                .into(imageView);
        imageView.setContentDescription(note.getName());
    }
}
