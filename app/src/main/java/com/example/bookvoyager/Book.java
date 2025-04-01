package com.example.bookvoyager;

public class Book {
    private String title;
    private String author;
    private int coverResId;

    public Book(String title, String author, int coverResId) {
        this.title = title;
        this.author = author;
        this.coverResId = coverResId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getCoverResId() {
        return coverResId;
    }
}
