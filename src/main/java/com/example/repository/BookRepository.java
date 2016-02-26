package com.example.repository;

import com.example.model.Book;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;


public class BookRepository {

    private File bookStoreFolder = new File("./all_books/");
    private Properties props = new Properties();
    
    public BookRepository() {
        if (!bookStoreFolder.exists()) {
            bookStoreFolder.mkdir();
        }
        System.out.println("Full path to folder with books: " + bookStoreFolder.getAbsolutePath());
    }
    
    public Book save(Book book) {
        book = storeBookIntoFile(book);
        System.out.println("!!!repo: saved book with id: " + book.getId());
        System.out.println("!!!repo: now all book id's: " + Arrays.toString(bookStoreFolder.list()));
        return book;
    }
    
    public Book findOne(String id) {
        System.out.println("!!!repo: requested book with id: " + id + ". All ids: " + Arrays.toString(bookStoreFolder.list()));
        return getBookFromFile(id);
    }
    
    public List<Book> findAll () {
        List allBooks = new ArrayList();
        for (String fileName : bookStoreFolder.list()) {
            String id = fileName.substring(0, fileName.length() - 4);
            System.out.println("!!!repo: converting filename: [" + fileName + "] into id: [" + id + "]");
            Book book = getBookFromFile(id);
            allBooks.add(book);
        }
        return allBooks;
    }
    
    public Book delete(String id) {
        File bookFile = new File(bookStoreFolder, id + ".book");
        Book book = null;
        if (bookFile.exists()) {
            book = getBookFromFile(id);
            bookFile.delete();
        }        
        return book;
    }
    
    
    private Book storeBookIntoFile(Book book) {
        if (book == null) {
            System.out.println("!!!repo: not able to store NULL as book");
            return null;
        }
        String id = book.getId();
        
        if ((null == id) || "".equals(id)) {
            System.out.println("!!!repo: not able to store book with null or empty id");
            return null;
        }
        
        File bookFile = new File(bookStoreFolder, book.getId() + ".book");
        if (bookFile.exists()) {
            bookFile.delete();
        }
        
        try {
            FileOutputStream fout = new FileOutputStream(bookFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(book);
            oos.close();
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
        return book;
    }
    
    private Book getBookFromFile(String id) {
        File bookFile = new File(bookStoreFolder, id + ".book");
        if (bookFile.exists()) {
            Book book = null;
            try {
                FileInputStream fin = new FileInputStream(bookFile);
                ObjectInputStream ois = new ObjectInputStream(fin);
                book = (Book) ois.readObject();
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
            return book;
        } else {
            return null;
        }
    }
}

