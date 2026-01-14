package com.amalitech.smartecommerce.service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import com.amalitech.smartecommerce.model.Order;



public interface OrderService {
    Order deleteOrder(UUID id);

    Order updateOrder(Order order) throws SQLException;

    Order createOrder(Order order) throws SQLException;

    List<Order> getAllOrders();

    Order getOrderById(UUID id);
}


