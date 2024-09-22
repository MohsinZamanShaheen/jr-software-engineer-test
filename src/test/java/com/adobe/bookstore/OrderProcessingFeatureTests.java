package com.adobe.bookstore;

import com.adobe.bookstore.model.Order;
import com.adobe.bookstore.model.BookOrder;
import com.adobe.bookstore.model.BookStock;
import com.adobe.bookstore.service.BookStockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderProcessingFeatureTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookStockService bookStockService;

    /*
    *   THIS FILE CONTAINS TESTS FROM DIFFERENT SCOPES IN ORDER TO MAKE SURE THE IMPLEMENTED FEATURE(PROCESSING ORDERS) WORKS IN
    *   ALL POSSIBLE SCENARIOS.
    *   WE HAVE MAINLY:
    *   1- UNIT TESTS
    *   2- INTEGRATION TESTS
    *   3- A TEST TO MAKE SURE STOCK UPDATE IN THE BACKGROUND AND REPLY TO CUSTUMER IS NON-BLOCKING.
     */


    // =======================
    // Unit Tests
    // =======================

    /**
     * Test to verify that sufficient stock is available for a specific book.
     */
    @Test
    @Sql(statements = "INSERT INTO book_stock (id, name, quantity) VALUES ('43782-28491', 'some book 1', 10)")
    public void testIsStockSufficient_SufficientStock() {
        boolean isSufficient = bookStockService.isSufficientStock("43782-28491", 5);
        assertThat(isSufficient).isTrue();
    }

    /**
     * Test to check if the stock is insufficient for a higher quantity.
     */
    @Test
    @Sql(statements = "INSERT INTO book_stock (id, name, quantity) VALUES ('62947-73821', 'some book 2', 10)")
    public void testIsStockSufficient_InsufficientStock() {
        // Verify that stock is insufficient for the requested quantity.
        boolean isSufficient = bookStockService.isSufficientStock("62947-73821", 20);
        assertThat(isSufficient).isFalse();
    }

    /**
     * Test to verify that the stock is updated after a successful order.
     */
    @Test
    @Sql(statements = "INSERT INTO book_stock (id, name, quantity) VALUES ('34871-19285', 'some book 3', 18)")
    public void testUpdateStock_Success() throws InterruptedException {
        // Update the stock for a specific book ID and verify it has been reduced accordingly.
        CountDownLatch latch = new CountDownLatch(1);
        bookStockService.updateStock(List.of(new BookOrder("34871-19285", 5)));
        latch.await(500, TimeUnit.MILLISECONDS);

        // Check that stock is reduced after the order.
        var stockResult = restTemplate.getForObject("http://localhost:" + port + "/books_stock/34871-19285", BookStock.class);
        assertThat(stockResult.getQuantity()).isEqualTo(13);
    }

    // =======================
    // Integration Tests
    // =======================

    /**
     * Test for successfully creating an order with sufficient stock.
     */
    @Test
    @Sql(statements = "INSERT INTO book_stock (id, name, quantity) VALUES ('94632-50871', 'some book 4', 10)")
    public void testCreateOrderSuccess() {
        Order order = new Order();
        order.setBooks(List.of(new BookOrder("94632-50871", 1)));

        Map<String, Object> response = restTemplate.postForObject(
                "http://localhost:" + port + "/orders/create",
                order,
                Map.class
        );
        assertThat(response).containsEntry("message", "Order successfully created");

        // Extract the order object from the response and verify if properties are as expected to consider order as successful.
        ObjectMapper objectMapper = new ObjectMapper();
        Order responseOrder = objectMapper.convertValue(response.get("order"), Order.class);
        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getOrderId()).isNotNull();
        assertThat(responseOrder.isOrderSuccess()).isTrue();
    }

    /**
     * Test for failing order creation due to insufficient stock.
     */
    @Test
    @Sql(statements = "INSERT INTO book_stock (id, name, quantity) VALUES ('57362-14983', 'some book 5', 10)")
    public void testCreateOrderFail_InsufficientStock() {
        Order order = new Order();
        order.setBooks(List.of(new BookOrder("57362-14983", 100)));

        Map<String, Object> response = restTemplate.postForObject(
                "http://localhost:" + port + "/orders/create",
                order,
                Map.class
        );
        // Ensure failure due to insufficient stock.
        assertThat(response).containsEntry("message", "Insufficient stock for book ID: 57362-14983");
        assertThat(response.get("order")).isNull();
    }

    /**
     * Test for failing order creation due to an empty order.
     */
    @Test
    public void testCreateOrderFail_EmptyOrder() {
        Order order = new Order();
        order.setBooks(List.of());
        Map<String, Object> response = restTemplate.postForObject(
                "http://localhost:" + port + "/orders/create",
                order,
                Map.class
        );
        assertThat(response).containsEntry("message", "The order cannot be empty");
        assertThat(response.get("order")).isNull();
    }

    // =======================
    // Asynchronous Stock Update Test
    // =======================

    /**
     * Test to verify stock is updated asynchronously after an order is placed.
     */
    @Test
    @Sql(statements = "INSERT INTO book_stock (id, name, quantity) VALUES ('26384-51972', 'some book 7', 10)")
    public void testAsyncStockUpdate() throws InterruptedException {
        // Create an order and verify async stock update.
        Order order = new Order();
        order.setBooks(List.of(new BookOrder("26384-51972", 1)));

        Map<String, Object> response = restTemplate.postForObject(
                "http://localhost:" + port + "/orders/create",
                order,
                Map.class
        );

        assertThat(response.get("message")).isEqualTo("Order successfully created");

        ObjectMapper objectMapper = new ObjectMapper();
        Order responseOrder = objectMapper.convertValue(response.get("order"), Order.class);
        // Assert that the order was successfully created.
        assertThat(responseOrder).isNotNull();
        assertThat(responseOrder.getOrderId()).isNotNull();
        assertThat(responseOrder.isOrderSuccess()).isTrue();

        // Wait for asynchronous stock update to complete.
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(500, TimeUnit.MILLISECONDS);

        // Verify that stock has been updated after order creation.
        var stockResult = restTemplate.getForObject("http://localhost:" + port + "/books_stock/26384-51972", BookStock.class);
        assertThat(stockResult.getQuantity()).isEqualTo(9);
    }
}
