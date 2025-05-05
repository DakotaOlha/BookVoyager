package com.example.bookvoyager.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookvoyager.Class.Book;
import com.example.bookvoyager.Fragment.BookTimerFragment;
import com.example.bookvoyager.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> books;
    private FragmentManager fragmentManager;

    public interface OnBookMenuClickListener {
        void onBookMenuClick(Book book, View anchorView);
    }

    private OnBookMenuClickListener menuClickListener;

    public void setOnBookMenuClickListener(OnBookMenuClickListener listener) {
        this.menuClickListener = listener;
    }

    public BookAdapter(List<Book> books, FragmentManager fragmentManager) {
        this.books = books;
        this.fragmentManager = fragmentManager;
    }

    public void FilterList(List<Book> filteredList){
        books = filteredList;
        notifyDataSetChanged();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover, bookMenu;
        TextView bookTitle, bookAuthor;

        public BookViewHolder(View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.bookCover);
            bookTitle = itemView.findViewById(R.id.bookTitle);
            bookAuthor = itemView.findViewById(R.id.bookAuthor);
            bookMenu = itemView.findViewById(R.id.bookMenu);
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
            Picasso.get()
                    .load(coverUrl)
                    .placeholder(R.drawable.img_none_cover)
                    .error(R.drawable.img_none_cover)
                    .into(holder.bookCover);
        } else if (coverUrl != null && !coverUrl.isEmpty()) {
            int resId = holder.itemView.getContext().getResources()
                    .getIdentifier(coverUrl, "drawable", holder.itemView.getContext().getPackageName());

            if (resId != 0) {
                holder.bookCover.setImageResource(resId);
            } else {
                holder.bookCover.setImageResource(R.drawable.img_none_cover);
            }
        }
        else {
            holder.bookCover.setImageResource(R.drawable.img_none_cover);
        }

        holder.bookMenu.setOnClickListener(v -> {
            if (menuClickListener != null) {
                menuClickListener.onBookMenuClick(book, holder.bookMenu);
            }
        });

        holder.bookCover.setOnClickListener(v -> {
            BookTimerFragment fragment = BookTimerFragment.newInstance(
                    book.getTitle(),
                    book.getCoverUrl()
            );

            fragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
}