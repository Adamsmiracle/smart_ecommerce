package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductItem;
import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductItemDao {

    public ProductItem findById(UUID id){
        String sql = "SELECT * FROM product_item WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProductItem(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find ProductItem by product_id (foreign key).
     * Returns the inventory/pricing info for a specific product.
     */
    public ProductItem findByProductId(UUID productId) {
        String sql = "SELECT * FROM product_item WHERE product_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProductItem(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding ProductItem by product_id " + productId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    public ProductItem updateProductQuantity(ProductItem productItem) {
        String sql = "UPDATE product_item SET qty_in_stock = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productItem.getQtyInStock());
            stmt.setObject(2, productItem.getProductId());
            int rowsAffected = stmt.executeUpdate();

            // If update was successful, return the updated item
            if (rowsAffected > 0) {
                return productItem;
            }
        } catch (SQLException e) {
            System.err.println("Error updating product quantity: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetch ALL ProductItems in one query.
     * Used for batch loading inventory to avoid N+1 problem.
     * Performance: 1 query instead of N queries.
     */
    public List<ProductItem> findAll() {
        List<ProductItem> items = new ArrayList<>();
        String sql = "SELECT * FROM product_item";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                items.add(mapResultSetToProductItem(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all ProductItems: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }



//    INSERT INTO product_item (id, product_id, qty_in_stock, price, image) VALUES (gen_random_uuid(), (SELECT id FROM product WHERE name = 'Smartphone' LIMIT 1), 50, 999.99, 'smartphone.jpg');

    private ProductItem mapResultSetToProductItem(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        UUID product_id = (UUID) rs.getObject("product_id");  // Fixed: was "category_id"
        Integer quantity = Integer.valueOf(rs.getString("qty_in_stock"));
        Double price = Double.valueOf(rs.getString("price"));
        String productImage = rs.getString("image");
        return new ProductItem(id, product_id, quantity, price, productImage);
    }
}
