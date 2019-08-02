package com.kozyrev.jotdown_room;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kozyrev.jotdown_room.DB.Note;

import java.util.List;

public class CaptionedImagesAdapter extends RecyclerView.Adapter<CaptionedImagesAdapter.ViewHolder> {

    private List<Note> notes = null;
    private Listener listener;
    private boolean isCard;

    public CaptionedImagesAdapter(List<Note> notes, boolean isCard){
        this.notes = notes;
        this.isCard = isCard;
    }

    public void updateNotesList(List<Note> notes){
        if (this.notes != null) {
            this.notes.clear();
            this.notes.addAll(notes);
        } else {
            this.notes = notes;
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount(){
        return notes == null ? 0 : notes.size();
    }

    @Override
    public CaptionedImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_captioned_image, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        CardView cardView = holder.cardView;
        Note note = notes.get(position);

        textViewSetText(cardView, R.id.info_name, note.getName());
        textViewSetText(cardView, R.id.info_description, note.getDescription());

        ImageView imageView = (ImageView)cardView.findViewById(R.id.info_image);

        if (note.getImageResourceUri() != null && isCard) {
            imageView.getLayoutParams().height = (int) imageView.getResources().getDimension(R.dimen.imageview_height);
            Uri imageUri = Uri.parse(note.getImageResourceUri());
            imageView.setImageURI(imageUri);
            imageView.setContentDescription(note.getName());
        } else {
            imageView.setImageURI(null);
            imageView.getLayoutParams().height = (int) imageView.getResources().getDimension(R.dimen.imageview_height_null);
        }

        cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(listener != null){
                    listener.onClick(position);
                }
            }
        });

        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(listener != null){
                    listener.onLongClick(position);
                }
                return true;
            }
        });
    }

    private void textViewSetText(CardView cardView, int textViewId, String text){
        TextView textView = (TextView)cardView.findViewById(textViewId);
        textView.setText(text);
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;

        public ViewHolder(CardView v){
            super(v);
            cardView = v;
        }
    }

    interface Listener{
        void onClick(int position);
        void onLongClick(int position);
    }
}
