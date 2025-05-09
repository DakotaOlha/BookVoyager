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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bookvoyager.Activity.AccountActivity;
import com.example.bookvoyager.Adapters.RewardAdapter;
import com.example.bookvoyager.Class.Reward;
import com.example.bookvoyager.Firebase.AuthRepository;
import com.example.bookvoyager.Firebase.FirebaseService;
import com.example.bookvoyager.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RewardsFragment extends Fragment {

    private FirebaseService firebaseService;
    String userId;
    private RecyclerView recyclerView;

    TextView xpText;

    private ProgressBar xpProgressBa;
    private RewardAdapter rewardAdapter;
    private List<Reward> rewards;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        firebaseService = FirebaseService.getInstance();
        userId = firebaseService.getAuth().getCurrentUser() != null ? firebaseService.getAuth().getCurrentUser().getUid() : null;

        xpText = view.findViewById(R.id.xpTextView);
        xpProgressBa = view.findViewById(R.id.xpProgressBa);
        recyclerView = view.findViewById(R.id.rewardRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        rewards = new ArrayList<>();
        rewardAdapter = new RewardAdapter(rewards);
        recyclerView.setAdapter(rewardAdapter);
        Button account_button = view.findViewById(R.id.account_button);
        account_button.setOnClickListener(v -> navigateToAccountActivity());

        TextView levelCount = view.findViewById(R.id.levelCount);

        firebaseService.getDb().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long level = documentSnapshot.getLong("level");
                        Long xp = documentSnapshot.getLong("xp");

                        if (level != null)
                            levelCount.setText(String.valueOf(level));
                        if (xp != null)
                            updateXpAndLvl(xp);
                    }
                })
                .addOnFailureListener(e -> {
                    levelCount.setText("N/A");
                    xpText.setText("XP: N/A");
                });

        loadRewards();

        return view;
    }

    private void updateXpAndLvl(Long xp) {
        if(xp < 100){
            xpText.setText(xp+"/100");
            float percent = (float) (xp/100.0);
            xpProgressBa.setProgress((int)(percent*100));
        }
        else if(xp < 250) {
            xpText.setText(xp+"/250");
            float percent = (float) (xp/250.0);
            xpProgressBa.setProgress((int)(percent*100));
        }
        else if(xp < 500){
            xpText.setText(xp+"/500");
            float percent = (float) (xp/500.0);
            xpProgressBa.setProgress((int)(percent*100));
        }
        else if(xp < 1000){
            xpText.setText(xp+"/1000");
            float percent = (float) (xp/1000.0);
            xpProgressBa.setProgress((int)(percent*100));
        }
        else {
            xpText.setText(xp+"");
            xpProgressBa.setProgress(100);
        }
    }

    private void navigateToAccountActivity(){
        Intent intent = new Intent(getActivity(), AccountActivity.class);
        startActivity(intent);
    }

    private void loadRewards() {
        firebaseService.getDb().collection("users")
                .document(userId)
                .collection("myRewards")
                .get().addOnSuccessListener(rewardSnapshot -> {
                    rewards.clear();
                    for (DocumentSnapshot doc : rewardSnapshot.getDocuments()) {
                        Reward reward = doc.toObject(Reward.class);
                        if(reward != null)
                            rewards.add(reward);
                    }
                    rewardAdapter.notifyDataSetChanged();
                });

    }
}