package com.example.bookvoyager;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bookvoyager.firebase.BookLibraryManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchFragment extends Fragment {

    private static final String BASE_API_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final String MAX_RESULTS = "&maxResults=20";
    private static final String DEFAULT_AUTHOR = "Автор невідомий";
    private static final String DEFAULT_TITLE = "Назва невідома";

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private SearchBookAdapter bookAdapter;

    private BookLibraryManager bookLibraryManager;

    private final List<Book> books = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initializeFirebase();
        initializeViews(view);
        setupRecyclerView();
        setupSearchButton(view);

        return view;
    }

    private void initializeFirebase() {
       bookLibraryManager = new BookLibraryManager(getActivity());
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.bookRecyclerView);
        searchEditText = view.findViewById(R.id.findNewBook);
        Button account_button = view.findViewById(R.id.account_button);
        account_button.setOnClickListener(v -> navigateToAccountActivity());
    }

    private void navigateToAccountActivity(){
        Intent intent = new Intent(getActivity(), AccountActivity.class);
        startActivity(intent);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        bookAdapter = new SearchBookAdapter(books, this::addBookToUserLibrary);
        recyclerView.setAdapter(bookAdapter);
    }

    private void addBookToUserLibrary(Book book) {

        bookLibraryManager.addBookToUserLibrary(book, new BookLibraryManager.AddBookCallback() {
            @Override
            public void onSuccess() {
                showToast("Книгу додано до бібліотеки");
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast("Помилка при додаванні книги");
            }
        });
//
//        Map<String, Object> bookData = new HashMap<>();
//        bookData.put("title", book.getTitle());
//        bookData.put("authors", book.getAuthor());
//        bookData.put("coverUrl", book.getCoverUrl());
//        bookData.put("pageCount", book.getPageCount());
//        bookData.put("description", book.getDescription());
//        bookData.put("country", book.getCountry());
//        bookData.put("addedDate", FieldValue.serverTimestamp());
//        bookData.put("isbn", book.getISBN());
//
//        db.collection("users")
//                .document(currentUserId)
//                .collection("books")
//                .document() // автоматичний ID
//                .set(bookData)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        showToast("Книгу додано до бібліотеки");
//                    } else {
//                        showToast("Помилка при додаванні книги");
//                    }
//                });
//
//        Map<String, Object> sessions = new HashMap<>();
//        sessions.put("title", book.getTitle());
//        sessions.put("pagesRead", 0);
//        sessions.put("pagesCount", book.getPageCount());
//
//        db.collection("users")
//                .document(currentUserId)
//                .collection("readingSessions")
//                .document()
//                .set(sessions);
//
//        db.collection("users")
//                .document(currentUserId)
//                .collection("locationSpot")
//                .whereEqualTo("locationId", book.getCountry())
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            long currentCount = document.getLong("countRequiredBooks") != null
//                                    ? document.getLong("countRequiredBooks")
//                                    : 0;
//
//                            db.collection("users")
//                                    .document(currentUserId)
//                                    .collection("locationSpot")
//                                    .document(document.getId())
//                                    .update("countRequiredBooks", currentCount + 1);
//                        }
//                    } else {
//                        Map<String, Object> countryData = new HashMap<>();
//                        countryData.put("locationId", book.getCountry());
//                        countryData.put("countRequiredBooks", 1);
//                        countryData.put("ifUnlocked", true);
//
//                        db.collection("users")
//                                .document(currentUserId)
//                                .collection("locationSpot")
//                                .document()
//                                .set(countryData);
//                    }
//                });
    }

    private void setupSearchButton(View view) {
        ImageView searchButton = view.findViewById(R.id.search_image);
        searchButton.setOnClickListener(v -> performSearch());
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();

        if (query.isEmpty()) {
            showToast("Будь ласка, введіть пошуковий запит");
            return;
        }

        searchBooks(query);
    }

    private void searchBooks(String query) {
        new Thread(() -> {
            try {
                String apiUrl = BASE_API_URL + query + MAX_RESULTS;
                String jsonData = fetchDataFromApi(apiUrl);
                processApiResponse(jsonData);
            } catch (Exception e) {
                handleSearchError(e);
            }
        }).start();
    }

    private String fetchDataFromApi(String url) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private void processApiResponse(String jsonData) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray items = jsonObject.optJSONArray("items");

        if (items == null || items.length() == 0) {
            showToastOnUiThread("Книги не знайдено");
            return;
        }

        List<Book> foundBooks = parseBooksFromJson(items);
        updateUiWithBooks(foundBooks);
    }

    private List<Book> parseBooksFromJson(JSONArray items) throws Exception {
        List<Book> foundBooks = new ArrayList<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            JSONObject volumeInfo = item.optJSONObject("volumeInfo");
            if (volumeInfo == null) continue;

            Book book = createBookFromVolumeInfo(volumeInfo, item);
            foundBooks.add(book);
        }

        return foundBooks;
    }

    private Book createBookFromVolumeInfo(JSONObject volumeInfo, JSONObject item) throws Exception {
        String title = volumeInfo.optString("title", DEFAULT_TITLE);
        String authors = getAuthorsFromJson(volumeInfo);
        String imageUrl = getImageUrlFromJson(volumeInfo);
        int pageCount = volumeInfo.optInt("pageCount", 0);
        String description = volumeInfo.optString("description", "No description available");
        String country = "Unknown country";
        JSONObject saleInfo = item.optJSONObject("saleInfo");
        if (saleInfo != null && saleInfo.has("country")) {
            country = saleInfo.getString("country");
        }
        System.out.println("Book cover URL: " + imageUrl);

        String isbn = getIsbnFromJson(volumeInfo);
        Book b = new Book(isbn, title, authors, imageUrl);
        b.setCountry(country);
        b.setDescription(description);
        b.setPageCount(pageCount);

        return b;
    }

    private String getAuthorsFromJson(JSONObject volumeInfo) throws Exception {
        JSONArray authorsArray = volumeInfo.optJSONArray("authors");
        return (authorsArray != null && authorsArray.length() > 0)
                ? authorsArray.join(", ").replace("\"", "")
                : DEFAULT_AUTHOR;
    }

    private String getImageUrlFromJson(JSONObject volumeInfo) throws Exception {
        if (!volumeInfo.has("imageLinks"))
            return null;

        JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
        if (!imageLinks.has("thumbnail"))
            return null;

        String thumbnailUrl = imageLinks.getString("thumbnail");
        thumbnailUrl = thumbnailUrl.replace("http://", "https://");

        return thumbnailUrl;
    }

    private void updateUiWithBooks(List<Book> books) {
        requireActivity().runOnUiThread(() -> {
            this.books.clear();
            this.books.addAll(books);
            bookAdapter.notifyDataSetChanged();
        });
    }

    private void handleSearchError(Exception e) {
        e.printStackTrace();
        showToastOnUiThread("Помилка пошуку: " + e.getMessage());
    }

    private void showToastOnUiThread(String message) {
        requireActivity().runOnUiThread(() -> showToast(message));
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String getIsbnFromJson(JSONObject volumeInfo) throws Exception {
        JSONArray identifiers = volumeInfo.optJSONArray("industryIdentifiers");
        if (identifiers != null) {
            for (int i = 0; i < identifiers.length(); i++) {
                JSONObject id = identifiers.getJSONObject(i);
                if ("ISBN_13".equals(id.optString("type"))) {
                    return id.optString("identifier");
                }
            }
        }
        return "Невідомо";
    }
}