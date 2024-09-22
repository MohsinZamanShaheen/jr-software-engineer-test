package com.adobe.bookstore.service;

import com.adobe.bookstore.model.BookOrder;
import com.adobe.bookstore.model.BookStock;
import com.adobe.bookstore.repository.BookStockRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BookStockService {

    private BookStockRepository stockRepository;
    private static final Logger logger = LoggerFactory.getLogger(BookStockService.class);

    public BookStockService(BookStockRepository stockRepository){
        this.stockRepository = stockRepository;
    }

    /**
     * Check for sufficient stock of a specific book.
     */
    public boolean isSufficientStock(String bookId, int expectedQuantity){
        Optional<BookStock> bookStock = stockRepository.findById(bookId);
        if (bookStock.isPresent()){
            return bookStock.get().getQuantity() >= expectedQuantity;
        }
        return false;
    }
    /**
     * This function is encharged for updating the stock upon an order is made.
     */
    @Async
    public void updateStock(List<BookOrder> books) {
        for(BookOrder bookOrder : books) {
            try {
                stockRepository.findById(bookOrder.getBookId()).ifPresent(stock -> {
                    stock.setQuantity(stock.getQuantity() - bookOrder.getBookQuantity());
                    stockRepository.save(stock);
                });
            }catch(Exception e){
                logger.error("Failed to update stock for bookId: " + bookOrder.getBookId(), e);
            }
        }
    }
}