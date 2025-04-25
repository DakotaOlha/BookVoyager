package com.example.bookvoyager.firebase;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final FirebaseService firebaseService;
    private final SharedPreferences preferences;
    private final Context context;

    public AuthRepository(Context context) {
        this.firebaseService = FirebaseService.getInstance();
        this.context = context;
        this.preferences = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
    }

    public void registerUser(String email, String password, String nickname, String birthDate, RegistrationCallback callback) {
        firebaseService.getAuth()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseService.getAuth().getCurrentUser();
                        if (user != null) {
                            saveUserData(user, email, nickname, birthDate, callback);
                        }
                    } else {
                        callback.onFailure(task.getException().getMessage());
                    }
                });
    }

    private void saveUserData(FirebaseUser user, String email, String nickname, String birthDate, RegistrationCallback callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("nickname", nickname);
        userData.put("email", email);
        userData.put("birth", birthDate);
        userData.put("registration_date", FieldValue.serverTimestamp());
        userData.put("uid", user.getUid());
        userData.put("level", 0);
        userData.put("xp", 0);
        userData.put("booksRead", 0);

        firebaseService.getDb()
                .collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveLoginPreferences(user, email);
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Failed to save user data.");
                    }
                });
    }

    private void saveLoginPreferences(FirebaseUser user, String email) {
        preferences.edit()
                .putBoolean("isLoggedIn", true)
                .putString("email", email)
                .putString("uid", user.getUid())
                .apply();
    }

    public void loginUser(String email, String password, LoginCallback callback) {
        firebaseService.getAuth()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseService.getAuth().getCurrentUser();
                        if (user != null) {
                            saveLoginPreferences(user, email);
                            callback.onSuccess();
                        } else {
                            callback.onFailure("User is null after login.");
                        }
                    } else {
                        callback.onFailure("Authentication failed. Check credentials.");
                    }
                });
    }

    public interface RegistrationCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }


    public interface LoginCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
