package com.adobe.bookstore.web;

import com.adobe.bookstore.repository.BookStockRepository;
import com.adobe.bookstore.model.BookStock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books_stock/")
public class BookStockResource {

    private BookStockRepository bookStockRepository;

    @Autowired
    public BookStockResource(BookStockRepository bookStockRepository) {
        this.bookStockRepository = bookStockRepository;
    }
    @GetMapping("{bookId}")
    public ResponseEntity<BookStock> getStockById(@PathVariable String bookId) {
        return bookStockRepository.findById(bookId)
                .map(bookStock -> ResponseEntity.ok(bookStock))
                .orElse(ResponseEntity.notFound().build());
    }
}
