package com.example.bookvoyager.Firebase;

public interface AddBookCallback {
    void onSuccess();
    void onFailure(String errorMessage);
}