package com.amalitech.smartecommerce.service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import com.amalitech.smartecommerce.model.Order;



public interface OrderService {
    Order deleteOrder(UUID id);

    Order updateOrder(Order order) throws SQLException;

    List<Order> getAllOrdersByUser(UUID user_id);

    Order createOrder(Order order) throws SQLException;

    // find an order by id
    Order getOrderById(UUID id);

    List<Order> getAllOrders();

    // Create an order and all its order lines in a single transaction. Returns the created Order on success.
    Order createOrderWithLines(Order order, java.util.List<com.amalitech.smartecommerce.model.OrderLine> orderLines) throws SQLException;
}
