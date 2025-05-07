package com.example.bookvoyager.Class;

import java.util.HashMap;
import java.util.Map;

public class UserStats {
    private static UserStats instance;

    private int booksAdded;
    private int booksRead;
    private Map<String, Integer> booksByCountry;
    private int countriesOpened;

    private UserStats() {
        booksByCountry = new HashMap<>();
    }

    public static synchronized UserStats getInstance() {
        if (instance == null) {
            instance = new UserStats();
        }
        return instance;
    }

    public void addCountry(String name){
        if (booksByCountry.containsKey(name)) {
            booksByCountry.put(name, booksByCountry.get(name) + 1);
        } else {
            booksByCountry.put(name, 1);
            countriesOpened += 1;
        }
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

    public int getCountriesOpened() {
        return countriesOpened;
    }

    public void setCountriesOpened(int newCountriesOpened) {
        this.countriesOpened = newCountriesOpened;
    }
}