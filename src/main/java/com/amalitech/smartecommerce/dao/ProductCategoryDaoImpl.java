package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.ProductCategory;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductCategoryDaoImpl implements ProductCategoryDao {
    @Override
    public ProductCategory findById(UUID id) {
        String sql = "SELECT * FROM product_category WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProductCategory(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ProductCategory> findAll() {
        List<ProductCategory> categories = new ArrayList<>();
        String sql = "SELECT * FROM product_category";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(mapResultSetToProductCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public ProductCategory create(ProductCategory category) {
        String sql = "INSERT INTO product_category (id, parent_category_id, category_name) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, category.getId());
            stmt.setObject(2, category.getParentCategoryId());
            stmt.setString(3, category.getCategoryName());
            if (stmt.executeUpdate() > 0) {
                return category;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ProductCategory update(ProductCategory category) {
        String sql = "UPDATE product_category SET parent_category_id = ?, category_name = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, category.getParentCategoryId());
            stmt.setString(2, category.getCategoryName());
            stmt.setObject(3, category.getId());
            if (stmt.executeUpdate() > 0) {
                return category;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ProductCategory delete(UUID id) {
        ProductCategory categoryToDelete = findById(id);
        if (categoryToDelete == null) {
            return null;
        }
        String sql = "DELETE FROM product_category WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            if (stmt.executeUpdate() > 0) {
                return categoryToDelete;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ProductCategory mapResultSetToProductCategory(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        UUID parentCategoryId = (UUID) rs.getObject("parent_category_id");
        String categoryName = rs.getString("category_name");
        return new ProductCategory(id, parentCategoryId, categoryName);
    }
}

