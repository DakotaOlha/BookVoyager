package com.example.bookvoyager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ReadingFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_AUTHOR = "author";
    private static final String ARG_DESCRIPTION = "description";

    public static ReadingFragment newInstance(String title, String author, String description) {
        ReadingFragment fragment = new ReadingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_AUTHOR, author);
        args.putString(ARG_DESCRIPTION, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reading, container, false);

        TextView titleView = view.findViewById(R.id.readingTitle);
        TextView authorView = view.findViewById(R.id.readingAuthor);
        TextView contentView = view.findViewById(R.id.readingContent);

        if (getArguments() != null) {
            titleView.setText(getArguments().getString(ARG_TITLE));
            authorView.setText(getArguments().getString(ARG_AUTHOR));
            contentView.setText(getArguments().getString(ARG_DESCRIPTION)); // Поки що замість контенту – опис
        }

        return view;
    }
}
