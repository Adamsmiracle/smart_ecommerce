package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductDaoImpl implements ProductDao {

    @Override
    public Product findById(UUID id) {
        String sql = "SELECT * FROM product WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM product";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findByCategoryId(UUID categoryId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM product WHERE category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }


    @Override
    public List<Product> searchByName(String name) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM product WHERE LOWER(name) LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name.toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public Product create(Product product) {
        String sql = "INSERT INTO product (id, category_id, name, description, product_image) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, product.getId());
            stmt.setObject(2, product.getCategoryId());
            stmt.setString(3, product.getName());
            stmt.setString(4, product.getDescription());
            stmt.setString(5, product.getProductImage());
            if (stmt.executeUpdate() > 0) {
                return product;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Product update(Product product) {
        String sql = "UPDATE product SET category_id = ?, name = ?, description = ?, product_image = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, product.getCategoryId());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setString(4, product.getProductImage());
            stmt.setObject(5, product.getId());
            if (stmt.executeUpdate() > 0) {
                return product;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Product delete(UUID id) {
        Connection conn = null;
        Product productToDelete = findById(id);
        if (productToDelete == null) {
            return null;
        }
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Delete from order_line where product_item references this product
            String deleteOrderLines = "DELETE FROM order_line WHERE product_item_id IN (SELECT id FROM product_item WHERE product_id = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrderLines)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 2. Delete product_item records for this product
            String deleteProductItems = "DELETE FROM product_item WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteProductItems)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 3. Delete the product
            String deleteProduct = "DELETE FROM product WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteProduct)) {
                stmt.setObject(1, id);
                int result = stmt.executeUpdate();

                if (result > 0) {
                    conn.commit();
                    return productToDelete;
                } else {
                    conn.rollback();
                    return null;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        UUID categoryId = (UUID) rs.getObject("category_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String productImage = rs.getString("product_image");
        return new Product(id, categoryId, name, description, productImage);
    }

}
