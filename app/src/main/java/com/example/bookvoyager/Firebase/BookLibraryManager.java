package com.example.bookvoyager.Firebase;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.util.Log;
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
    AddXpLevelToUser xpManager = new AddXpLevelToUser();

    private final String currentUserId = getAuth().getCurrentUser() != null ? getAuth().getCurrentUser().getUid() : null;

    public BookLibraryManager(Context context){
        super();
        this.context = context;
        if(stats != null && currentUserId != null)
            loadUserStatsFromFirestore(currentUserId, () -> {});
    }

    public void addBookToUserLibrary(Book book, AddBookCallback callback) {

        Map<String, Object> bookData = getBookData(book);
        bookData.put("status", false);

        getDb().collection("users")
                .document(currentUserId)
                .collection("books")
                .document()
                .set(bookData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        xpManager.addXpToUser(5, new AddBookCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(context, "XP успішно оновлено на +" + 5, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(context, "Не вдалося оновити XP: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });

                        stats.setBooksRead(stats.getBooksRead()+1);
                        stats.setBooksAdded(stats.getBooksAdded()+1);
                        stats.addCountry(book.getCountry());

                        getDb().collection("users")
                                .document(currentUserId)
                                .update("booksAdded", FieldValue.increment(1));

                        RewardManager rewardManager = new RewardManager(currentUserId, reward -> {
                            Toast.makeText(context, "Отримано винагороду: " + reward.getName(), Toast.LENGTH_LONG).show();
                            xpManager.addXpToUser(20, new AddBookCallback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onFailure(String errorMessage) {

                                }
                            });
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
        bookData.put("status", false);
        getDb().collection("users")
                .document(currentUserId)
                .collection("books")
                .add(bookData)
                .addOnSuccessListener(documentReference -> {
                    getDb().collection("users")
                            .document(currentUserId)
                            .update("booksAdded", FieldValue.increment(1));
                    callback.onSuccess();
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));

        addReadingSession(book);
        updateOrCreateLocation(book);
    }

    public void updateBookInFirestore(Book book, String lastCountry, String documentId, AddBookCallback callback) {
        Map<String, Object> bookData = getBookData(book);
        bookData.put("status", false);

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

        updateLocationAfterUpdate(book, lastCountry);

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
        sessions.put("status", false);

        getDb().collection("users")
                .document(currentUserId)
                .collection("readingSessions")
                .add(sessions);
    }

    private void updateLocationAfterUpdate(Book book, String lastCountry){
        if(book.getCountry().equals(lastCountry))
            return;
        getDb().collection("users")
                .document(currentUserId)
                .collection("locationSpot")
                .whereEqualTo("locationId", lastCountry)
                .get()
                .addOnSuccessListener(task -> {
                    if(!task.isEmpty()){
                        for(QueryDocumentSnapshot doc : task){
                            long currentCount = doc.getLong("countRequiredBooks") != null
                                    ? doc.getLong("countRequiredBooks") : 0;

                            Map<String, Object> updates = new HashMap<>();
                            long newCount = currentCount - 1;
                            updates.put("countRequiredBooks", newCount);
                            if (newCount <= 0) {
                                updates.put("ifUnlocked", false);
                            }

                            getDb().collection("users")
                                    .document(currentUserId)
                                    .collection("locationSpot")
                                    .document(doc.getId())
                                    .update(updates);
                        }
                    }
                });
        getDb().collection("users")
                .document(currentUserId)
                .collection("locationSpot")
                .whereEqualTo("locationId", book.getCountry())
                .get()
                .addOnSuccessListener(task -> {
                    if (!task.isEmpty()) {
                        for (QueryDocumentSnapshot doc : task) {
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

                        xpManager.addXpToUser(15, new AddBookCallback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(String errorMessage) {

                            }
                        });
                    }
                });
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
                        xpManager.addXpToUser(15, new AddBookCallback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(String errorMessage) {

                            }
                        });
                    }
                });
    }

    public void deleteBookFromFirestore(Book book, AddBookCallback callback) {
        getDb().collection("users")
                .document(currentUserId)
                .collection("books")
                .whereEqualTo("title", book.getTitle())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        getDb().collection("users")
                                .document(currentUserId)
                                .collection("books")
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    getDb().collection("users")
                                            .document(currentUserId)
                                            .update("booksAdded", FieldValue.increment(-1));
                                    deleteReadingSession(book);
                                    deleteLocation(book);
                                    callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    callback.onFailure(e.getMessage());
                                });
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

    private void deleteReadingSession(Book book) {
        getDb().collection("users")
                .document(currentUserId)
                .collection("readingSessions")
                .whereEqualTo("title", book.getTitle())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot){
                        doc.getReference().delete();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SessionDelete", "Failed to delete reading sessions", e);
                });
    }

    private void deleteLocation(Book book){
        getDb().collection("users")
                .document(currentUserId)
                .collection("locationSpot")
                .whereEqualTo("locationId", book.getCountry())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot){
                        long currentCount = doc.getLong("countRequiredBooks") != null
                                ? doc.getLong("countRequiredBooks") : 0;

                        getDb().collection("users")
                                .document(currentUserId)
                                .collection("locationSpot")
                                .document(doc.getId())
                                .update("countRequiredBooks", currentCount - 1);
                    }
                });
    }
}
