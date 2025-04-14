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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText findBookText;
    private BookAdapter bookAdapter;
    private List<Book> books;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        ImageView search_btn = view.findViewById(R.id.search_image);

        recyclerView = view.findViewById(R.id.bookRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        books = new ArrayList<>();
        bookAdapter = new BookAdapter(books);
        recyclerView.setAdapter(bookAdapter);

        findBookText = view.findViewById(R.id.findNewBook);
        search_btn.setOnClickListener(v -> {
            String query = findBookText.getText().toString().toLowerCase().trim();
            if (!query.isEmpty()) {
                searchBooks(query);
//                fetchBookInfo(isbn);
            } else {
                Toast.makeText(getActivity(), "Введіть ISBN", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void searchBooks(String query) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = "https://www.googleapis.com/books/v1/volumes?q=" + query + "&maxResults=20";
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                String jsonData = response.body().string();

                JSONObject jsonObject = new JSONObject(jsonData);
                JSONArray items = jsonObject.optJSONArray("items");

                if (items == null || items.length() == 0) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Книги не знайдено", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                List<Book> foundBooks = new ArrayList<>();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject volumeInfo = item.optJSONObject("volumeInfo");
                    if (volumeInfo == null) continue;

                    String title = volumeInfo.optString("title", "Назва невідома");

                    JSONArray authorsArray = volumeInfo.optJSONArray("authors");
                    String authors = (authorsArray != null && authorsArray.length() > 0)
                            ? authorsArray.join(", ").replace("\"", "")
                            : "Автор невідомий";

                    String imageUrl = null;
                    if (volumeInfo.has("imageLinks")) {
                        JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                        if (imageLinks.has("thumbnail")) {
                            imageUrl = imageLinks.getString("thumbnail")
                                    .replace("http://", "https://");
                        }
                    }

                    foundBooks.add(new Book(title, authors, imageUrl, "Not read"));
                }

                requireActivity().runOnUiThread(() -> {
                    books.clear();
                    books.addAll(foundBooks);
                    bookAdapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Помилка пошуку: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

}