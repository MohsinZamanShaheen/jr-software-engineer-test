package com.adobe.bookstore.service;

import com.adobe.bookstore.model.Order;
import com.adobe.bookstore.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * This method saves an order in the Database.
     */
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    /**
     * This method is to simply fetch all orders from the Database.
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}

