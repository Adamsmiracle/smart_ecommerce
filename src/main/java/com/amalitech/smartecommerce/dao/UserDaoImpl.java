package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.User;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDaoImpl implements UserDao {
    @Override
    public User findById(UUID id) {
        String sql = "SELECT * FROM app_user WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM app_user";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public User findByEmail(String emailAddress) {
        String sql = "SELECT * FROM app_user WHERE email_address = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailAddress);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public User insert(User user) {
        String sql = "INSERT INTO app_user (id, email_address, first_name, last_name, phone_number, password) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, user.getId());
            stmt.setString(2, user.getEmailAddress());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getPhoneNumber());
            stmt.setString(6, user.getPassword());
            if (stmt.executeUpdate() > 0) {
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public User update(User user) {
        String sql = "UPDATE app_user SET email_address = ?, first_name = ?, last_name = ?, phone_number = ?, password = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmailAddress());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getPhoneNumber());
            stmt.setString(5, user.getPassword());
            stmt.setObject(6, user.getId());
            if (stmt.executeUpdate() > 0) {
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean delete(UUID id) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Delete related records in order (respecting foreign key constraints)

            // 1. Delete user reviews (references order_line which references customer_order)
            String deleteReviews = "DELETE FROM user_review WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteReviews)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 2. Delete order lines for user's orders
            String deleteOrderLines = "DELETE FROM order_line WHERE order_id IN (SELECT id FROM customer_order WHERE user_id = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrderLines)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 3. Delete customer orders
            String deleteOrders = "DELETE FROM customer_order WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrders)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 4. Delete shopping cart items
            String deleteCartItems = "DELETE FROM shopping_cart_item WHERE cart_id IN (SELECT id FROM shopping_cart WHERE user_id = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(deleteCartItems)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 5. Delete shopping cart
            String deleteCart = "DELETE FROM shopping_cart WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteCart)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 6. Delete payment methods
            String deletePayments = "DELETE FROM use_payment_method WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deletePayments)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 7. Delete user addresses (should cascade but let's be explicit)
            String deleteUserAddresses = "DELETE FROM user_address WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteUserAddresses)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 8. Finally delete the user
            String deleteUser = "DELETE FROM app_user WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteUser)) {
                stmt.setObject(1, id);
                int result = stmt.executeUpdate();

                if (result > 0) {
                    conn.commit(); // Commit transaction
                    return true;
                } else {
                    conn.rollback(); // Rollback if user not found
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
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


    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String email = rs.getString("email_address");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String phone = rs.getString("phone_number");
        String password = rs.getString("password");
        return new User(id, email, firstName, lastName, phone, password);
    }
}
