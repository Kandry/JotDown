package com.kozyrev.jotdown_room;

import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kozyrev.jotdown_room.DB.Note;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CaptionedImagesAdapter extends RecyclerView.Adapter<CaptionedImagesAdapter.ViewHolder> {

    private List<Note> notes = null;
    private Listener listener;
    private boolean isCard;

    public CaptionedImagesAdapter(List<Note> notes, boolean isCard){
        this.notes = notes;
        this.isCard = isCard;
    }


    public void updateNotesList(List<Note> notes, boolean isCard){
        if (this.notes != null) {
            this.notes.clear();
            this.notes.addAll(notes);
        } else {
            this.notes = notes;
        }
        this.isCard = isCard;
        this.notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount(){
        return notes == null ? 0 : notes.size();
    }

    @Override
    public CaptionedImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_captioned_image, parent, false);
        ViewHolder viewHolder = new ViewHolder(cardView) {};

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
    public void onBindViewHolder(ViewHolder holder, final int position){
        CardView cardView = holder.cardView;

        Note note = notes.get(position);

        textViewSetText(cardView, R.id.info_name, note.getName());
        textViewSetText(cardView, R.id.info_description, note.getDescription());

        ImageView imageView = (ImageView)cardView.findViewById(R.id.info_image);
        imageView.setImageURI(null);

        if (note.getImageResourceUri() != null && isCard) {
            imageView.getLayoutParams().height = (int) imageView.getResources().getDimension(R.dimen.imageview_height);
            Uri imageUri = Uri.parse(note.getImageResourceUri());
            Picasso.get()
                    .load(imageUri)
                    .resize(800, 450)
                    .centerCrop()
                    .into(imageView);
            imageView.setContentDescription(note.getName());
        } else {
            imageView.getLayoutParams().height = (int) imageView.getResources().getDimension(R.dimen.height_null);
        }
    }

    private void textViewSetText(CardView cardView, int textViewId, String text){
        TextView textView = (TextView)cardView.findViewById(textViewId);
        textView.setText(text);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;

        ViewHolder(CardView v){
            super(v);
            cardView = v;
        }
    }

    interface Listener{
        void onClick(int position);
        void onLongClick(int position);
    }
}
