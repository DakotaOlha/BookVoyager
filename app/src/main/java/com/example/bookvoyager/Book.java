package com.example.bookvoyager;

public class Book {
    private String ISBN;
    private String title;
    private String authors;
    private int pageCount;
    private String coverUrl;
    private String description;
    private String country;

    public Book(){
        this.ISBN = "";
        this.title = "";
        this.authors = "";
        this.pageCount = 0;
        this.description = "";
        this.country = "";
    }

    public Book(String title, String authors, String coverUrl) {
        this.title = title;
        this.authors = authors;
        this.coverUrl = coverUrl;
    }

    public Book(String ISBN, String title, String authors, String coverUrl) {
        this.ISBN = ISBN;
        this.title = title;
        this.authors = authors;
        this.coverUrl = coverUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return authors;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getISBN() {
        return ISBN;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String getDescription() {
        return description;
    }

    public String getCountry() {
        return country;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.authors = author;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
