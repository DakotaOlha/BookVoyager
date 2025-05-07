package com.example.bookvoyager.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.bookvoyager.Activity.AccountActivity;
import com.example.bookvoyager.Adapters.RewardAdapter;
import com.example.bookvoyager.Class.Reward;
import com.example.bookvoyager.R;

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

        recyclerView = view.findViewById(R.id.rewardRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        rewards = new ArrayList<>();
        rewardAdapter = new RewardAdapter(rewards);
        recyclerView.setAdapter(rewardAdapter);
        Button account_button = view.findViewById(R.id.account_button);
        account_button.setOnClickListener(v -> navigateToAccountActivity());
        TextView levelCount = view.findViewById(R.id.levelCount);
        levelCount.setText("1");

        loadRewards();

        return view;
    }

    private void navigateToAccountActivity(){
        Intent intent = new Intent(getActivity(), AccountActivity.class);
        startActivity(intent);
    }

    private void loadRewards() {
        rewards.add(new Reward("Add first book"));
        rewards.add(new Reward("Add an Ukrainian book"));
        rewardAdapter.notifyDataSetChanged();
    }
}