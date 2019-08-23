package com.kozyrev.jotdown_room.Factory;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.selection.ItemDetailsLookup;

import com.kozyrev.jotdown_room.Adapter.AppViewHolder;
import com.kozyrev.jotdown_room.Adapter.NoteItemDetail;
import com.kozyrev.jotdown_room.Adapter.ViewHolderWithDetails;
import com.kozyrev.jotdown_room.R;
import com.kozyrev.jotdown_room.RowTypes.RowType;

import java.util.List;

public class ViewHolderFactory {

    private static List<RowType> factoryDataSet;

    public static class ImageViewHolder extends AppViewHolder implements ViewHolderWithDetails {

        public ImageView imageView;
        public TextView textView_title;
        public TextView textView_description;

        ImageViewHolder(CardView itemView){
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.info_image_imageCard);
            textView_title = (TextView) itemView.findViewById(R.id.info_name_imageCard);
            textView_description = (TextView) itemView.findViewById(R.id.info_description_imageCard);
        }

        @Override
        public ItemDetailsLookup.ItemDetails getItemDetails(){
            return new NoteItemDetail(getAdapterPosition(), factoryDataSet.get(getAdapterPosition()));
        }
    }

    public static class TextViewHolder extends AppViewHolder implements ViewHolderWithDetails {

        public TextView textView_title;
        public TextView textView_description;

        TextViewHolder(CardView itemView){
            super(itemView);
            textView_title = (TextView) itemView.findViewById(R.id.info_name_textCard);
            textView_description = (TextView) itemView.findViewById(R.id.info_description_textCard);
        }

        @Override
        public ItemDetailsLookup.ItemDetails getItemDetails(){
            return new NoteItemDetail(getAdapterPosition(), factoryDataSet.get(getAdapterPosition()));
        }
    }

    public static AppViewHolder create(ViewGroup parent, int viewType, List<RowType> dataSet){

        factoryDataSet = dataSet;

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
