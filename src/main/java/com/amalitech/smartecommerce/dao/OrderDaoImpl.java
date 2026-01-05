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
    public boolean insert(Order order) {
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
        String sql = "DELETE FROM customer_order WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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

