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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword, enterNick, enterDay, enterMonth, enterYear;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        fullScreen();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        signupEmail = findViewById(R.id.enterEmail);
        signupPassword = findViewById(R.id.enterPassword);
        enterNick = findViewById(R.id.enterNick);
        enterDay = findViewById(R.id.enterDay);
        enterMonth = findViewById(R.id.enterMonth);
        enterYear = findViewById(R.id.enterYear);


        Button registrationButton = findViewById(R.id.registration);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = signupEmail.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();
                String nick = enterNick.getText().toString().trim();
                String day = enterDay.getText().toString().trim();
                String month = enterMonth.getText().toString().trim();
                String year = enterYear.getText().toString().trim();

                if(nick.isEmpty()){
                    enterNick.setError("is empty");
                }
                else if(user.isEmpty()){
                    signupEmail.setError("is empty");
                }
                else if(pass.isEmpty()) {
                    signupPassword.setError("is empty");
                }
                else if(day.isEmpty() || month.isEmpty() || year.isEmpty()){
                    enterYear.setError("empty");
                }
                else {
                    auth.createUserWithEmailAndPassword(user, pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser currentUser = auth.getCurrentUser();
                                if(currentUser != null) {
                                    Map<String, Object> userData = new HashMap<>();
                                    String birth = day + "-" + month + "-" + year;
                                    userData.put("nickname", nick);
                                    userData.put("email", user);
                                    userData.put("birth", birth);
                                    userData.put("registration_date", FieldValue.serverTimestamp());
                                    userData.put("uid", currentUser.getUid());

                                    userData.put("books", new ArrayList<>());
                                    userData.put("settings", new HashMap<String, Object>());


                                    db.collection("users")
                                            .document(currentUser.getUid())
                                            .set(userData)
                                            .addOnCompleteListener(dbTask -> {
                                                if(dbTask.isSuccessful()){
                                                    // Додатково зберігаємо в SharedPreferences
                                                    SharedPreferences preferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = preferences.edit();
                                                    editor.putBoolean("isLoggedIn", true);
                                                    editor.putString("email", user);
                                                    editor.putString("uid", currentUser.getUid());
                                                    editor.apply();

                                                    Toast.makeText(RegistrationActivity.this, "Реєстрація успішна!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(RegistrationActivity.this, MainMenuActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(RegistrationActivity.this, "Помилка збереження даних", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                            else {
                                Toast.makeText(RegistrationActivity.this, "Помилка реєстрації", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
    }

    private void fullScreen(){
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }
}