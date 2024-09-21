package com.adobe.bookstore.model;

import java.util.List;
import java.util.UUID;


// Class that represents an order composed by a list of books.
public class Order {

    private List<BookOrder> books;
    private boolean orderSuccess;
    private UUID orderId;

    public Order() {
        this.orderId = UUID.randomUUID();
    }

    public List<BookOrder> getBooks() {
        return books;
    }

    public void setBooks(List<BookOrder> books) {
        this.books = books;
    }

    public boolean isOrderSuccess() {
        return orderSuccess;
    }

    public void setOrderSuccess(boolean orderSuccess) {
        this.orderSuccess = orderSuccess;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
