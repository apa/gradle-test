package com.example.controller;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.Book;
import com.example.repository.BookRepository;

@RestController
@RequestMapping("/book")
public class BookController {

  private BookRepository bookRepository = new BookRepository();

  @RequestMapping(method = RequestMethod.POST)
  public Map<String, Object> createBook(@RequestBody Map<String, Object> bookMap){
    Book book = new Book(bookMap.get("name").toString(),
        bookMap.get("isbn").toString(),
        bookMap.get("author").toString(),
        Integer.parseInt(bookMap.get("pages").toString()));
    
    String id = new Random(100).nextInt() + "";
    System.out.println("!!! Creating book with id: " + id); 
    book.setId(id);
    book = bookRepository.save(book);
    Map<String, Object> response = new LinkedHashMap<String, Object>();
    response.put("message", "Book created successfully");
    response.put("book", book);
    return response;
  }

  @RequestMapping(method = RequestMethod.GET, value="/{bookId}")
  public Book getBookDetails(@PathVariable("bookId") String bookId){
    Book book = bookRepository.findOne(bookId);
    if (null == book) {
        throw new IllegalStateException("Book not found");
    }
    return book;
  }
  
  @RequestMapping(method = RequestMethod.PUT, value="/{bookId}")
  public Map<String, Object> editBook(@PathVariable("bookId") String bookId, @RequestBody Map<String, Object> bookMap){
    Book book = new Book(bookMap.get("id").toString(),
    bookMap.get("isbn").toString(),
    bookMap.get("author").toString(),
    Integer.parseInt(bookMap.get("pages").toString()));

    Map<String, Object> response = new LinkedHashMap<String, Object>();
    response.put("message", "Book Updated successfully");
    response.put("book", bookRepository.save(book));
    return response;
  }
  
  
    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> getAllBooks(){
      List<Book> books = bookRepository.findAll();
      Map<String, Object> response = new LinkedHashMap<String, Object>();
      response.put("totalBooks", books.size());
      response.put("books", books);
      return response;
    }
    
    
    @RequestMapping(method = RequestMethod.DELETE, value="/{bookId}")
    public Map<String, String> deleteBook(@PathVariable("bookId") String bookId){
        Book deletedBook = bookRepository.delete(bookId);
        Map<String, String> response = new HashMap<String, String>();
        if (null != deletedBook) {
            response.put("message", "Book deleted successfully");
        } else {
            response.put("message", "Something happens during deleting of book");
        }
        return response;
    }


}

