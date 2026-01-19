package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.ShippingMethod;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ShippingMethodDaoImpl implements ShippingMethodDao {

    @Override
    public ShippingMethod findById(UUID id) {
        String sql = "SELECT * FROM shipping_method WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShippingMethod(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ShippingMethod> findAll() {
        List<ShippingMethod> methods = new ArrayList<>();
        String sql = "SELECT * FROM shipping_method ORDER BY price ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                methods.add(mapResultSetToShippingMethod(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return methods;
    }

    @Override
    public ShippingMethod create(ShippingMethod shippingMethod) {
        String sql = "INSERT INTO shipping_method (id, name, price) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            UUID id = shippingMethod.getId() != null ? shippingMethod.getId() : UUID.randomUUID();
            stmt.setObject(1, id);
            stmt.setString(2, shippingMethod.getName());
            stmt.setDouble(3, shippingMethod.getPrice());
            if (stmt.executeUpdate() > 0) {
                shippingMethod.setId(id);
                return shippingMethod;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ShippingMethod update(ShippingMethod shippingMethod) {
        String sql = "UPDATE shipping_method SET name = ?, price = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, shippingMethod.getName());
            stmt.setDouble(2, shippingMethod.getPrice());
            stmt.setObject(3, shippingMethod.getId());
            if (stmt.executeUpdate() > 0) {
                return shippingMethod;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ShippingMethod delete(UUID id) {
        ShippingMethod methodToDelete = findById(id);
        if (methodToDelete == null) {
            return null;
        }
        String sql = "DELETE FROM shipping_method WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            if (stmt.executeUpdate() > 0) {
                return methodToDelete;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ShippingMethod mapResultSetToShippingMethod(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        return new ShippingMethod(id, name, price);
    }

    @Override
    /**
     * Gets the shipping cost for a shipping method.
     */
    public double getShippingCost(UUID shippingMethodId) {
        if (shippingMethodId == null) {
            return 0.0;
        }
        try {
            java.sql.Connection conn = com.amalitech.smartecommerce.utils.DBConnection.getConnection();
            String sql = "SELECT price FROM shipping_method WHERE id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, shippingMethodId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("price");
                    }
                }
            }
        } catch (java.sql.SQLException e) {
        }
        return 0.0;
    }
}

