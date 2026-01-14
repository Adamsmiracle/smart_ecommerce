package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.OrderLine;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderLineDaoImpl implements OrderLineDao {
    @Override
    public OrderLine findById(UUID id) {
        String sql = "SELECT * FROM order_line WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrderLine(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<OrderLine> findAll() {
        List<OrderLine> lines = new ArrayList<>();
        String sql = "SELECT * FROM order_line";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lines.add(mapResultSetToOrderLine(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lines;
    }

    @Override
    public List<OrderLine> findByOrderId(UUID orderId) {
        List<OrderLine> lines = new ArrayList<>();
        String sql = "SELECT * FROM order_line WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lines.add(mapResultSetToOrderLine(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lines;
    }

    @Override
    public OrderLine create(OrderLine orderLine) {
        String sql = "INSERT INTO order_line (id, product_item_id, order_id, qty, price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, orderLine.getId());
            stmt.setObject(2, orderLine.getProductItemId());
            stmt.setObject(3, orderLine.getOrderId());
            stmt.setInt(4, orderLine.getQty());
            stmt.setObject(5, orderLine.getPrice());
            if (stmt.executeUpdate() > 0) {
                return orderLine;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public OrderLine update(OrderLine orderLine) {
        String sql = "UPDATE order_line SET product_item_id = ?, order_id = ?, qty = ?, price = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, orderLine.getProductItemId());
            stmt.setObject(2, orderLine.getOrderId());
            stmt.setInt(3, orderLine.getQty());
            stmt.setObject(4, orderLine.getPrice());
            stmt.setObject(5, orderLine.getId());
            if (stmt.executeUpdate() > 0) {
                return orderLine;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public OrderLine delete(UUID id) {
        OrderLine orderLineToDelete = findById(id);
        if (orderLineToDelete == null) {
            return null;
        }
        String sql = "DELETE FROM order_line WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            if (stmt.executeUpdate() > 0) {
                return orderLineToDelete;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private OrderLine mapResultSetToOrderLine(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        UUID productItemId = (UUID) rs.getObject("product_item_id");
        UUID orderId = (UUID) rs.getObject("order_id");
        int qty = rs.getInt("qty");
        Double price = rs.getObject("price") != null ? ((Number) rs.getObject("price")).doubleValue() : null;
        return new OrderLine(id, productItemId, orderId, qty, price);
    }
}

