package com.example.controller;

import org.junit.runner.RunWith;
import org.junit.AfterClass;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.TestRestTemplate;

import com.example.DemoApplication;
import com.example.repository.BookRepository;
import com.example.model.Book;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.*;
import java.io.File;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import static org.junit.Assert.*;
import org.junit.Test;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoApplication.class)
@WebIntegrationTest
public class BookControllerTest {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private BookRepository bookRepository = new BookRepository();
    private RestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void testCreateBookApi() throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("name", "Book 1");
        requestBody.put("isbn", "QWER1234");
        requestBody.put("author", "Author 1");
        requestBody.put("pages", 200);
        
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> httpEntity = new HttpEntity<String>(OBJECT_MAPPER.writeValueAsString(requestBody), requestHeaders);


        Map<String, Object> apiResponse =
          restTemplate.postForObject("http://localhost:8888/book", httpEntity, Map.class, Collections.EMPTY_MAP);

        assertNotNull(apiResponse);

        String message = apiResponse.get("message").toString();
        assertEquals("Book created successfully", message);

        String bookId = ((Map<String, Object>)apiResponse.get("book")).get("id").toString();
        assertNotNull(bookId);

        Book bookFromDb = bookRepository.findOne(bookId);
        assertEquals("Book 1", bookFromDb.getName());
        assertEquals("QWER1234", bookFromDb.getIsbn());
        assertEquals("Author 1", bookFromDb.getAuthor());
        assertTrue(200 == bookFromDb.getPages());


        bookRepository.delete(bookId);
    }
        
        
    @Test
    public void testGetBookDetailsApi(){
        Book book = new Book("Book1", "ÏSBN1", "Author1", 200);
        book.setId(new Random(100).nextInt() + "");
        bookRepository.save(book);

        String bookId = book.getId();

        Book apiResponse = restTemplate.getForObject("http://localhost:8888/book/"+ bookId, Book.class);

        assertNotNull(apiResponse);
        assertEquals(book.getName(), apiResponse.getName());
        assertEquals(book.getId(), apiResponse.getId());
        assertEquals(book.getIsbn(), apiResponse.getIsbn());
        assertEquals(book.getAuthor(), apiResponse.getAuthor());
        assertTrue(book.getPages() == apiResponse.getPages());

        bookRepository.delete(bookId);
    }
    
    @AfterClass
    public static void doCleanup() {
        File tmpFolder = new File("./all_books/");
        String [] entries = tmpFolder.list();
        for(String s: entries){
            File currentFile = new File(tmpFolder.getPath(), s);
            currentFile.delete();
        }
        tmpFolder.delete();
    }

}

