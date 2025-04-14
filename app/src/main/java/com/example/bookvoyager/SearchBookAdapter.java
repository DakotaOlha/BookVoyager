package com.example.bookvoyager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.BookViewHolder> {
    private List<Book> books;
    private final OnBookClickListener clickListener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public SearchBookAdapter(List<Book> books, OnBookClickListener clickListener) {
        this.books = books;
        this.clickListener = clickListener;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle, bookAuthor;

        public BookViewHolder(View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.bookCover);
            bookTitle = itemView.findViewById(R.id.bookTitle);
            bookAuthor = itemView.findViewById(R.id.bookAuthor);
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item_book, parent, false);
        return new BookViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bookTitle.setText(book.getTitle());
        holder.bookAuthor.setText(book.getAuthor());

        holder.itemView.setOnClickListener(v -> clickListener.onBookClick(book));

        String coverUrl = book.getCoverUrl();
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Picasso.get()
                    .load(coverUrl)
                    .placeholder(R.drawable.agata)
                    .error(R.drawable.agata)
                    .into(holder.bookCover, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("Picasso", "Зображення завантажено");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("Picasso", "Помилка завантаження: " + e.getMessage());
                        }
                    });
        } else {
            holder.bookCover.setImageResource(R.drawable.agata);
        }
    }


    @Override
    public int getItemCount() {
        return books.size();
    }
}
