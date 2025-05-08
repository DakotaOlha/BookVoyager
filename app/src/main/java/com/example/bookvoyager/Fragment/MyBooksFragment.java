
package com.example.bookvoyager.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookvoyager.Activity.AccountActivity;
import com.example.bookvoyager.Adapters.BookAdapter;
import com.example.bookvoyager.Class.Book;
import com.example.bookvoyager.Class.RewardManager;
import com.example.bookvoyager.Class.UserStats;
import com.example.bookvoyager.R;
import com.example.bookvoyager.SortingBooks;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyBooksFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText findBookText;
    private Button ifReadButton;

    private BookAdapter bookAdapter;
    private List<Book> books = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    private SortingBooks sortingBooks = new SortingBooks();




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_books, container, false);

        initializeFirebase();
        initializeViews(view);
        setupRecyclerView(view);
        setupEventListeners(view);

        loadUserBooks();

        return view;
    }
    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
    }

    private void initializeViews(View view) {
        findBookText = view.findViewById(R.id.findBook);
        ifReadButton = view.findViewById(R.id.ifRead);

        Button account_button = view.findViewById(R.id.account_button);
        account_button.setOnClickListener(v -> navigateToAccountActivity());
    }

    private void navigateToAccountActivity(){
        Intent intent = new Intent(getActivity(), AccountActivity.class);
        startActivity(intent);
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.bookRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        bookAdapter = new BookAdapter(books, getParentFragmentManager());
        recyclerView.setAdapter(bookAdapter);
    }

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    private void clickOnMoreDots(View view){
        bookAdapter.setOnBookMenuClickListener((book, anchorView) -> {
            View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.custom_dots_menu, null);

            PopupWindow popupWindow = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            popupWindow.setElevation(20f);

            popupView.findViewById(R.id.menu_edit).setOnClickListener(v -> {
                db.collection("users")
                            .document(currentUserId)
                            .collection("books")
                            .whereEqualTo("title", book.getTitle())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                    new EditBookDialogFragment(book, docId, this::loadUserBooks, "Edit the book", "edit")
                                            .show(getParentFragmentManager(), "EditBook");
                                }
                            });
                popupWindow.dismiss();
            });

            popupView.findViewById(R.id.menu_del).setOnClickListener(v -> {
                deleteBookFromFirestore(book);
                popupWindow.dismiss();
            });

            popupView.setOnTouchListener((v, event) -> {
                popupWindow.dismiss();
                return true;
            });

            popupWindow.showAsDropDown(anchorView);
        });
    }

    private void setupEventListeners(View view) {
        ImageView search_btn = view.findViewById(R.id.search_image);
        search_btn.setOnClickListener(v -> filterBooksBySearchQuery());

        Button sortingButton = view.findViewById(R.id.sortingButton);
        sortingButton.setOnClickListener(v -> showSortingMenu(v));

        ifReadButton.setOnClickListener(v ->  toggleReadingFilter(v));

        clickOnMoreDots(view);
    }

    private void filterBooksBySearchQuery(){
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
    }

    public void toggleReadingFilter(View view){
        String currentFilter = ifReadButton.getText().toString();
        String newFilter;

        switch (currentFilter) {
            case "Is read":
                newFilter = "In process";
                break;
            case "In process":
                newFilter = "Not read";
                break;
            case "Not read":
                newFilter = "Read?";
                break;
            case "Read?":
                newFilter = "Is read";
                break;
            default:
                newFilter = "Is read";
        }

        ifReadButton.setText(newFilter);
        filterBooksByReadingStatus(newFilter);
    }

    public void showSortingMenu(View view) {
        SortDialogFragment dialog = new SortDialogFragment(new SortDialogFragment.OnSortOptionSelected() {
            @Override
            public void onSortByTitle() {
                books = sortingBooks.sortByTitle(books);
                bookAdapter.FilterList(books);
                bookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSortByAuthor() {
                sortingBooks.sortByAuthors(books);
                bookAdapter.FilterList(books);
                bookAdapter.notifyDataSetChanged();
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

    private void filterBooksByReadingStatus(String filter){
//        if(filter.equals("Read?")){
//            bookAdapter.FilterList(books);
//            return;
//        }
//        List<Book> filteredBooks = new ArrayList<>();
//        for (Book bk: books){
//            if(bk.getProgressReading().equals(filter)){
//                filteredBooks.add(bk);
//            }
//        }
//        bookAdapter.FilterList(filteredBooks);
    }

    public void loadUserBooks(){
        db.collection("users")
                .document(currentUserId)
                .collection("books")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        processFetchedBooks(task);
                    }
                    else {
                        Toast.makeText(getContext(), "Помилка завантаження книг", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processFetchedBooks(Task<QuerySnapshot> task) {
        books.clear();
        for (QueryDocumentSnapshot document : task.getResult()) {
            Book book = createBookFromDocument(document);
            books.add(book);
        }
        bookAdapter.notifyDataSetChanged();
    }

    private Book createBookFromDocument(QueryDocumentSnapshot document) {
        Book b = new Book(document.getString("isbn"), document.getString("title"), document.getString("authors"),document.getString("coverUrl"));
        b.setPageCount(Math.toIntExact(document.getLong("pageCount")));
        b.setCountry(document.getString("country"));
        b.setDescription(document.getString("description"));
        return b;
    }

    private void deleteBookFromFirestore(Book book) {
        db.collection("users")
                .document(currentUserId)
                .collection("books")
                .whereEqualTo("title", book.getTitle())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("users")
                                .document(currentUserId)
                                .collection("books")
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Книгу видалено", Toast.LENGTH_SHORT).show();
                                    loadUserBooks();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Помилка при видаленні книги", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

}