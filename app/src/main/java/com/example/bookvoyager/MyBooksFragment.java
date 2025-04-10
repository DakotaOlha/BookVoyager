
package com.example.bookvoyager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyBooksFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> books;
    private EditText findBookText;
    private boolean wasFocused = false;

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

        search_btn.setOnClickListener(v -> {
            Toast toast = Toast.makeText(getActivity(), "search", Toast.LENGTH_SHORT);
            toast.show();
        });


        recyclerView = view.findViewById(R.id.bookRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2)); // 2 columns

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

        //setupKeyboardListener(view);

        return view;
    }

//    private void setupKeyboardListener(View rootView) {
//        findBookText.setOnFocusChangeListener((v, hasFocus) -> {
//            wasFocused = hasFocus;
//        });
//
//        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
//            Rect r = new Rect();
//            rootView.getWindowVisibleDisplayFrame(r);
//            int screenHeight = rootView.getRootView().getHeight();
//            int keypadHeight = screenHeight - r.bottom;
//
//            boolean isKeyboardOpen = keypadHeight > screenHeight * 0.15;
//
//            if (!isKeyboardOpen && wasFocused) {
//                findBookText.clearFocus();
//                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(findBookText.getWindowToken(), 0);
//                wasFocused = false;
//            }
//        });
//    }

    private void loadBooks() {
        books.add(new Book("Книга 1", "Автор 1", R.drawable.agata));
        books.add(new Book("Книга 2", "Автор 2", R.drawable.agata));
        books.add(new Book("Книга 3", "Автор 3", R.drawable.agata));
        books.add(new Book("Книга 4", "Автор 4", R.drawable.agata));
        books.add(new Book("Книга 1", "Автор 3", R.drawable.agata));
        books.add(new Book("Книга 2", "Автор 2", R.drawable.agata));
        books.add(new Book("Книга 3", "Автор 1", R.drawable.agata));
        books.add(new Book("Книга 4", "Автор 4", R.drawable.agata));
        bookAdapter.notifyDataSetChanged();
    }

}