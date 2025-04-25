package com.example.bookvoyager.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseService {

    private static FirebaseService instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public FirebaseService() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getDb() {
        return db;
    }
}
