package com.example.bookvoyager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BookTimerFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_PROGRESS = "progress";
    private static final String ARG_TIME_LOGGED = "time_logged";

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

        TextView tvTitle = view.findViewById(R.id.tvBookTitle);
        TextView tvProgress = view.findViewById(R.id.tvBookProgress);
        TextView tvTimeLogged = view.findViewById(R.id.tvTimeLogged);

        if (getArguments() != null) {
            tvTitle.setText(getArguments().getString(ARG_TITLE));
            tvProgress.setText(getArguments().getString(ARG_PROGRESS));
            tvTimeLogged.setText(getArguments().getString(ARG_TIME_LOGGED));
        }

        return view;
    }
}