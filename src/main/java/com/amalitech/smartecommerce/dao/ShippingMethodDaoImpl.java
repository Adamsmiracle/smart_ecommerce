package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.ShippingMethod;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public boolean insert(ShippingMethod shippingMethod) {
        String sql = "INSERT INTO shipping_method (id, name, price) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, shippingMethod.getId() != null ? shippingMethod.getId() : UUID.randomUUID());
            stmt.setString(2, shippingMethod.getName());
            stmt.setDouble(3, shippingMethod.getPrice());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(ShippingMethod shippingMethod) {
        String sql = "UPDATE shipping_method SET name = ?, price = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, shippingMethod.getName());
            stmt.setDouble(2, shippingMethod.getPrice());
            stmt.setObject(3, shippingMethod.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(UUID id) {
        String sql = "DELETE FROM shipping_method WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ShippingMethod mapResultSetToShippingMethod(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        return new ShippingMethod(id, name, price);
    }
}

