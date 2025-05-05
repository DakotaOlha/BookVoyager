package com.example.bookvoyager.Class;

public class ReadingSessions {
    private String title;
    private int pagesCount;
    private int pagesRead;

    public ReadingSessions(String title, int pagesCount, int pagesRead){
        this.title = title;
        this.pagesCount = pagesCount;
        this.pagesRead = pagesRead;
    }

    public String getTitle() {
        return title;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public int getPagesRead() {
        return pagesRead;
    }
}
