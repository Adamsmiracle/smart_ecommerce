package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.Order;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderDaoImpl implements OrderDao {
    @Override
    public Order findUserOrderById(UUID id) {
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return orders;
    }

    @Override
    public boolean create(Order order) {
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
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Order order) {
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
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(UUID id) {
        Connection conn = null;
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

            // 2. Delete order lines for this order
            String deleteOrderLines = "DELETE FROM order_line WHERE order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrderLines)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 3. Delete the order
            String deleteOrder = "DELETE FROM customer_order WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrder)) {
                stmt.setObject(1, id);
                int result = stmt.executeUpdate();

                if (result > 0) {
                    conn.commit(); // Commit transaction
                    return true;
                } else {
                    conn.rollback(); // Rollback if order not found
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
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

