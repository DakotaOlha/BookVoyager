package com.example.bookvoyager.Firebase;

import android.content.Context;
import android.widget.Toast;

import com.example.bookvoyager.Class.Book;
import com.example.bookvoyager.Class.RewardManager;
import com.example.bookvoyager.Class.UserStats;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class BookLibraryManager extends FirebaseService {

    private final Context context;
    UserStats stats = UserStats.getInstance();

    String currentUserId = getAuth().getCurrentUser() != null ? getAuth().getCurrentUser().getUid() : null;

    public BookLibraryManager(Context context){
        super();
        this.context = context;
        if(stats != null)
            loadUserStatsFromFirestore(currentUserId, () -> {});
//        stats.setBooksAdded(0);
//        stats.setBooksRead(0);
//        stats.setCountriesOpened(0);
    }

    public void addBookToUserLibrary(Book book, AddBookCallback callback) {

        RewardManager rewardManager = new RewardManager(currentUserId, reward -> {
            Toast.makeText(context, "Отримано винагороду: " + reward.getName(), Toast.LENGTH_LONG).show();
        });

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
                        stats.setBooksRead(stats.getBooksRead()+1);
                        stats.setBooksAdded(stats.getBooksAdded()+1);
                        stats.addCountry(book.getCountry());
                        rewardManager.checkAndAssignRewards(stats);
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
    public void updateBookInFirestore(Book book, String documentId, AddBookCallback callback) {
        String userId = getAuth().getCurrentUser() != null ? getAuth().getCurrentUser().getUid() : null;
        getDb().collection("users")
                .document(userId)
                .collection("books")
                .document(documentId)
                .update(
                        "title", book.getTitle(),
                        "authors", book.getAuthor(),
                        "isbn", book.getISBN(),
                        "pageCount", book.getPageCount(),
                        "country", book.getCountry(),
                        "description", book.getDescription(),
                        "coverUrl", book.getCoverUrl()
                )
                .addOnSuccessListener(unused -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });

        if (!book.getCountry().equals(book.getCountry())) {
            getDb().collection("users")
                    .document(userId)
                    .collection("locationSpot")
                    .whereEqualTo("locationId", book.getCountry())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Long currentCount = document.getLong("countRequiredBooks");
                                if (currentCount != null && currentCount > 1) {
                                    getDb().collection("users")
                                            .document(userId)
                                            .collection("locationSpot")
                                            .document(document.getId())
                                            .update("countRequiredBooks", currentCount - 1);
                                } else {
                                    getDb().collection("users")
                                            .document(userId)
                                            .collection("locationSpot")
                                            .document(document.getId())
                                            .delete();
                                }
                            }
                        }
                    });

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
    }

    public void loadUserStatsFromFirestore(String userId, Runnable onComplete) {
        getDb().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserStats stats = UserStats.getInstance();
                        Long booksAdded = documentSnapshot.getLong("booksAdded");
                        Long booksRead = documentSnapshot.getLong("booksRead");
                        Long countriesOpened = documentSnapshot.getLong("countriesOpened");

                        stats.setBooksAdded(booksAdded != null ? booksAdded.intValue() : 0);
                        stats.setBooksRead(booksRead != null ? booksRead.intValue() : 0);
                        stats.setCountriesOpened(countriesOpened != null ? countriesOpened.intValue() : 0);

                        if(countriesOpened != null){
                            getDb().collection("users")
                                    .document(userId)
                                    .collection("locationSpot")
                                    .get()
                                    .addOnSuccessListener(map -> {
                                        Map<String, Integer> booksByCountryInt = new HashMap<>();
                                        for(DocumentSnapshot m : map.getDocuments()){
                                            booksByCountryInt.put(m.getString("locationId"), m.getLong("countRequiredBooks").intValue());
                                        }
                                        stats.setBooksByCountry(booksByCountryInt);
                                    });
                        }
                    }
                    if (onComplete != null) onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Помилка завантаження статистики", Toast.LENGTH_SHORT).show();
                    if (onComplete != null) onComplete.run();
                });
    }
}
