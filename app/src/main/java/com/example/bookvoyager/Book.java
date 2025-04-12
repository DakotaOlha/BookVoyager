package com.example.bookvoyager;

public class Book {
    private String title;
    private String author;
    private int coverResId;

    private String progressReading;

    public Book(String title, String author, int coverResId, String isRead) {
        this.title = title;
        this.author = author;
        this.coverResId = coverResId;
        this.progressReading = isRead;
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

    public String getProgressReading(){
        return progressReading;
    }
}
