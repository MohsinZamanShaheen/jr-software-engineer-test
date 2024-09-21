package com.adobe.bookstore.model;

public class BookOrder {

    private String bookId;
    private int bookQuantity;

    public BookOrder(String id, int quantity){
        this.bookId = id;
        this.bookQuantity = quantity;
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
