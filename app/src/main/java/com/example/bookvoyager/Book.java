package com.example.bookvoyager;

public class Book {
    private int ISBN;
    private String title;
    private String author;
    private int yearIssue;
    private String coverUrl;
    private String publisher;
    private String description;

    private String progressReading;

    public Book(String title, String author, String coverUrl, String isRead) {
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.progressReading = isRead;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getProgressReading(){
        return progressReading;
    }
}
