package com.example.meltingbooks.calendar.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;

import java.util.List;

public class BookListHelper {

    public static class BookItem {
        private String imageUrl; // URL에서 불러올 경우
        private int imageResId;  // 리소스에서 불러올 경우
        public boolean isRead;
        public boolean isSelected = false;

        public BookItem(String imageUrl, boolean isSelected) {
            this.imageUrl = imageUrl;
            this.isSelected = isSelected;
        }

        public BookItem(int imageResId, boolean isSelected) {
            this.imageResId = imageResId;
            this.isSelected = isSelected;
        }

        public BookItem(int imageResId, boolean isRead, boolean isSelected) {
            this.imageResId = imageResId;
            this.isRead = isRead;
            this.isSelected = isSelected;
        }

        public BookItem(String imageUrl, boolean isRead, boolean isSelected) {
            this.imageUrl = imageUrl;
            this.isRead = isRead;
            this.isSelected = isSelected;
        }
    }

    public static void setupBooks(Context context, ViewGroup container, List<BookItem> bookItems, boolean showOverlayAndSelection) {
        container.removeAllViews();

        for (BookItem book : bookItems) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_book, container, false);

            ImageView bookImage = itemView.findViewById(R.id.book_image);
            View selectionBorder = itemView.findViewById(R.id.selection_border);
            View overlayRead = itemView.findViewById(R.id.overlay_read);
            ImageView awardIcon = itemView.findViewById(R.id.award_icon);

            //bookImage.setImageResource(book.imageResId);
            if (book.imageUrl != null) {
                Glide.with(context)
                        .load(book.imageUrl)
                        .placeholder(R.drawable.book_image) // 책 기본 이미지
                        .into(bookImage);
            } else {
                bookImage.setImageResource(book.imageResId);
            }

            if (book.isRead) {
                awardIcon.setVisibility(View.VISIBLE);
                overlayRead.setVisibility(View.GONE);
            } else {
                awardIcon.setVisibility(View.GONE);
                overlayRead.setVisibility(View.GONE);
            }

            if (showOverlayAndSelection) {
                itemView.setOnClickListener(v -> {
                    book.isSelected = !book.isSelected;
                    selectionBorder.setVisibility(book.isSelected ? View.VISIBLE : View.GONE);
                });
            } else {
                selectionBorder.setVisibility(View.GONE);
            }

            container.addView(itemView);
        }
    }
}