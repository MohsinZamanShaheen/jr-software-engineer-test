package com.adobe.bookstore.web;

import com.adobe.bookstore.model.BookOrder;
import com.adobe.bookstore.model.Order;
import com.adobe.bookstore.service.BookStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class BookOrderResource {

    private BookStockService bookStockService;

    @Autowired
    public BookOrderResource(BookStockService bookStockService) {
        this.bookStockService = bookStockService;
    }

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody Order order){
        // Check sufficiency of stock. If not, reject the order.
        for(BookOrder bookOrder : order.getBooks()){
            if(!bookStockService.isSufficientStock(bookOrder.getBookId(), bookOrder.getBookQuantity())){
                return ResponseEntity.badRequest().body(null);
            }
        }
        // if we have enough quantity then we'll fulfill the order and update the stock;
        order.setOrderSuccess(true);
        bookStockService.updateStock(order.getBooks());

        return ResponseEntity.ok(order);
    }
}
