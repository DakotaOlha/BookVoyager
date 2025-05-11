package com.example.bookvoyager.Firebase;

import com.example.bookvoyager.Class.ReadingSessions;
import com.example.bookvoyager.Class.Session;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionsManager extends FirebaseService{

    private final String currentUserId;

    private String currentSessionId = null;

    public SessionsManager(String currentSessionId){
        this.currentSessionId = currentSessionId;
        this.currentUserId = getCurrentUserId();
    }

    private String getCurrentUserId() {
        return getAuth().getCurrentUser() != null ? getAuth().getCurrentUser().getUid() : null;
    }

    public void saveReadingProgress(ReadingSessions readingSessions, int currentPage, long readingTime, String date) {
        if(readingSessions == null || currentUserId == null) return;

        if(currentPage > readingSessions.getPagesRead() && currentPage < readingSessions.getPagesCount())
            updateReadingProgress(readingSessions, currentPage, readingTime, date);

        else if (currentPage > readingSessions.getPagesRead() && currentPage >= readingSessions.getPagesCount()) {
            markBookAsCompleted(readingSessions, readingTime, date);
            AddXpLevelToUser xpManager = new AddXpLevelToUser();
            xpManager.addXpToUser(20, new AddBookCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(String errorMessage) {

                }
            });
        }

    }

    private void updateReadingProgress(ReadingSessions readingSessions, int currentPage, long readingTime, String date){
        readingSessions.setPagesRead(currentPage);

        int percent = (currentPage* 100) / readingSessions.getPagesCount();

        Map<String, Object> session = new HashMap<>();
        session.put("date", date);
        session.put("currentPage", currentPage);
        session.put("readingTime", readingTime);
        session.put("percent", percent);

        getDb().collection("users")
                .document(currentUserId)
                .collection("readingSessions")
                .document(currentSessionId)
                .collection("session")
                .add(session);

        Map<String, Object> updates = new HashMap<>();
        updates.put("pagesRead", currentPage);

        getDb().collection("users")
                .document(currentUserId)
                .collection("readingSessions")
                .whereEqualTo("title", readingSessions.getTitle())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentReference docRef = task.getResult().getDocuments().get(0).getReference();
                        docRef.update(updates);
                    }
                });
    }

    public void getSessions(FirestoreCallback callback){
        getDb().collection("users")
                .document(currentUserId)
                .collection("readingSessions")
                .document(currentSessionId)
                .collection("session")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Session> sessionsList = queryDocumentSnapshots.toObjects(Session.class);
                    callback.onCallback(sessionsList);
                });
    }

    public interface FirestoreCallback {
        void onCallback(List<Session> sessions);
    }

    private void markBookAsCompleted(ReadingSessions readingSessions, long readingTime, String date) {
        readingSessions.setPagesRead(readingSessions.getPagesCount());
        readingSessions.setStatus(true);

        Map<String, Object> session = new HashMap<>();
        session.put("date", date);
        session.put("currentPage", readingSessions.getPagesCount());
        session.put("readingTime", readingTime);
        session.put("percent", 100);

        getDb().collection("users")
                .document(currentUserId)
                .collection("readingSessions")
                .document(currentSessionId)
                .collection("session")
                .add(session);

        Map<String, Object> updates = new HashMap<>();
        updates.put("pagesRead", readingSessions.getPagesCount());
        updates.put("status", true);

        getDb().collection("users")
                .document(currentUserId)
                .collection("readingSessions")
                .whereEqualTo("title", readingSessions.getTitle())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentReference docRef = task.getResult().getDocuments().get(0).getReference();
                        docRef.update(updates);
                    }
                });
        Map<String, Object> updatesBook = new HashMap<>();
        updatesBook.put("status", true);

        getDb().collection("users")
                .document(currentUserId)
                .collection("books")
                .whereEqualTo("title", readingSessions.getTitle())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentReference docRef = task.getResult().getDocuments().get(0).getReference();
                        docRef.update(updatesBook);
                    }
                });
    }
}
