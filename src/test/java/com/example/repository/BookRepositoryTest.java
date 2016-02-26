package com.example.controller;

import com.example.repository.BookRepository;
import com.example.model.Book;

import java.util.*;

import static org.junit.Assert.*;
import org.junit.Test;

public class BookRepositoryTest {

    private BookRepository bookRepository = new BookRepository();

    @Test
    public void testBasicFacilities() {

        //Create a new book using the BookRepository API
        Book book = new Book("Book 1", "ÏSBN1", "Author 1", 200);
        book.setId(new Random(100).nextInt() + "");
        bookRepository.save(book);

        String bookId = book.getId();



        Book bookFromDb = bookRepository.findOne(bookId);
        assertEquals("Book 1", bookFromDb.getName());
        assertEquals("ÏSBN1", bookFromDb.getIsbn());
        assertEquals("Author 1", bookFromDb.getAuthor());
        assertTrue(200 == bookFromDb.getPages());


        //Delete the data added for testing
        bookRepository.delete(bookId);
    }
}

