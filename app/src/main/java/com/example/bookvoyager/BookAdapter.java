package com.example.bookvoyager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> books;

    public BookAdapter(List<Book> books) {
        this.books = books;
    }

    public void FilterList(List<Book> filteredList){
        books = filteredList;
        notifyDataSetChanged();
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bookTitle.setText(book.getTitle());
        holder.bookAuthor.setText(book.getAuthor());
        String coverUrl = book.getCoverUrl();

        if (coverUrl != null && (coverUrl.startsWith("http://") || coverUrl.startsWith("https://"))) {
            // Завантаження зображення з інтернету
            Picasso.get()
                    .load(coverUrl)
                    .placeholder(R.drawable.agata) // тимчасове зображення
                    .error(R.drawable.agata)         // якщо помилка
                    .into(holder.bookCover);
        } else if (coverUrl != null && !coverUrl.isEmpty()) {
            // Завантаження локального ресурсу
            int resId = holder.itemView.getContext().getResources()
                    .getIdentifier(coverUrl, "drawable", holder.itemView.getContext().getPackageName());

            if (resId != 0) {
                holder.bookCover.setImageResource(resId);
            } else {
                holder.bookCover.setImageResource(R.drawable.agata); // запасне зображення
            }
        }
        else {
            holder.bookCover.setImageResource(R.drawable.agata);
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
}