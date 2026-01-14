package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.OrderStatus;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderStatusDaoImpl implements OrderStatusDao {

    @Override
    public OrderStatus findById(UUID id) {
        String sql = "SELECT * FROM order_status WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrderStatus(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<OrderStatus> findAll() {
        List<OrderStatus> statuses = new ArrayList<>();
        String sql = "SELECT * FROM order_status";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                statuses.add(mapResultSetToOrderStatus(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statuses;
    }

    @Override
    public OrderStatus findByStatus(String status) {
        String sql = "SELECT * FROM order_status WHERE LOWER(status) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrderStatus(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public OrderStatus create(OrderStatus orderStatus) {
        String sql = "INSERT INTO order_status (id, status) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            UUID id = orderStatus.getId() != null ? orderStatus.getId() : UUID.randomUUID();
            stmt.setObject(1, id);
            stmt.setString(2, orderStatus.getStatus());
            if (stmt.executeUpdate() > 0) {
                orderStatus.setId(id);
                return orderStatus;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public OrderStatus update(OrderStatus orderStatus) {
        String sql = "UPDATE order_status SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderStatus.getStatus());
            stmt.setObject(2, orderStatus.getId());
            if (stmt.executeUpdate() > 0) {
                return orderStatus;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public OrderStatus delete(UUID id) {
        OrderStatus statusToDelete = findById(id);
        if (statusToDelete == null) {
            return null;
        }
        String sql = "DELETE FROM order_status WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            if (stmt.executeUpdate() > 0) {
                return statusToDelete;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private OrderStatus mapResultSetToOrderStatus(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String status = rs.getString("status");
        return new OrderStatus(id, status);
    }
}

