package com.example.bookvoyager;

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

import java.util.ArrayList;
import java.util.List;

public class RewardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RewardAdapter rewardAdapter;
    private List<Reward> rewards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reward);

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
        rewards.add(new Reward("Read 10 books in English", (1>10)));
        rewards.add(new Reward("100 books read", (1>10)));
        rewards.add(new Reward("books from 10 different countries were read", (1>10)));
        rewards.add(new Reward("Read 10 books in English", (1>10)));
        rewards.add(new Reward("100 books read", (1>10)));
        rewards.add(new Reward("books from 10 different countries were read", (1>10)));
        rewards.add(new Reward("Read 10 books in English", (1>10)));
        rewards.add(new Reward("100 books read", (1>10)));
        rewards.add(new Reward("books from 10 different countries were read", (1>10)));
        rewardAdapter.notifyDataSetChanged();
    }
}