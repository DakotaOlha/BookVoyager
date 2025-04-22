package com.example.bookvoyager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookTimerFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_PROGRESS = "progress";
    private static final String ARG_TIME_LOGGED = "time_logged";

    private

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUserId;

    public static BookTimerFragment newInstance(String title, String progress, String timeLogged) {
        BookTimerFragment fragment = new BookTimerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_PROGRESS, progress);
        args.putString(ARG_TIME_LOGGED, timeLogged);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_timer, container, false);

        initializeFirebase();
        getDataFromFirebase();

        TextView tvTitle = view.findViewById(R.id.tvBookTitle);

        if (getArguments() != null) {
            tvTitle.setText(getArguments().getString(ARG_TITLE));
        }

        return view;
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private void getDataFromFirebase() {

    }
}