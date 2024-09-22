package com.adobe.bookstore.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class BookOrder {

    private String bookId;
    private int bookQuantity;

    public BookOrder() {}

    public BookOrder(String bookId, int bookQuantity) {
        this.bookId = bookId;
        this.bookQuantity = bookQuantity;
    }
    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getBookQuantity() {
        return bookQuantity;
    }

    public void setBookQuantity(int bookQuantity) {
        this.bookQuantity = bookQuantity;
    }
}
