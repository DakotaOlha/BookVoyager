
package com.example.bookvoyager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyBooksFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> books;
    private EditText findBookText;
    private boolean wasFocused = false;

    private Button ifRead;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_books, container, false);

        Button account_button = view.findViewById(R.id.account_button);
        account_button.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AccountActivity.class);
            startActivity(intent);
        });

        ImageView search_btn = view.findViewById(R.id.search_image);
        Button sortingButton = view.findViewById(R.id.sortingButton);
        sortingButton.setOnClickListener(v -> showSortingMenu(v));

        ifRead = view.findViewById(R.id.ifRead);
        ifRead.setOnClickListener(v -> ifReadButtonClick(v));


        recyclerView = view.findViewById(R.id.bookRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        books = new ArrayList<>();
        bookAdapter = new BookAdapter(books);
        recyclerView.setAdapter(bookAdapter);

        loadBooks();

        findBookText = view.findViewById(R.id.findBook);
        search_btn.setOnClickListener(v -> {
            String query = findBookText.getText().toString().toLowerCase().trim();

            if(query.isEmpty()){
                bookAdapter.FilterList(books);
                return;
            }

            List<Book> filteredBooks = new ArrayList<>();

            for(Book bk: books){
                if(bk.getTitle().toLowerCase().contains(query) || bk.getAuthor().toLowerCase().contains(query)){
                    filteredBooks.add(bk);
                }
            }

            bookAdapter.FilterList(filteredBooks);
        });


        return view;
    }

    public void ifReadButtonClick(View view){
        if(ifRead.getText().equals("Is read")){
            ifRead.setText("In process");
            filterByProcess("In process");
        }
        else if(ifRead.getText().equals("In process")){
            ifRead.setText("Not read");
            filterByProcess("Not read");
        }
        else if(ifRead.getText().equals("Not read")){
            ifRead.setText("Read?");
            filterByProcess("Read?");
        }
        else if(ifRead.getText().equals("Read?")){
            ifRead.setText("Is read");
            filterByProcess("Is read");
        }
    }

    public void showSortingMenu(View view) {

        SortDialogFragment dialog = new SortDialogFragment(new SortDialogFragment.OnSortOptionSelected() {
            @Override
            public void onSortByTitle() {
                sortBooksByTitle();
            }

            @Override
            public void onSortByAuthor() {
                sortBooksByAuthor();
            }
        });

        dialog.show(getChildFragmentManager(), "SortDialog");

    }

    private void sortBooksByTitle(){
        Collections.sort(books, (b1, b2) -> b1.getTitle().compareToIgnoreCase(b2.getTitle()));
        bookAdapter.notifyDataSetChanged();
    }

    private void sortBooksByAuthor(){
        Collections.sort(books, (b1, b2) -> b1.getAuthor().compareToIgnoreCase(b2.getAuthor()));
        bookAdapter.notifyDataSetChanged();
    }

    private void filterByProcess(String filter){
        if(filter.equals("Read?")){
            bookAdapter.FilterList(books);
            return;
        }
        List<Book> filteredBooks = new ArrayList<>();
        for (Book bk: books){
            if(bk.getProgressReading().equals(filter)){
                filteredBooks.add(bk);
            }
        }
        bookAdapter.FilterList(filteredBooks);
    }

    private void loadBooks() {
        books.add(new Book("Книга 1", "Автор 1", "R.drawable.agata", "In process"));
        books.add(new Book("Книга 2", "Автор 2", "R.drawable.agata", "Is read"));
        books.add(new Book("Книга 3", "Автор 3", "R.drawable.agata", "In process"));
        books.add(new Book("Книга 4", "Автор 4", "R.drawable.agata", "Not read"));
        books.add(new Book("Книга 1", "Автор 3", "R.drawable.agata", "Not read"));
        books.add(new Book("Книга 2", "Автор 2", "R.drawable.agata", "Not read"));
        books.add(new Book("Книга 3", "Автор 1", "R.drawable.agata", "Not read"));
        books.add(new Book("Книга 4", "Автор 4", "R.drawable.agata", "In process"));
        bookAdapter.notifyDataSetChanged();
    }

}