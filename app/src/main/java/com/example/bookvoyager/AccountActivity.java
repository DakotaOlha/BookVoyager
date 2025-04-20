package com.example.bookvoyager;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button changeEmailBtn = findViewById(R.id.changeEmail);
        changeEmailBtn.setOnClickListener(v -> showChangeEmailDialog());

        Button exitBtn = findViewById(R.id.exit);
        exitBtn.setOnClickListener(v -> signOut());
    }

    private void showChangeEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_email, null);
        builder.setView(dialogView);

        EditText newEmailEditText = dialogView.findViewById(R.id.newEmail);
        EditText passwordEditText = dialogView.findViewById(R.id.password);

        builder.setTitle("Зміна email")
                .setPositiveButton("Змінити", (dialog, id) -> {
                    String newEmail = newEmailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    changeUserEmail(newEmail, password);
                })
                .setNegativeButton("Скасувати", (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Підтвердження виходу")
                .setMessage("Ви впевнені, що хочете вийти з акаунту?")
                .setPositiveButton("Так", (dialog, id) -> {
                    try {
                        // 1. Виходимо з Firebase
                        FirebaseAuth.getInstance().signOut();

                        // 2. Очищаємо SharedPreferences
                        clearLoginPreferences();

                        // 3. Переходимо на LoginActivity
                        navigateToLogin();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AccountActivity.this,
                                "Помилка при виході: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Ні", (dialog, id) -> dialog.dismiss())
                .show();
    }

    private void clearLoginPreferences() {
        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Очищаємо всі збережені дані
        editor.apply();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);

        // Додаємо флаги для очистки стеку активностей
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finishAffinity(); // Завершуємо всі активності
    }

    private void changeUserEmail(String newEmail, String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Користувач не автентифікований", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updateEmail(newEmail)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(AccountActivity.this,
                                                "Email успішно змінено", Toast.LENGTH_SHORT).show();

                                        user.sendEmailVerification()
                                                .addOnCompleteListener(emailTask -> {
                                                    if (emailTask.isSuccessful()) {
                                                        Toast.makeText(AccountActivity.this,
                                                                "Лист підтвердження відправлено",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(AccountActivity.this,
                                                "Помилка зміни email: " + updateTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(AccountActivity.this,
                                "Помилка автентифікації: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}