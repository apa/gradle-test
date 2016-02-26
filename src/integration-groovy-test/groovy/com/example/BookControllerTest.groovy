package com.example.controller

import org.junit.runner.RunWith
import org.junit.AfterClass
import org.junit.Before
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.web.client.RestTemplate
import org.springframework.boot.test.TestRestTemplate

import com.example.DemoApplication
import com.example.repository.BookRepository
import com.example.model.Book

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonProcessingException

import java.util.*
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType

import groovy.json.JsonBuilder

import static org.junit.Assert.*
import org.junit.Test

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoApplication.class)
@WebIntegrationTest
public class BookControllerTest {


    private BookRepository bookRepository = new BookRepository()
    private RestTemplate restTemplate = new TestRestTemplate()

    @Before
    public void init () {
            Book.metaClass.asSimpleMap = asSimpleMapClz
            Book.metaClass.asMap = asMapClz
    }
    
    @Test
    public void testCreateBookApi() {
        Map<String, Object> requestBody = 
            ["name" : "Book 1", 
            "isbn" : "QWER1234", 
            "author" : "Author 1", 
            "pages" : 200]
            
        HttpHeaders requestHeaders = new HttpHeaders()
        requestHeaders.setContentType(MediaType.APPLICATION_JSON)

        HttpEntity<String> httpEntity = new HttpEntity<String>(new JsonBuilder(requestBody).toPrettyString(), requestHeaders)

        Map<String, Object> apiResponse =
          restTemplate.postForObject("http://localhost:8888/book", httpEntity, Map.class, Collections.EMPTY_MAP)
        assertNotNull(apiResponse)

        assertEquals("Book created successfully", apiResponse.message)

        def bookId = apiResponse.book.id
        assertNotNull(bookId)

        Book bookFromDb = bookRepository.findOne(bookId)
        assertEquals(requestBody, bookFromDb.asSimpleMap())
        
        bookRepository.delete(bookId)
    }
        
        
    @Test
    public void testGetBookDetailsApi(){
        Book book = new Book("Book1", "ÏSBN1", "Author1", 200)
        
        book.asSimpleMap()
        
        book.setId(new Random(100).nextInt() + "")
        bookRepository.save(book)

        Book apiResponse = restTemplate.getForObject("http://localhost:8888/book/"+ book.id, Book.class)
        assertNotNull(apiResponse)
        
        assertEquals(book.asMap(), apiResponse.asMap())

        bookRepository.delete(book.id)
    }
    
    @AfterClass
    public static void doCleanup() {
        File tmpFolder = new File("./all_books/")
        tmpFolder.deleteDir()
    }
    
    def asSimpleMapClz = {
        delegate.class.declaredFields.findAll { (!it.synthetic && (it.name != "id")) }.collectEntries {
                [ (it.name):delegate."$it.name" ]
            }
    }
    
    def asMapClz = {
        delegate.class.declaredFields.findAll { !it.synthetic }.collectEntries {
                [ (it.name):delegate."$it.name" ]
            }
    }
    
    
}

