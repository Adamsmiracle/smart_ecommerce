package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.controller.CustomerDashboardController;
import com.amalitech.smartecommerce.model.Order;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderDaoImpl implements OrderDao {
    private static final Logger LOGGER = Logger.getLogger(OrderDaoImpl.class.getName());

    @Override
    public Order findById(UUID id) {
        String sql = "SELECT * FROM customer_order WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding Order by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM customer_order";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all orders", e);
        }
        return orders;
    }

    @Override
    public Order create(Order order) {
        String sql = "INSERT INTO customer_order (id, user_id, order_date, payment_method_id, shipping_address_id, shipping_method_id, order_total, order_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, order.getId());
            stmt.setObject(2, order.getUserId());
            stmt.setObject(3, order.getOrderDate());
            stmt.setObject(4, order.getPaymentMethodId());
            stmt.setObject(5, order.getShippingAddressId());
            stmt.setObject(6, order.getShippingMethodId());
            stmt.setObject(7, order.getOrderTotal());
            stmt.setObject(8, order.getOrderStatus());
            if (stmt.executeUpdate() > 0) {
                return order;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating order: " + order, e);
        }
        return null;
    }

    @Override
    public Order update(Order order) {
        String sql = "UPDATE customer_order SET user_id = ?, order_date = ?, payment_method_id = ?, shipping_address_id = ?, shipping_method_id = ?, order_total = ?, order_status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, order.getUserId());
            stmt.setObject(2, order.getOrderDate());
            stmt.setObject(3, order.getPaymentMethodId());
            stmt.setObject(4, order.getShippingAddressId());
            stmt.setObject(5, order.getShippingMethodId());
            stmt.setObject(6, order.getOrderTotal());
            stmt.setObject(7, order.getOrderStatus());
            stmt.setObject(8, order.getId());
            if (stmt.executeUpdate() > 0) {
                return order;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating order: " + order, e);
        }
        return null;
    }

    @Override
    public Order delete(UUID id) {
        Connection conn = null;
        Order orderToDelete = findById(id);
        if (orderToDelete == null) {
            return null;
        }
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Delete user reviews associated with order lines of this order
            // Note: user_review.ordered_product_id references order_line.id
            String deleteReviews = "DELETE FROM user_review WHERE ordered_product_id IN (SELECT id FROM order_line WHERE order_id = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(deleteReviews)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 2. Restore inventory qty for each order_line of this order
            String restoreQtySql = "UPDATE product_item SET qty_in_stock = qty_in_stock + ol.qty FROM order_line ol WHERE ol.product_item_id = product_item.id AND ol.order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(restoreQtySql)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Warning: failed to restore inventory quantities for order: " + id, ex);
                // proceed to delete to avoid leaving orphan orders; developer may choose to rollback instead
            }

            // Refresh InventoryCache for affected product_items
            try {
                com.amalitech.smartecommerce.cache.InventoryCache invCache = com.amalitech.smartecommerce.cache.InventoryCache.getInstance();
                String selectQtySql = "SELECT pi.id, pi.qty_in_stock FROM product_item pi JOIN order_line ol ON ol.product_item_id = pi.id WHERE ol.order_id = ?";
                try (PreparedStatement qStmt = conn.prepareStatement(selectQtySql)) {
                    qStmt.setObject(1, id);
                    try (ResultSet rs = qStmt.executeQuery()) {
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
                LOGGER.log(Level.WARNING, "Warning: failed to refresh inventory cache during order delete: " + id, ex);
            }

            // 3. Delete order lines for this order
            String deleteOrderLines = "DELETE FROM order_line WHERE order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrderLines)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }


            // 4. Delete the order
            String deleteOrder = "DELETE FROM customer_order WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrder)) {
                stmt.setObject(1, id);
                int result = stmt.executeUpdate();
                System.out.println("Deleted order return data: "+ result);

                if (result > 0) {
                    conn.commit(); // Commit transaction
                    return orderToDelete;
                } else {
                    conn.rollback(); // Rollback if order not found
                    return null;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting order: " + id, e);
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error during rollback for order delete: " + id, ex);
                }
            }
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error resetting autoCommit after order delete", e);
                }
            }
        }
    }



    @Override
    public List<CustomerDashboardController.OrderItemDetail> getOrderItemsForCustomer(UUID orderId) {
        List<CustomerDashboardController.OrderItemDetail> items = new ArrayList<>();

        String sql = """
            SELECT p.name AS product_name, ol.qty, ol.price
            FROM order_line ol
            JOIN product_item pi ON ol.product_item_id = pi.id
            JOIN product p ON pi.product_id = p.id
            WHERE ol.order_id = ?
            ORDER BY p.name
            """;

        try {
            java.sql.Connection conn = com.amalitech.smartecommerce.utils.DBConnection.getConnection();
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, orderId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String productName = rs.getString("product_name");
                        int qty = rs.getInt("qty");
                        double price = rs.getDouble("price");
                        items.add(new CustomerDashboardController.OrderItemDetail(
                                productName != null ? productName : "Unknown Product",
                                qty,
                                price
                        ));
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching order items: {0}", e.getMessage());
        }

        return items;
    }

    @Override
    public List<Order> getOrdersByUser(UUID user_id) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM customer_order WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, user_id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching orders for user: " + user_id, e);
        }
        return orders;

    }


    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        UUID userId = (UUID) rs.getObject("user_id");
        LocalDate orderDate = rs.getObject("order_date", LocalDate.class);
        UUID paymentMethodId = (UUID) rs.getObject("payment_method_id");
        UUID shippingAddressId = (UUID) rs.getObject("shipping_address_id");
        UUID shippingMethodId = (UUID) rs.getObject("shipping_method_id");
        Double orderTotal = rs.getObject("order_total") != null ? ((Number) rs.getObject("order_total")).doubleValue() : null;
        UUID orderStatus = (UUID) rs.getObject("order_status");
        return new Order(id, userId, orderDate, paymentMethodId, shippingAddressId, shippingMethodId, orderTotal, orderStatus);
    }
}

