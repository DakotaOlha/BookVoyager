package com.example.bookvoyager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "userPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText nicknameEditText;
    private EditText dayEditText;
    private EditText monthEditText;
    private EditText yearEditText;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setupFullScreen();
        initializeFirebase();
        initializeViews();
        setupRegistrationButton();

    }

    private void setupRegistrationButton() {
        Button registrationButton = findViewById(R.id.registration);
        registrationButton.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptRegistration() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String nickname = nicknameEditText.getText().toString().trim();
        String day = dayEditText.getText().toString().trim();
        String month = monthEditText.getText().toString().trim();
        String year = yearEditText.getText().toString().trim();

        if (validateInput(email, password, nickname, day, month, year)) {
            registerUser(email, password, nickname, day, month, year);
        }
    }

    private void registerUser(String email, String password, String nickname, String day, String month, String year) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user, email, nickname, day, month, year);
                        }
                    } else {
                        showRegistrationError(Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    private void saveUserData(FirebaseUser user, String email, String nickname,
                              String day, String month, String year) {
        String birthDate = day + "-" + month + "-" + year;

        Map<String, Object> userData = new HashMap<>();
        userData.put("nickname", nickname);
        userData.put("email", email);
        userData.put("birth", birthDate);
        userData.put("registration_date", FieldValue.serverTimestamp());
        userData.put("uid", user.getUid());
        userData.put("level", 0);
        userData.put("xp", 0);
        userData.put("booksRead", 0);

        db.collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveLoginPreferences(user, email);
                        navigateToMainMenu();
                        showSuccessMessage();
                    } else {
                        showDatabaseError();
                    }
                });
    }

    private void saveLoginPreferences(FirebaseUser user, String email) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_EMAIL, email)
                .putString(KEY_UID, user.getUid())
                .apply();
    }

    private void navigateToMainMenu() {
        startActivity(new Intent(this, MainMenuActivity.class));
        finish();
    }

    private void showSuccessMessage() {
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
    }

    private void showRegistrationError(String errorMessage) {
        Toast.makeText(this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showDatabaseError() {
        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
    }

    private boolean validateInput(String email, String password, String nickname, String day, String month, String year) {
        if (nickname.isEmpty()) {
            nicknameEditText.setError("Nickname cannot be empty");
            return false;
        }
        if (email.isEmpty()) {
            emailEditText.setError("Email cannot be empty");
            return false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password cannot be empty");
            return false;
        }
        if (day.isEmpty() || month.isEmpty() || year.isEmpty()) {
            yearEditText.setError("Birth date cannot be empty");
            return false;
        }
        return true;
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.enterEmail);
        passwordEditText = findViewById(R.id.enterPassword);
        nicknameEditText = findViewById(R.id.enterNick);
        dayEditText = findViewById(R.id.enterDay);
        monthEditText = findViewById(R.id.enterMonth);
        yearEditText = findViewById(R.id.enterYear);
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setupFullScreen(){
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }
}