package com.example.bookvoyager;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

        Button clearDataBtn = findViewById(R.id.clearData);
        clearDataBtn.setOnClickListener(v -> confirmAndClearData());

        Button chageNickBtn = findViewById(R.id.chageNick);
        chageNickBtn.setOnClickListener(v -> showChangeNickDialog());

    }

    private void showChangeNickDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_nick, null);
        builder.setView(dialogView);

        EditText newNickEditText = dialogView.findViewById(R.id.newNick);

        builder.setTitle("Зміна email")
                .setPositiveButton("Змінити", (dialog, id) -> {
                    String newNick = newNickEditText.getText().toString().trim();
                    changeUserNick(newNick);
                })
                .setNegativeButton("Скасувати", (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void changeUserNick(String newNick) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Користувач не автентифікований", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .update("nickname", newNick)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Нікнейм успішно змінено", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Помилка зміни ніку: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void confirmAndClearData() {
        new AlertDialog.Builder(this)
                .setTitle("Очистити всі дані?")
                .setMessage("Це дію неможливо відмінити. Ви впевнені?")
                .setPositiveButton("Так", (dialog, which) -> deleteAllUserData())
                .setNegativeButton("Скасувати", null)
                .show();
    }

    private void deleteAllUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Користувач не авторизований", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("books")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    Toast.makeText(this, "Всі дані користувача видалено", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Помилка видалення: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        db.collection("users").document(userId).collection("locationSpot")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    Toast.makeText(this, "Всі дані користувача видалено", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Помилка видалення: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        db.collection("users").document(userId).collection("readingSessions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    Toast.makeText(this, "Всі дані користувача видалено", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Помилка видалення: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
        if (TextUtils.isEmpty(newEmail) || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(this, "Введіть коректний email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Введіть пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Користувач не автентифікований", Toast.LENGTH_SHORT).show();
            return;
        }

        // Перевірка, чи email дійсно змінився
        if (newEmail.equals(user.getEmail())) {
            Toast.makeText(this, "Новий email такий самий як поточний", Toast.LENGTH_SHORT).show();
            return;
        }

        // Повторна автентифікація
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        // Оновлення email у Firebase Auth
                        user.updateEmail(newEmail)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        // Оновлення у Firestore
                                        updateEmailInFirestore(newEmail);

                                        // Відправка листа підтвердження
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(emailTask -> {
                                                    if (emailTask.isSuccessful()) {
                                                        Toast.makeText(this,
                                                                "Лист підтвердження відправлено на " + newEmail,
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        handleEmailUpdateError(updateTask.getException());
                                    }
                                });
                    } else {
                        Toast.makeText(this,
                                "Помилка автентифікації: неправильний пароль",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleEmailUpdateError(Exception e) {
        String errorMessage = "Помилка оновлення email";
        if (e != null) {
            if (e instanceof FirebaseAuthWeakPasswordException) {
                errorMessage = "Слабкий пароль";
            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                errorMessage = "Невірний формат email";
            } else if (e instanceof FirebaseAuthUserCollisionException) {
                errorMessage = "Email вже використовується";
            }
            errorMessage += ": " + e.getMessage();
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void updateEmailInFirestore(String newEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .update("email", newEmail)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Email оновлено у базі даних", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Помилка оновлення email у базі: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}