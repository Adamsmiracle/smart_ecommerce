package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.ShoppingCart;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShoppingCartDaoImpl implements ShoppingCartDao {
    @Override
    public ShoppingCart findById(UUID id) {
        String sql = "SELECT * FROM shopping_cart WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShoppingCart(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ShoppingCart> findAll() {
        List<ShoppingCart> carts = new ArrayList<>();
        String sql = "SELECT * FROM shopping_cart";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                carts.add(mapResultSetToShoppingCart(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carts;
    }

    @Override
    public List<ShoppingCart> findByUserId(UUID userId) {
        List<ShoppingCart> carts = new ArrayList<>();
        String sql = "SELECT * FROM shopping_cart WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    carts.add(mapResultSetToShoppingCart(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carts;
    }

    @Override
    public ShoppingCart create(ShoppingCart cart) {
        String sql = "INSERT INTO shopping_cart (id, user_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, cart.getId());
            stmt.setObject(2, cart.getUserId());
            if (stmt.executeUpdate() > 0) {
                return cart;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ShoppingCart update(ShoppingCart cart) {
        String sql = "UPDATE shopping_cart SET user_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, cart.getUserId());
            stmt.setObject(2, cart.getId());
            if (stmt.executeUpdate() > 0) {
                return cart;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ShoppingCart delete(UUID id) {
        ShoppingCart cartToDelete = findById(id);
        if (cartToDelete == null) {
            return null;
        }
        String sql = "DELETE FROM shopping_cart WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            if (stmt.executeUpdate() > 0) {
                return cartToDelete;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ShoppingCart mapResultSetToShoppingCart(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        UUID userId = (UUID) rs.getObject("user_id");
        return new ShoppingCart(id, userId);
    }
}

