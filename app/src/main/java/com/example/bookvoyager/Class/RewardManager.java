package com.example.bookvoyager.Class;

import com.example.bookvoyager.Firebase.FirebaseService;
import com.example.bookvoyager.RewardCallback;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RewardManager {
    private final FirebaseService firebaseService;
    private final String userId;
    private final RewardCallback callback;

    public RewardManager(String userId, RewardCallback callback){
        this.userId = userId;
        this.callback = callback;
        this.firebaseService = FirebaseService.getInstance();
    }


    public void checkAndAssignRewards(UserStats userStats) {
        firebaseService.getDb().collection("rewards").get().addOnSuccessListener(rewardSnapshot -> {
            firebaseService.getDb().collection("users")
                    .document(userId)
                    .collection("myRewards")
                    .get().addOnSuccessListener(userRewardSnapshot -> {
                        Set<String> userRewardIds = new HashSet<>();
                        for (DocumentSnapshot doc : userRewardSnapshot.getDocuments()) {
                            userRewardIds.add(doc.getId());
                        }

                        for (DocumentSnapshot doc : rewardSnapshot.getDocuments()) {
                            Reward reward = doc.toObject(Reward.class);
                            if (reward != null && !userRewardIds.contains(reward.getId())) {
                                if (isConditionMet(reward.getCondition(), userStats)) {
                                    assignRewardToUser(reward);
                                }
                            }
                        }
                    });
        });
    }

    private boolean isConditionMet(RewardCondition condition, UserStats stats) {
        switch (condition.getType()) {
            case "books_added":
                return stats.getBooksAdded() >= condition.getValue();
            case "books_read":
                return stats.getBooksRead() >= condition.getValue();
            case "books_added_by_country":
                int count = stats.getBooksByCountry().getOrDefault(condition.getCountry(), 0);
                return count >= condition.getValue();
            case "country_discovered":
                return stats.getCountriesOpened() >= condition.getValue();
            default:
                return false;
        }
    }

    private void assignRewardToUser(Reward reward) {
        firebaseService.getDb().collection("users")
                .document(userId)
                .collection("myRewards")
                .document(reward.getId())
                .set(new HashMap<String, Object>() {{
                    put("name", reward.getName());
                    put("description", reward.getDescription());
                    put("timestamp", FieldValue.serverTimestamp());
                }})
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onRewardGranted(reward);
                    }
                });
    }
}
