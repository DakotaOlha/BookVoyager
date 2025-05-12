package com.example.bookvoyager.Class;
public class ReadingSessions {
    private String title;
    private int pagesCount;
    private int pagesRead;
    private boolean status;
    public ReadingSessions(String title, int pagesCount, int pagesRead, boolean status){
        this.title = title;
        this.pagesCount = pagesCount;
        this.pagesRead = pagesRead;
        this.status = status;
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
    public void setPagesRead(int page){ pagesRead = page;}
    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
}

