package com.example.bookvoyager.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookvoyager.Firebase.AuthRepository;
import com.example.bookvoyager.R;

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
    private AuthRepository authRepository;

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
        String birthDate = day + "-" + month + "-" + year;

        authRepository.registerUser(email, password, nickname, birthDate, new AuthRepository.RegistrationCallback() {
            @Override
            public void onSuccess() {
                navigateToMainMenu();
                showSuccessMessage();
            }

            @Override
            public void onFailure(String errorMessage) {
                showRegistrationError(errorMessage);
            }
        });
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
        authRepository = new AuthRepository(this);
    }

    private void setupFullScreen(){
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }
}