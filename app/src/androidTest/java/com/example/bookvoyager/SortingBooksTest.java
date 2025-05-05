package com.example.bookvoyager;

import com.example.bookvoyager.Class.Book;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SortingBooksTest {

    private SortingBooks sortingBooks;
    private List<Book> bookList;

    @Before
    public void setUp() {
        sortingBooks = new SortingBooks();
        bookList = new ArrayList<>();
        bookList.add(new Book("The Hobbit", "Tolkien"));
        bookList.add(new Book("A Tale of Two Cities", "Dickens"));
        bookList.add(new Book("Pride and Prejudice", "Austen"));
        bookList.add(new Book("Hamlet", "Shakespeare"));
        bookList.add(new Book("1984", "Orwell"));
    }

    @Test
    public void testSortByAuthors() {
        List<Book> sorted = sortingBooks.sortByAuthors(bookList);

        assertEquals("Austen", sorted.get(0).getAuthor());
        assertEquals("Dickens", sorted.get(1).getAuthor());
        assertEquals("Orwell", sorted.get(2).getAuthor());
        assertEquals("Shakespeare", sorted.get(3).getAuthor());
        assertEquals("Tolkien", sorted.get(4).getAuthor());
    }

    @Test
    public void testSortByTitle() {
        List<Book> sorted = sortingBooks.sortByTitle(bookList);

        assertEquals("1984", sorted.get(0).getTitle());
        assertEquals("A Tale of Two Cities", sorted.get(1).getTitle());
        assertEquals("Hamlet", sorted.get(2).getTitle());
        assertEquals("Pride and Prejudice", sorted.get(3).getTitle());
        assertEquals("The Hobbit", sorted.get(4).getTitle());
    }

    @Test
    public void testSortByAuthors_emptyList() {
        List<Book> sorted = sortingBooks.sortByAuthors(new ArrayList<>());
        assertTrue(sorted.isEmpty());
    }

    @Test
    public void testSortByTitle_emptyList() {
        List<Book> sorted = sortingBooks.sortByTitle(new ArrayList<>());
        assertTrue(sorted.isEmpty());
    }

    @Test
    public void testSortByAuthors_singleElement() {
        List<Book> single = List.of(new Book("Dune", "Herbert"));
        List<Book> sorted = sortingBooks.sortByAuthors(single);
        assertEquals("Herbert", sorted.get(0).getAuthor());
    }

    @Test
    public void testSortByTitle_singleElement() {
        List<Book> single = List.of(new Book("Dune", "Herbert"));
        List<Book> sorted = sortingBooks.sortByTitle(single);
        assertEquals("Dune", sorted.get(0).getTitle());
    }

    @Test
    public void testSortByAuthors_withDuplicates() {
        bookList.add(new Book("Duplicate Book", "Austen"));
        List<Book> sorted = sortingBooks.sortByAuthors(bookList);

        assertEquals("Austen", sorted.get(0).getAuthor());
        assertEquals("Austen", sorted.get(1).getAuthor());
    }

    @Test
    public void testSortByTitle_withDuplicates() {
        bookList.add(new Book("Hamlet", "Another Author"));
        List<Book> sorted = sortingBooks.sortByTitle(bookList);

        assertEquals("1984", sorted.get(0).getTitle());
        assertEquals("A Tale of Two Cities", sorted.get(1).getTitle());
        assertEquals("Hamlet", sorted.get(2).getTitle());
        assertEquals("Hamlet", sorted.get(3).getTitle());
    }

}
