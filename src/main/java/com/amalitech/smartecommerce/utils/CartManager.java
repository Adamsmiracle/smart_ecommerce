package com.amalitech.smartecommerce.utils;

import com.amalitech.smartecommerce.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple in-memory shopping cart manager.
 */
public class CartManager {
    private static CartManager instance;

    // Map of product ID to quantity
    private final Map<UUID, CartItem> cartItems = new HashMap<>();

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Product product, int quantity) {
        if (product == null || product.getId() == null) return;

        UUID productId = product.getId();
        if (cartItems.containsKey(productId)) {
            CartItem item = cartItems.get(productId);
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            cartItems.put(productId, new CartItem(product, quantity));
        }
    }

    public void removeFromCart(UUID productId) {
        cartItems.remove(productId);
    }

    public void updateQuantity(UUID productId, int quantity) {
        if (cartItems.containsKey(productId)) {
            if (quantity <= 0) {
                cartItems.remove(productId);
            } else {
                cartItems.get(productId).setQuantity(quantity);
            }
        }
    }

    public void clearCart() {
        cartItems.clear();
    }

    public ObservableList<CartItem> getCartItems() {
        return FXCollections.observableArrayList(cartItems.values());
    }

    public int getCartSize() {
        return cartItems.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public int getUniqueItemCount() {
        return cartItems.size();
    }

    public double getCartTotal() {
        double total = 0;
        for (CartItem item : cartItems.values()) {
            double price = getProductPrice(item.getProductId());
            total += price * item.getQuantity();
        }
        return total;
    }

    /**
     * Gets the price of a product from the product_item table.
     */
    private double getProductPrice(UUID productId) {
        if (productId == null) return 0.0;
        try {
            java.sql.Connection conn = DBConnection.getConnection();
            String sql = "SELECT price FROM product_item WHERE product_id = ? LIMIT 1";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, productId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("price");
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            // Silently fail - return 0 if price not found
        }
        return 0.0;
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    /**
     * Inner class representing a cart item.
     */
    public static class CartItem {
        private final Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getProductName() {
            return product != null ? product.getName() : "Unknown";
        }

        public UUID getProductId() {
            return product != null ? product.getId() : null;
        }
    }
}

