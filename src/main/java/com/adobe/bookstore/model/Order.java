package com.adobe.bookstore.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;


// Entity that represents an order composed by a list of books.
@Entity
@Table(name = "orders")
public class Order {

    @ElementCollection
    private List<BookOrder> books;
    private boolean orderSuccess;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID orderId;

    public Order() {
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
