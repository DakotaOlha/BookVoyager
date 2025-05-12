package com.example.bookvoyager.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.bookvoyager.Activity.AccountActivity;
import com.example.bookvoyager.Adapters.SearchBookAdapter;
import com.example.bookvoyager.Animation;
import com.example.bookvoyager.Class.Book;
import com.example.bookvoyager.Class.RewardManager;
import com.example.bookvoyager.Class.UserStats;
import com.example.bookvoyager.Firebase.AddBookCallback;
import com.example.bookvoyager.Firebase.BookLibraryManager;
import com.example.bookvoyager.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    TextView xpText;
    Animation animation;

    private final List<Book> books = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initializeFirebase(view);
        initializeViews(view);
        setupRecyclerView();
        setupSearchButton(view);

        return view;
    }

    private void initializeFirebase(View view) {
        CardView rewardCard = view.findViewById(R.id.rewardCard);
        LottieAnimationView confettiAnimation = view.findViewById(R.id.confettiAnimation);
        TextView rewardDescription = view.findViewById(R.id.rewardDescription);
        bookLibraryManager = new BookLibraryManager(getActivity(), rewardCard, confettiAnimation, rewardDescription);
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.bookRecyclerView);
        searchEditText = view.findViewById(R.id.findNewBook);
        animation = new Animation();
        xpText = view.findViewById(R.id.xpText);
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
        bookLibraryManager.addBookToUserLibrary(book, new AddBookCallback() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {

                    xpText.setVisibility(View.VISIBLE);
                    xpText.setAlpha(0f);
                    xpText.setScaleX(0.5f);
                    xpText.setScaleY(0.5f);
                    xpText.setTranslationY(100f);

                    xpText.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationY(0f)
                            .setDuration(600)
                            .setInterpolator(new OvershootInterpolator())
                            .start();

                    // Затримка перед показом картки
//                    new Handler().postDelayed(() -> {
//                        animation.showRewardWithAutoHide(rewardCard, confettiAnimation, 3000);
//                    }, 300);
//
//                    // Сховати текст XP через 3 секунди
                    new Handler().postDelayed(() -> {
                        xpText.animate()
                                .alpha(0f)
                                .scaleX(0.5f)
                                .scaleY(0.5f)
                                .translationY(-100f)
                                .setDuration(400)
                                .withEndAction(() -> xpText.setVisibility(View.GONE))
                                .start();
                    }, 1500); // 300 + 3000
                });
            }

            @Override
            public void onFailure(String errorMessage) {
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