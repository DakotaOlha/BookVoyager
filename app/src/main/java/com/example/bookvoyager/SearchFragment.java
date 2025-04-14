package com.example.bookvoyager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUserId;

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
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.bookRecyclerView);
        searchEditText = view.findViewById(R.id.findNewBook);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        bookAdapter = new SearchBookAdapter(books, this::addBookToUserLibrary);
        recyclerView.setAdapter(bookAdapter);
    }

    private void addBookToUserLibrary(Book book) {
        if (currentUserId == null) {
            showToast("Будь ласка, увійдіть в систему");
            return;
        }

        Map<String, Object> bookData = new HashMap<>();
        bookData.put("title", book.getTitle());
        bookData.put("author", book.getAuthor());
        bookData.put("coverUrl", book.getCoverUrl());
        bookData.put("readingStatus", "Not read");
        bookData.put("addedDate", FieldValue.serverTimestamp());

        db.collection("users")
                .document(currentUserId)
                .collection("books")
                .document() // автоматичний ID
                .set(bookData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Книгу додано до бібліотеки");
                    } else {
                        showToast("Помилка при додаванні книги");
                    }
                });
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

            Book book = createBookFromVolumeInfo(volumeInfo);
            foundBooks.add(book);
        }

        return foundBooks;
    }

    private Book createBookFromVolumeInfo(JSONObject volumeInfo) throws Exception {
        String title = volumeInfo.optString("title", DEFAULT_TITLE);
        String authors = getAuthorsFromJson(volumeInfo);
        String imageUrl = getImageUrlFromJson(volumeInfo);

        System.out.println("Book cover URL: " + imageUrl);

        return new Book(title, authors, imageUrl, "Not read");
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
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

}