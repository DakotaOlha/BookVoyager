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
    private final UserStats stats = UserStats.getInstance();

    private final String currentUserId = getAuth().getCurrentUser() != null ? getAuth().getCurrentUser().getUid() : null;

    public BookLibraryManager(Context context){
        super();
        this.context = context;
        if(stats != null && currentUserId != null)
            loadUserStatsFromFirestore(currentUserId, () -> {});
    }

    public void addBookToUserLibrary(Book book, AddBookCallback callback) {

        Map<String, Object> bookData = getBookData(book);

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

                        RewardManager rewardManager = new RewardManager(currentUserId, reward -> {
                            Toast.makeText(context, "Отримано винагороду: " + reward.getName(), Toast.LENGTH_LONG).show();
                        });
                        rewardManager.checkAndAssignRewards(stats);

                        callback.onSuccess();
                    } else {
                        callback.onFailure("Помилка при додаванні книги");
                    }
                });

        addReadingSession(book);
        updateOrCreateLocation(book);
    }

    public void saveNewBookToFirestore(Book book, AddBookCallback callback) {

        Map<String, Object> bookData = getBookData(book);

        getDb().collection("users")
                .document(currentUserId)
                .collection("books")
                .add(bookData)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));

        addReadingSession(book);
        updateOrCreateLocation(book);
    }

    public void updateBookInFirestore(Book book, String documentId, AddBookCallback callback) {
        Map<String, Object> bookData = getBookData(book);

        getDb().collection("users")
                .document(currentUserId)
                .collection("books")
                .document(documentId)
                .update(bookData)
                .addOnSuccessListener(unused -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });

    }

    private Map<String, Object> getBookData(Book book) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", book.getTitle());
        data.put("authors", book.getAuthor());
        data.put("isbn", book.getISBN());
        data.put("pageCount", book.getPageCount());
        data.put("country", book.getCountry());
        data.put("description", book.getDescription());
        data.put("coverUrl", book.getCoverUrl());
        data.put("addedDate", FieldValue.serverTimestamp());
        return data;
    }

    private void addReadingSession(Book book) {
        Map<String, Object> sessions = new HashMap<>();
        sessions.put("title", book.getTitle());
        sessions.put("pagesRead", 0);
        sessions.put("pagesCount", book.getPageCount());

        getDb().collection("users")
                .document(currentUserId)
                .collection("readingSessions")
                .add(sessions);
    }

    private void updateOrCreateLocation(Book book) {
        getDb().collection("users")
                .document(currentUserId)
                .collection("locationSpot")
                .whereEqualTo("locationId", book.getCountry())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            long currentCount = doc.getLong("countRequiredBooks") != null
                                    ? doc.getLong("countRequiredBooks") : 0;
                            getDb().collection("users")
                                    .document(currentUserId)
                                    .collection("locationSpot")
                                    .document(doc.getId())
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
                                .add(countryData);
                    }
                });
    }

    public void loadUserStatsFromFirestore(String userId, Runnable onComplete) {
        getDb().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long booksAdded = documentSnapshot.getLong("booksAdded");
                        Long booksRead = documentSnapshot.getLong("booksRead");
                        Long countriesOpened = documentSnapshot.getLong("countriesOpened");

                        stats.setBooksAdded(booksAdded != null ? booksAdded.intValue() : 0);
                        stats.setBooksRead(booksRead != null ? booksRead.intValue() : 0);
                        stats.setCountriesOpened(countriesOpened != null ? countriesOpened.intValue() : 0);
                    }
                    onComplete.run();
                });
    }
}
