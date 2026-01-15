package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.OrderDao;
import com.amalitech.smartecommerce.dao.OrderDaoImpl;
import com.amalitech.smartecommerce.model.Order;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;
    private static final Logger LOGGER = Logger.getLogger(OrderServiceImpl.class.getName());

    public OrderServiceImpl() {
        this.orderDao = new OrderDaoImpl();
    }

    public OrderServiceImpl(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    // find an order by id
    @Override
    public Order getOrderById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Provide order id");
        return orderDao.findById(id);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderDao.findAll();
    }


//    Get all order by the user id
    @Override
    public List<Order> getAllOrdersByUser(UUID user_id) {
        return orderDao.getOrdersByUser(user_id);
    }

    @Override
    public Order createOrder(Order order) throws SQLException {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        if (order.getUserId() == null) throw new IllegalArgumentException("Order must have a userId");
        if (order.getOrderTotal() == null || order.getOrderTotal() < 0) throw new IllegalArgumentException("Order total must be non-negative");

        // ensure id and orderDate are set
        if (order.getId() == null) order.setId(UUID.randomUUID());
        if (order.getOrderDate() == null) order.setOrderDate(LocalDate.now());

        return orderDao.create(order);
    }

    @Override
    public Order createOrderWithLines(Order order, java.util.List<com.amalitech.smartecommerce.model.OrderLine> orderLines) throws SQLException {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        if (orderLines == null) throw new IllegalArgumentException("Order lines cannot be null");

        // ensure id and orderDate are set
        if (order.getId() == null) order.setId(UUID.randomUUID());
        if (order.getOrderDate() == null) order.setOrderDate(LocalDate.now());

        java.sql.Connection conn = null;
        try {
            conn = com.amalitech.smartecommerce.utils.DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Create order
            String insertOrderSql = "INSERT INTO customer_order (id, user_id, order_date, payment_method_id, shipping_address_id, shipping_method_id, order_total, order_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement orderStmt = conn.prepareStatement(insertOrderSql)) {
                orderStmt.setObject(1, order.getId());
                orderStmt.setObject(2, order.getUserId());
                orderStmt.setObject(3, order.getOrderDate());
                orderStmt.setObject(4, order.getPaymentMethodId());
                orderStmt.setObject(5, order.getShippingAddressId());
                orderStmt.setObject(6, order.getShippingMethodId());
                orderStmt.setObject(7, order.getOrderTotal());
                orderStmt.setObject(8, order.getOrderStatus());
                orderStmt.executeUpdate();
            }

            // Insert each order line and decrement stock in the same connection/transaction
            String insertLineSql = "INSERT INTO order_line (id, product_item_id, order_id, qty, price) VALUES (?, ?, ?, ?, ?)";
            String decStockSql = "UPDATE product_item SET qty_in_stock = qty_in_stock - ? WHERE id = ? AND qty_in_stock >= ?"; // ensure available stock

            for (com.amalitech.smartecommerce.model.OrderLine ol : orderLines) {
                if (ol.getId() == null) ol.setId(UUID.randomUUID());
                ol.setOrderId(order.getId());

                try (java.sql.PreparedStatement lineStmt = conn.prepareStatement(insertLineSql)) {
                    lineStmt.setObject(1, ol.getId());
                    lineStmt.setObject(2, ol.getProductItemId());
                    lineStmt.setObject(3, ol.getOrderId());
                    lineStmt.setInt(4, ol.getQty());
                    lineStmt.setObject(5, ol.getPrice());
                    lineStmt.executeUpdate();
                }

                // decrement stock only if there is sufficient stock
                try (java.sql.PreparedStatement decStmt = conn.prepareStatement(decStockSql)) {
                    decStmt.setInt(1, ol.getQty());
                    decStmt.setObject(2, ol.getProductItemId());
                    decStmt.setInt(3, ol.getQty());
                    int rows = decStmt.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Insufficient stock for product_item: " + ol.getProductItemId());
                    }
                }
            }

            conn.commit();

            // Refresh InventoryCache entries for affected product_item ids
            try {
                com.amalitech.smartecommerce.cache.InventoryCache invCache = com.amalitech.smartecommerce.cache.InventoryCache.getInstance();
                // Build IN clause
                StringBuilder inClause = new StringBuilder();
                for (int i = 0; i < orderLines.size(); i++) {
                    if (i > 0) inClause.append(",");
                    inClause.append("?");
                }
                String selectQtySql = "SELECT id, qty_in_stock FROM product_item WHERE id IN (" + inClause + ")";
                try (java.sql.PreparedStatement qStmt = conn.prepareStatement(selectQtySql)) {
                    int idx = 1;
                    for (com.amalitech.smartecommerce.model.OrderLine ol : orderLines) {
                        qStmt.setObject(idx++, ol.getProductItemId());
                    }
                    try (java.sql.ResultSet rs = qStmt.executeQuery()) {
                        while (rs.next()) {
                            java.util.UUID pid = (java.util.UUID) rs.getObject("id");
                            int qty = rs.getInt("qty_in_stock");
                            if (invCache.containsProductId(pid)) {
                                invCache.updateQuantity(pid, qty);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // non-fatal: log
                LOGGER.log(Level.WARNING, "Failed to refresh inventory cache after order creation: {0}", ex.getMessage());
            }

            return order;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed: {0}", ex.getMessage()); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Failed to close connection: {0}", ex.getMessage()); }
            }
        }
    }

    @Override
    public Order updateOrder(Order order) throws SQLException {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        if (order.getId() == null) throw new IllegalArgumentException("Order id is required for update");
        if (order.getOrderTotal() == null || order.getOrderTotal() < 0) throw new IllegalArgumentException("Order total must be non-negative");
        return orderDao.update(order);
    }

    @Override
    public Order deleteOrder(UUID id) {
        if (id == null) throw new IllegalArgumentException("Order id cannot be null");
        return orderDao.delete(id);
    }
}
