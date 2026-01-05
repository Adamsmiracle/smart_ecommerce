package com.amalitech.smartecommerce.service;

import java.util.List;
import java.util.UUID;
import com.amalitech.smartecommerce.model.Order;



public interface OrderService {
    boolean deleteOrder(UUID id);

    boolean updateOrder(Order order);

    boolean createOrder(Order order);

    List<Order> getAllOrders();

    Order getOrderById(UUID id);
}


