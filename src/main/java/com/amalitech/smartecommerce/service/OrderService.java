package com.amalitech.smartecommerce.service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.amalitech.smartecommerce.controller.CustomerDashboardController;
import com.amalitech.smartecommerce.model.Order;



public interface OrderService {
    Order deleteOrder(UUID id);

    Order updateOrder(Order order) throws SQLException;

    List<Order> getAllOrdersByUser(UUID user_id);

    // find an order by id
    Order getOrderById(UUID id);

     List<Order> getAllOrders();

     // Create an order and all its order lines in a single transaction. Returns the created Order on success.
     Order createOrderWithLines(Order order, java.util.List<com.amalitech.smartecommerce.model.OrderLine> orderLines) throws SQLException;
     List<CustomerDashboardController.OrderItemDetail> getOrderItems(UUID order_id);

    /**
     * Modify an existing order's order lines (only allowed for editable states such as Pending).
     * This will update order lines, adjust inventory (increment/decrement) and update order total transactionally.
     */
    Order modifyOrderLines(UUID orderId, java.util.List<com.amalitech.smartecommerce.model.OrderLine> newOrderLines) throws SQLException;

    /**
     * Retrieve raw order lines (with product_item_id and price) for use by UI or services.
     */
    List<com.amalitech.smartecommerce.model.OrderLine> getOrderLinesRaw(UUID orderId);
 }
