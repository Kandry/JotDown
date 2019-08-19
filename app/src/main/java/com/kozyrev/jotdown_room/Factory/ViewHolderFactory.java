package com.kozyrev.jotdown_room.Factory;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kozyrev.jotdown_room.R;
import com.kozyrev.jotdown_room.RowTypes.RowType;

public class ViewHolderFactory {

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView textView_title;
        public TextView textView_description;

        ImageViewHolder(CardView itemView){
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.info_image_imageCard);
            textView_title = (TextView) itemView.findViewById(R.id.info_name_imageCard);
            textView_description = (TextView) itemView.findViewById(R.id.info_description_imageCard);
        }
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder {

        public TextView textView_title;
        public TextView textView_description;

        TextViewHolder(CardView itemView){
            super(itemView);
            textView_title = (TextView) itemView.findViewById(R.id.info_name_textCard);
            textView_description = (TextView) itemView.findViewById(R.id.info_description_textCard);
        }
    }

    public static RecyclerView.ViewHolder create(ViewGroup parent, int viewType){
        switch (viewType){
            case RowType.IMAGE_ROW_TYPE:
                CardView imageTypeView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_captioned_image, parent, false);
                return new ViewHolderFactory.ImageViewHolder(imageTypeView);

            case RowType.TEXT_ROW_TYPE:
                CardView textTypeView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_captioned_text, parent, false);
                return new ViewHolderFactory.TextViewHolder(textTypeView);

            default:
                return null;
        }
    }
}
