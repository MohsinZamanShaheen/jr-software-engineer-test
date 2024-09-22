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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class BookOrderResource {

    private BookStockService bookStockService;

    @Autowired
    public BookOrderResource(BookStockService bookStockService) {
        this.bookStockService = bookStockService;
    }

    /**
     * This endpoint is used for the "New orders processing" feature
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Order order) {
        Map<String, Object> response = new HashMap<>();

        // Base case validation: check for empty orders
        if (order.getBooks() == null || order.getBooks().isEmpty()) {
            response.put("message", "The order cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }

        // Check sufficiency of stock. If no enough stock is available, reject the order with a message.
        for (BookOrder bookOrder : order.getBooks()) {
            if (!bookStockService.isSufficientStock(bookOrder.getBookId(), bookOrder.getBookQuantity())) {
                response.put("message", "Insufficient stock for book ID: " + bookOrder.getBookId());
                return ResponseEntity.badRequest().body(response);
            }
        }
        // if we have enough quantity then we'll fulfill the order and update the stock;
        order.setOrderSuccess(true);
        // Asynchronously update stock to avoid blocking replying to the customer.
        bookStockService.updateStock(order.getBooks());


        // Return the success message and the order object
        response.put("message", "Order successfully created");
        response.put("order", order);
        return ResponseEntity.ok(response);
    }
}
