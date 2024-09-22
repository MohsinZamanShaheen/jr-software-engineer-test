package com.adobe.bookstore;

import com.adobe.bookstore.model.BookOrder;
import com.adobe.bookstore.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderRetrievalFeatureTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Test to check if the orders retrieval feature works as expected.
     */
    @Test
    @Sql(statements = "INSERT INTO book_stock (id, name, quantity) VALUES ('177345-67890', 'Book 1', 10)")
    public void testCreateAndRetrieveOrder() {

        //1. Create order by posting to /orders/create
        Order order = new Order();
        order.setBooks(List.of(new BookOrder("177345-67890", 2)));

        Map<String, Object> createResponse = restTemplate.postForObject(
                "http://localhost:" + port + "/orders/create",
                order,
                Map.class
        );
        // Verification of order creation
        assertThat(createResponse).containsEntry("message", "Order successfully created");
        assertThat(createResponse.get("order")).isNotNull();

        // Extract the orderId from the created order to filter afterwards if requiered.
        String createdOrderId = (String) ((Map<String, Object>) createResponse.get("order")).get("orderId");

        //2 Retrieve the list of orders
        List<Map<String, Object>> retrieveResponse = restTemplate.getForObject(
                "http://localhost:" + port + "/orders",
                List.class
        );
        // Verify the list contains at least one order
        assertThat(retrieveResponse).isNotEmpty();

        //3. Check the number of orders.
        Map<String, Object> retrievedOrder;
        if (retrieveResponse.size() == 1) {
            // If only one order, it's the one we created
            retrievedOrder = retrieveResponse.get(0);
        } else {
            // If more than one order, filter by the created orderId
            retrievedOrder = retrieveResponse.stream()
                    .filter(orderMap -> createdOrderId.equals(orderMap.get("orderId")))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Order not found"));
        }
        // Verify the properties of the retrieved order
        assertThat(retrievedOrder.get("orderId")).isEqualTo(createdOrderId);
        assertThat(retrievedOrder.get("orderSuccess")).isEqualTo(true);
    }

}
