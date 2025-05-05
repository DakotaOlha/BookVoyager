package com.example.bookvoyager;

import com.example.bookvoyager.Class.Book;

import java.util.ArrayList;
import java.util.List;

public class SortingBooks {
    public List<Book> sortByAuthors(List<Book> list) {
        if (list.size() <= 1) {
            return list;
        }

        Book pivot = list.get(list.size() / 2);
        List<Book> left = new ArrayList<>();
        List<Book> middle = new ArrayList<>();
        List<Book> right = new ArrayList<>();

        for (Book x : list) {
            if (x.getAuthor().compareTo(pivot.getAuthor()) < 0) {
                left.add(x);
            } else if (x.getAuthor().compareTo(pivot.getAuthor()) == 0) {
                middle.add(x);
            } else {
                right.add(x);
            }
        }

        List<Book> sorted = new ArrayList<>();
        sorted.addAll(sortByAuthors(left));
        sorted.addAll(middle);
        sorted.addAll(sortByAuthors(right));
        return sorted;
    }

    public List<Book> sortByTitle(List<Book> list) {
        if (list.size() <= 1) {
            return list;
        }

        Book pivot = list.get(list.size() / 2);
        List<Book> left = new ArrayList<>();
        List<Book> middle = new ArrayList<>();
        List<Book> right = new ArrayList<>();

        for (Book x : list) {
            if (x.getTitle().compareTo(pivot.getTitle()) < 0) {
                left.add(x);
            } else if (x.getTitle().compareTo(pivot.getTitle()) == 0) {
                middle.add(x);
            } else {
                right.add(x);
            }
        }

        List<Book> sorted = new ArrayList<>();
        sorted.addAll(sortByTitle(left));
        sorted.addAll(middle);
        sorted.addAll(sortByTitle(right));
        return sorted;
    }
}
