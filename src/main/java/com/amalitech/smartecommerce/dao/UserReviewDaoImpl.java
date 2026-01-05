package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.UserReview;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserReviewDaoImpl implements UserReviewDao {
    @Override
    public UserReview findById(UUID id) {
        String sql = "SELECT * FROM user_review WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserReview(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<UserReview> findAll() {
        List<UserReview> reviews = new ArrayList<>();
        String sql = "SELECT * FROM user_review";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reviews.add(mapResultSetToUserReview(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    @Override
    public List<UserReview> findByUserId(UUID userId) {
        List<UserReview> reviews = new ArrayList<>();
        String sql = "SELECT * FROM user_review WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapResultSetToUserReview(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    @Override
    public List<UserReview> findByOrderedProductId(UUID orderedProductId) {
        List<UserReview> reviews = new ArrayList<>();
        String sql = "SELECT * FROM user_review WHERE ordered_product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, orderedProductId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapResultSetToUserReview(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    @Override
    public boolean insert(UserReview review) {
        String sql = "INSERT INTO user_review (id, user_id, ordered_product_id, rating_value, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, review.getId());
            stmt.setObject(2, review.getUserId());
            stmt.setObject(3, review.getOrderedProductId());
            stmt.setObject(4, review.getRatingValue());
            stmt.setString(5, review.getComment());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(UserReview review) {
        String sql = "UPDATE user_review SET user_id = ?, ordered_product_id = ?, rating_value = ?, comment = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, review.getUserId());
            stmt.setObject(2, review.getOrderedProductId());
            stmt.setObject(3, review.getRatingValue());
            stmt.setString(4, review.getComment());
            stmt.setObject(5, review.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(UUID id) {
        String sql = "DELETE FROM user_review WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private UserReview mapResultSetToUserReview(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        UUID userId = (UUID) rs.getObject("user_id");
        UUID orderedProductId = (UUID) rs.getObject("ordered_product_id");
        Integer ratingValue = rs.getObject("rating_value") != null ? rs.getInt("rating_value") : null;
        String comment = rs.getString("comment");
        return new UserReview(id, userId, orderedProductId, ratingValue, comment);
    }
}

