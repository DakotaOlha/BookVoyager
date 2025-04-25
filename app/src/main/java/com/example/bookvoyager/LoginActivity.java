package com.example.bookvoyager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookvoyager.firebase.AuthRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "userPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";

    private AuthRepository authRepository;

    private EditText emailEditText, passwordEditText;
    private TextView registrationLink;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupFullScreen();
        initializeViews();
        checkUserLoginStatus();

    }

    private void setupFullScreen(){
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.enterEmail);
        passwordEditText = findViewById(R.id.enterPassword);
        registrationLink = findViewById(R.id.registrationTransitionLink);
        loginButton = findViewById(R.id.loginButton);

        setupClickListeners();
    }

    private void setupClickListeners() {
        registrationLink.setOnClickListener(v -> navigateToRegistration());
        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void navigateToRegistration() {
        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void checkUserLoginStatus() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (isLoggedIn && currentUser != null) {
            navigateToMainMenu();
        } else {
            if (currentUser == null) {
                clearLoginPreferences();
            }
            initializeFirebase();
        }
    }

    private void clearLoginPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    private void navigateToMainMenu() {
        Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeFirebase() {
        authRepository = new AuthRepository(this);
    }

    private void handleLogin() {
        String user = emailEditText.getText().toString().trim();
        String pass = passwordEditText.getText().toString().trim();

        if(validateInput(user, pass)){
            authenticateUser(user, pass);
        }
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            emailEditText.setError("Email cannot be empty");
            return false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password cannot be empty");
            return false;
        }

        return true;
    }

    private void authenticateUser(String email, String password) {
        authRepository.loginUser(email, password, new AuthRepository.LoginCallback() {
            @Override
            public void onSuccess() {
                navigateToMainMenu();
            }

            @Override
            public void onFailure(String errorMessage) {
                showLoginError(errorMessage);
            }
        });
    }

    private void showLoginError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}