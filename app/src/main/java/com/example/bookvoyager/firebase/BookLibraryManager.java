package com.example.bookvoyager.firebase;

import android.content.Context;
import android.widget.Toast;

import com.example.bookvoyager.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class BookLibraryManager extends FirebaseService {

    private final Context context;

    public BookLibraryManager(Context context){
        super();

        this.context = context;
    }

    public void addBookToUserLibrary(Book book, AddBookCallback callback) {

        String currentUserId = getAuth().getCurrentUser() != null ? getAuth().getCurrentUser().getUid() : null;

        Map<String, Object> bookData = new HashMap<>();
        bookData.put("title", book.getTitle());
        bookData.put("authors", book.getAuthor());
        bookData.put("coverUrl", book.getCoverUrl());
        bookData.put("pageCount", book.getPageCount());
        bookData.put("description", book.getDescription());
        bookData.put("country", book.getCountry());
        bookData.put("addedDate", FieldValue.serverTimestamp());
        bookData.put("isbn", book.getISBN());


        getDb().collection("users")
                .document(currentUserId)
                .collection("books")
                .document()
                .set(bookData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Помилка при додаванні книги");
                    }
                });

        Map<String, Object> sessions = new HashMap<>();
        sessions.put("title", book.getTitle());
        sessions.put("pagesRead", 0);
        sessions.put("pagesCount", book.getPageCount());

        getDb().collection("users")
                .document(currentUserId)
                .collection("readingSessions")
                .document()
                .set(sessions);

        getDb().collection("users")
                .document(currentUserId)
                .collection("locationSpot")
                .whereEqualTo("locationId", book.getCountry())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            long currentCount = document.getLong("countRequiredBooks") != null
                                    ? document.getLong("countRequiredBooks")
                                    : 0;

                            getDb().collection("users")
                                    .document(currentUserId)
                                    .collection("locationSpot")
                                    .document(document.getId())
                                    .update("countRequiredBooks", currentCount + 1);
                        }
                    } else {
                        Map<String, Object> countryData = new HashMap<>();
                        countryData.put("locationId", book.getCountry());
                        countryData.put("countRequiredBooks", 1);
                        countryData.put("ifUnlocked", true);

                        getDb().collection("users")
                                .document(currentUserId)
                                .collection("locationSpot")
                                .document()
                                .set(countryData);
                    }
                });
    }


    public void saveNewBookToFirestore(Book book, AddBookCallback callback) {

        String userId = getAuth().getCurrentUser() != null ? getAuth().getCurrentUser().getUid() : null;

        Map<String, Object> bookData = new HashMap<>();
        bookData.put("title", book.getTitle());
        bookData.put("authors", book.getAuthor());
        bookData.put("isbn", book.getISBN());
        bookData.put("pageCount", book.getPageCount());
        bookData.put("country", book.getCountry());
        bookData.put("description", book.getDescription());
        bookData.put("addedDate", FieldValue.serverTimestamp());
        bookData.put("coverUrl", book.getCoverUrl());

        getDb().collection("users")
                .document(userId)
                .collection("books")
                .add(bookData)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));

        Map<String, Object> sessions = new HashMap<>();
        sessions.put("title", book.getTitle());
        sessions.put("pagesRead", 0);
        sessions.put("pagesCount", book.getPageCount());

        getDb().collection("users")
                .document(userId)
                .collection("readingSessions")
                .document()
                .set(sessions);

        getDb().collection("users")
                .document(userId)
                .collection("locationSpot")
                .whereEqualTo("locationId", book.getCountry())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            long currentCount = document.getLong("countRequiredBooks") != null
                                    ? document.getLong("countRequiredBooks")
                                    : 0;

                            getDb().collection("users")
                                    .document(userId)
                                    .collection("locationSpot")
                                    .document(document.getId())
                                    .update("countRequiredBooks", currentCount + 1);
                        }
                    } else {
                        Map<String, Object> countryData = new HashMap<>();
                        countryData.put("locationId", book.getCountry());
                        countryData.put("countRequiredBooks", 1);
                        countryData.put("ifUnlocked", true);

                        getDb().collection("users")
                                .document(userId)
                                .collection("locationSpot")
                                .document()
                                .set(countryData);
                    }
                });
    }

    public interface AddBookCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
