package com.example.bookvoyager;

import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class RewardsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RewardAdapter rewardAdapter;
    private List<Reward> rewards;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

//        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
//            WindowInsetsCompat.Type type = WindowInsetsCompat.Type.systemBars();
//            v.setPadding(
//                    insets.getInsets(type).left,
//                    insets.getInsets(type).top,
//                    insets.getInsets(type).right,
//                    insets.getInsets(type).bottom
//            );
//            return insets;
//        });

        recyclerView = view.findViewById(R.id.rewardRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        rewards = new ArrayList<>();
        rewardAdapter = new RewardAdapter(rewards);
        recyclerView.setAdapter(rewardAdapter);

        loadRewards();

        return view;
    }

    private void loadRewards() {
        rewards.add(new Reward("Read 10 books in English", false));
        rewards.add(new Reward("100 books read", false));
        rewards.add(new Reward("Books from 10 different countries", false));
        rewards.add(new Reward("Read 10 books in English", false));
        rewards.add(new Reward("100 books read", false));
        rewards.add(new Reward("Books from 10 different countries", false));
        rewards.add(new Reward("Read 10 books in English", false));
        rewards.add(new Reward("100 books read", false));
        rewards.add(new Reward("Books from 10 different countries", false));
        rewardAdapter.notifyDataSetChanged();
    }
}