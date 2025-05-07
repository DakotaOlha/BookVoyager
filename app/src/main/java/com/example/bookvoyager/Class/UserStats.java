package com.example.bookvoyager.Class;

import java.util.HashMap;
import java.util.Map;

public class UserStats {
    private int booksAdded;
    private int booksRead;
    private Map<String, Integer> booksByCountry;
    private int newCountriesOpened;

    public UserStats(){
        booksByCountry = new HashMap<>();
    }

    public int getBooksAdded() {
        return booksAdded;
    }

    public void setBooksAdded(int booksAdded) {
        this.booksAdded = booksAdded;
    }

    public int getBooksRead() {
        return booksRead;
    }

    public void setBooksRead(int booksRead) {
        this.booksRead = booksRead;
    }

    public Map<String, Integer> getBooksByCountry() {
        return booksByCountry;
    }

    public void setBooksByCountry(Map<String, Integer> booksByCountry) {
        this.booksByCountry = booksByCountry;
    }

    public int getNewCountriesOpened() {
        return newCountriesOpened;
    }

    public void setNewCountriesOpened(int newCountriesOpened) {
        this.newCountriesOpened = newCountriesOpened;
    }


}
