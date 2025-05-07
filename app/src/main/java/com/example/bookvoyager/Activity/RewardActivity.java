package com.example.bookvoyager.Activity;

import static com.google.firebase.auth.AuthKt.getAuth;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookvoyager.Adapters.RewardAdapter;
import com.example.bookvoyager.Class.Reward;
import com.example.bookvoyager.Firebase.FirebaseService;
import com.example.bookvoyager.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RewardActivity extends AppCompatActivity {

    private FirebaseService firebaseService;
    String userId;
    private RecyclerView recyclerView;
    private RewardAdapter rewardAdapter;
    private List<Reward> rewards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reward);

        userId = firebaseService.getAuth().getCurrentUser() != null ? firebaseService.getAuth().getCurrentUser().getUid() : null;
        firebaseService = FirebaseService.getInstance();

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.rewardRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1)); // 2 колонки

        rewards = new ArrayList<>();
        rewardAdapter = new RewardAdapter(rewards);
        recyclerView.setAdapter(rewardAdapter);

        loadRewards();

    }

    private void loadRewards() {
        firebaseService.getDb().collection("users")
                .document(userId)
                .collection("myRewards")
                .get().addOnSuccessListener(rewardSnapshot -> {
                    for (DocumentSnapshot doc : rewardSnapshot.getDocuments()) {
                        Reward reward = doc.toObject(Reward.class);
                        if(reward != null)
                            rewards.add(reward);
                    }
                });
        rewardAdapter.notifyDataSetChanged();
    }
}