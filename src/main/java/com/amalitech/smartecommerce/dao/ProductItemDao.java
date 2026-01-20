package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductItem;
import com.amalitech.smartecommerce.utils.DBConnection;
import com.amalitech.smartecommerce.utils.PerformanceMonitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductItemDao {

    private final PerformanceMonitor perf = PerformanceMonitor.getInstance();

    public ProductItem findById(UUID id){
        return perf.measureDbOperation("ProductItem.findById", () -> {
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
        });
    }

    /**
     * Find ProductItem by product_id (foreign key).
     * Returns the inventory/pricing info for a specific product.
     */
    public ProductItem findByProductId(UUID productId) {
        return perf.measureDbOperation("ProductItem.findByProductId", () -> {
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
        });
    }


    public ProductItem updateProductQuantity(ProductItem productItem) {
        return perf.measureDbOperation("ProductItem.updateProductQuantity", () -> {
            if (productItem == null || productItem.getProductId() == null) {
                System.err.println("ProductItem.updateProductQuantity called with null productItem or productId");
                return null;
            }
            String sql = "UPDATE product_item SET qty_in_stock = ? WHERE product_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, productItem.getQtyInStock());
                stmt.setObject(2, productItem.getProductId());
                int rowsAffected = stmt.executeUpdate();

                // If update was successful, return the updated item
                if (rowsAffected > 0) {
                    return productItem;
                } else {
                    // no rows updated - likely product_item row doesn't exist
                    System.err.println("ProductItem.updateProductQuantity: no rows updated for product_id=" + productItem.getProductId());
                }
            } catch (SQLException e) {
                System.err.println("Error updating product quantity: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Fetch ALL ProductItems in one query.
     * Used for batch loading inventory to avoid N+1 problem.
     * Performance: 1 query instead of N queries.
     */
    public List<ProductItem> findAll() {
        return perf.measureDbOperation("ProductItem.findAll", () -> {
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
        });
    }

    /**
     * Create a product_item record. Returns the created ProductItem or null on failure.
     */
    public ProductItem create(ProductItem item) {
        return perf.measureDbOperation("ProductItem.create", () -> {
            String sql = "INSERT INTO product_item (id, product_id, qty_in_stock, price, image) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                UUID id = item.getId() != null ? item.getId() : UUID.randomUUID();
                stmt.setObject(1, id);
                stmt.setObject(2, item.getProductId());
                stmt.setInt(3, item.getQtyInStock());
                // price is primitive double in the model; just use it (defaults to 0.0 if not set)
                double priceVal = item.getPrice();
                stmt.setDouble(4, priceVal);
                // image may be null
                stmt.setString(5, item.getImage());
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    return new ProductItem(id, item.getProductId(), item.getQtyInStock(), priceVal, item.getImage());
                }
            } catch (SQLException e) {
                System.err.println("Warning: Could not create product_item for product " + item.getProductId() + ": " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Update both price and quantity for a product_item identified by product_id.
     * Returns the updated ProductItem or null on failure.
     */
    public ProductItem updatePriceAndStock(UUID productId, double price, int qty) {
        return perf.measureDbOperation("ProductItem.updatePriceAndStock", () -> {
            String sql = "UPDATE product_item SET price = ?, qty_in_stock = ? WHERE product_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, price);
                stmt.setInt(2, qty);
                stmt.setObject(3, productId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    // return fresh copy
                    return findByProductId(productId);
                }
            } catch (SQLException e) {
                System.err.println("Error updating price/stock for product_item (product_id=" + productId + "): " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        });
    }

//    INSERT INTO product_item (id, product_id, qty_in_stock, price, image) VALUES (gen_random_uuid(), (SELECT id FROM product WHERE name = 'Smartphone' LIMIT 1), 50, 999.99, 'smartphone.jpg');

    private ProductItem mapResultSetToProductItem(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        UUID product_id = (UUID) rs.getObject("product_id");  // Fixed: was "category_id"
        Integer quantity = Integer.valueOf(rs.getString("qty_in_stock"));
        // Handle price possibly null in result set
        Object priceObj = rs.getObject("price");
        double price = priceObj != null ? ((Number) priceObj).doubleValue() : 0.0;
        String productImage = rs.getString("image");
        return new ProductItem(id, product_id, quantity, price, productImage);
    }
}
