package com.example.bookvoyager.Firebase;

import android.widget.Toast;

import com.example.bookvoyager.Class.Session;
import com.google.firebase.firestore.FieldValue;

import java.util.List;

public class AddXpLevelToUser extends FirebaseService {

    private final String currentUserId = getAuth().getCurrentUser() != null ? getAuth().getCurrentUser().getUid() : null;

    public void addXpToUser(int xp, AddBookCallback callback) {

        getDb().collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(e -> {
                    int lastXp = e.getLong("xp").intValue();
                    updateLvl(lastXp-xp, lastXp);
                });

        getDb().collection("users")
                .document(currentUserId)
                .update("xp", FieldValue.increment(xp))
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.toString());
                });
    }
    private void updateLvl(int lastXp, int xp) {
        if((lastXp < 100 && xp >= 100)||(lastXp < 250 && xp >= 250) || (lastXp < 500 && xp >= 500) || (lastXp < 1000 && xp >= 1000))
            getDb().collection("users")
                .document(currentUserId)
                .update("level", FieldValue.increment(1));
    }

}
