package com.example.meltingbooks.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.R;

import java.util.List;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public SearchBookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookInfoTitle, bookInfoAuthor, bookInfoPublisher, bookInfoCategory;

        public BookViewHolder(View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.bookCover);
            bookInfoTitle = itemView.findViewById(R.id.bookInfoTitle);
            bookInfoAuthor = itemView.findViewById(R.id.bookInfoAuthor);
            bookInfoPublisher = itemView.findViewById(R.id.bookInfoPublisher);
            bookInfoCategory = itemView.findViewById(R.id.bookInfoCategory);
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_book_list, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.bookInfoTitle.setText(book.getTitle());
        holder.bookInfoAuthor.setText(book.getAuthor());
        holder.bookInfoPublisher.setText(book.getPublisher());
        holder.bookInfoCategory.setText(book.getCategoryName());

        Glide.with(context)
                .load(book.getCover())
                .into(holder.bookCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(book);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }
}

