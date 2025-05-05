package com.example.bookvoyager.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bookvoyager.R;

public class SortDialogFragment extends DialogFragment {

    public interface OnSortOptionSelected {
        void onSortByTitle();
        void onSortByAuthor();
    }

    private OnSortOptionSelected listener;

    public SortDialogFragment(OnSortOptionSelected listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sort_dialog, container, false);

        view.findViewById(R.id.sortByTitle).setOnClickListener(v -> {
            listener.onSortByTitle();
            dismiss();
        });

        view.findViewById(R.id.sortByAuthor).setOnClickListener(v -> {
            listener.onSortByAuthor();
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog_rounded);
        }
    }
}