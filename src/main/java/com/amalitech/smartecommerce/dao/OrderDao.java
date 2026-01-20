package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.controller.CustomerDashboardController;
import com.amalitech.smartecommerce.model.Order;
import java.util.List;
import java.util.UUID;


public interface OrderDao extends DAO<Order> {
     List<Order> getOrdersByUser(UUID user_id);
    List<CustomerDashboardController.OrderItemDetail> getOrderItemsForCustomer(UUID orderId);

    // Create an order transactionally with its order lines and update inventory
    Order createWithLines(Order order, List<com.amalitech.smartecommerce.model.OrderLine> orderLines) throws java.sql.SQLException;

    /**
     * Modify existing order lines transactionally: replace the order's lines with new ones and adjust inventory accordingly.
     * Caller is responsible for validating order state (e.g., pending) before invoking.
     */
    Order modifyOrderLines(UUID orderId, List<com.amalitech.smartecommerce.model.OrderLine> newOrderLines) throws java.sql.SQLException;

    /**
     * Retrieve raw OrderLine entries (including product_item_id and price) for a given order.
     */
    List<com.amalitech.smartecommerce.model.OrderLine> getOrderLinesRaw(UUID orderId);
 }
