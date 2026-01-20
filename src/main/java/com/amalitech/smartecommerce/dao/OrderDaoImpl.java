package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.cache.InventoryCache;
import com.amalitech.smartecommerce.controller.CustomerDashboardController;
import com.amalitech.smartecommerce.model.Order;
import com.amalitech.smartecommerce.model.OrderLine;
import com.amalitech.smartecommerce.utils.DBConnection;
import com.amalitech.smartecommerce.utils.PerformanceMonitor;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderDaoImpl implements OrderDao {
    private static final Logger LOGGER = Logger.getLogger(OrderDaoImpl.class.getName());
    private final PerformanceMonitor perf = PerformanceMonitor.getInstance();

    @Override
    public Order findById(UUID id) {
        String sql = "SELECT * FROM customer_order WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding Order by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM customer_order";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all orders", e);
        }
        return orders;
    }

    @Override
    public Order create(Order order) {
        String sql = "INSERT INTO customer_order (id, user_id, order_date, payment_method_id, shipping_address_id, shipping_method_id, order_total, order_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, order.getId());
            stmt.setObject(2, order.getUserId());
            stmt.setObject(3, order.getOrderDate());
            stmt.setObject(4, order.getPaymentMethodId());
            stmt.setObject(5, order.getShippingAddressId());
            stmt.setObject(6, order.getShippingMethodId());
            stmt.setObject(7, order.getOrderTotal());
            stmt.setObject(8, order.getOrderStatus());
            if (stmt.executeUpdate() > 0) {
                return order;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating order: " + order, e);
        }
        return null;
    }

    @Override
    public Order update(Order order) {
        String sql = "UPDATE customer_order SET user_id = ?, order_date = ?, payment_method_id = ?, shipping_address_id = ?, shipping_method_id = ?, order_total = ?, order_status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, order.getUserId());
            stmt.setObject(2, order.getOrderDate());
            stmt.setObject(3, order.getPaymentMethodId());
            stmt.setObject(4, order.getShippingAddressId());
            stmt.setObject(5, order.getShippingMethodId());
            stmt.setObject(6, order.getOrderTotal());
            stmt.setObject(7, order.getOrderStatus());
            stmt.setObject(8, order.getId());
            if (stmt.executeUpdate() > 0) {
                return order;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating order: " + order, e);
        }
        return null;
    }

    @Override
    public Order delete(UUID id) {
        Connection conn = null;
        Order orderToDelete = findById(id);
        if (orderToDelete == null) {
            return null;
        }
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Delete user reviews associated with order lines of this order
            String deleteReviews = "DELETE FROM user_review WHERE ordered_product_id IN (SELECT id FROM order_line WHERE order_id = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(deleteReviews)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }

            // 2. Restore inventory qty for each order_line of this order
            String restoreQtySql = "UPDATE product_item SET qty_in_stock = qty_in_stock + ol.qty FROM order_line ol WHERE ol.product_item_id = product_item.id AND ol.order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(restoreQtySql)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Warning: failed to restore inventory quantities for order: " + id, ex);
                // proceed to delete to avoid leaving orphan orders; developer may choose to rollback instead
            }

            // Refresh InventoryCache for affected product_items
            try {
                InventoryCache invCache = InventoryCache.getInstance();
                // Select product_id (the FK to product) so we can update InventoryCache which indexes by product_id
                // corrected JOIN: link order_line.product_item_id to product_item.id
                String selectQtySql = "SELECT pi.product_id, pi.qty_in_stock FROM product_item pi JOIN order_line ol ON ol.product_item_id = pi.id WHERE ol.order_id = ?";
                try (PreparedStatement qStmt = conn.prepareStatement(selectQtySql)) {
                    qStmt.setObject(1, id);
                    try (ResultSet rs = qStmt.executeQuery()) {
                        while (rs.next()) {
                            java.util.UUID productId = (java.util.UUID) rs.getObject("product_id");
                            int qty = rs.getInt("qty_in_stock");
                            if (productId != null && invCache.containsProductId(productId)) {
                                invCache.updateQuantity(productId, qty);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Warning: failed to refresh inventory cache during order delete: " + id, ex);
            }

            // 3. Delete order lines for this order
            String deleteOrderLines = "DELETE FROM order_line WHERE order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrderLines)) {
                stmt.setObject(1, id);
                stmt.executeUpdate();
            }


            // 4. Delete the order
            String deleteOrder = "DELETE FROM customer_order WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteOrder)) {
                stmt.setObject(1, id);
                int result = stmt.executeUpdate();
                System.out.println("Deleted order return data: "+ result);

                if (result > 0) {
                    conn.commit(); // Commit transaction
                    return orderToDelete;
                } else {
                    conn.rollback(); // Rollback if order not found
                    return null;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting order: " + id, e);
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error during rollback for order delete: " + id, ex);
                }
            }
            return null;
        } finally {
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
                        try { conn.close(); } catch (SQLException ignore) {}
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error checking connection closed state after order delete", e);
                }
            }
         }
     }

    @Override
    public Order createWithLines(Order order, List<OrderLine> orderLines) throws SQLException {
        long start = System.nanoTime();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Create order
            String insertOrderSql = "INSERT INTO customer_order (id, user_id, order_date, payment_method_id, shipping_address_id, shipping_method_id, order_total, order_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSql)) {
                orderStmt.setObject(1, order.getId());
                orderStmt.setObject(2, order.getUserId());
                orderStmt.setObject(3, order.getOrderDate());
                orderStmt.setObject(4, order.getPaymentMethodId());
                orderStmt.setObject(5, order.getShippingAddressId());
                orderStmt.setObject(6, order.getShippingMethodId());
                orderStmt.setObject(7, order.getOrderTotal());
                orderStmt.setObject(8, order.getOrderStatus());
                orderStmt.executeUpdate();
            }

            // Insert each order line and decrement stock in the same connection/transaction
            String insertLineSql = "INSERT INTO order_line (id, product_item_id, order_id, qty, price) VALUES (?, ?, ?, ?, ?)";
            String decStockSql = "UPDATE product_item SET qty_in_stock = qty_in_stock - ? WHERE id = ? AND qty_in_stock >= ?"; // ensure available stock

            for (OrderLine ol : orderLines) {
                if (ol.getId() == null) ol.setId(UUID.randomUUID());
                ol.setOrderId(order.getId());

                try (PreparedStatement lineStmt = conn.prepareStatement(insertLineSql)) {
                    lineStmt.setObject(1, ol.getId());
                    lineStmt.setObject(2, ol.getProductItemId());
                    lineStmt.setObject(3, ol.getOrderId());
                    lineStmt.setInt(4, ol.getQty());
                    lineStmt.setObject(5, ol.getPrice());
                    lineStmt.executeUpdate();
                }

                // decrement stock only if there is sufficient stock
                try (PreparedStatement decStmt = conn.prepareStatement(decStockSql)) {
                    decStmt.setInt(1, ol.getQty());
                    decStmt.setObject(2, ol.getProductItemId());
                    decStmt.setInt(3, ol.getQty());
                    int rows = decStmt.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Insufficient stock for product_item: " + ol.getProductItemId());
                    }
                }
            }

            conn.commit();

            // Refresh InventoryCache entries for affected product_item ids
            try {
                com.amalitech.smartecommerce.cache.InventoryCache invCache = com.amalitech.smartecommerce.cache.InventoryCache.getInstance();
                // Build IN clause
                StringBuilder inClause = new StringBuilder();
                for (int i = 0; i < orderLines.size(); i++) {
                    if (i > 0) inClause.append(",");
                    inClause.append("?");
                }
                // Select product_id for the given product_item ids so we can update the inventory cache keyed by product_id
                String selectQtySql = "SELECT product_id, qty_in_stock FROM product_item WHERE id IN (" + inClause + ")";
                try (PreparedStatement qStmt = conn.prepareStatement(selectQtySql)) {
                    int idx = 1;
                    for (com.amalitech.smartecommerce.model.OrderLine ol : orderLines) {
                        qStmt.setObject(idx++, ol.getProductItemId());
                    }
                    try (ResultSet rs = qStmt.executeQuery()) {
                        while (rs.next()) {
                            java.util.UUID productId = (java.util.UUID) rs.getObject("product_id");
                            int qty = rs.getInt("qty_in_stock");
                            if (productId != null && invCache.containsProductId(productId)) {
                                invCache.updateQuantity(productId, qty);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // non-fatal: log
                LOGGER.log(Level.WARNING, "Failed to refresh inventory cache after order creation: {0}", ex.getMessage());
            }

            return order;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed: {0}", ex.getMessage()); }
            }
            throw e;
        } finally {
            // Restore auto-commit and close connection
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
                        try { conn.close(); } catch (SQLException ignore) {}
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error checking connection closed state after createWithLines", e);
                }
            }
            long durationMicros = (System.nanoTime() - start) / 1000;
            perf.recordDbOperation("Order.createWithLines", durationMicros);
        }
    }

    @Override
    public Order modifyOrderLines(UUID orderId, List<OrderLine> newOrderLines) throws SQLException {
        long start = System.nanoTime();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Load existing order lines
            List<OrderLine> existingLines = new ArrayList<>();
            String selectLines = "SELECT id, product_item_id, order_id, qty, price FROM order_line WHERE order_id = ?";
            try (PreparedStatement sel = conn.prepareStatement(selectLines)) {
                sel.setObject(1, orderId);
                try (ResultSet rs = sel.executeQuery()) {
                    while (rs.next()) {
                        OrderLine ol = new OrderLine();
                        ol.setId((java.util.UUID) rs.getObject("id"));
                        ol.setProductItemId((java.util.UUID) rs.getObject("product_item_id"));
                        ol.setOrderId((java.util.UUID) rs.getObject("order_id"));
                        ol.setQty(rs.getInt("qty"));
                        ol.setPrice(rs.getDouble("price"));
                        existingLines.add(ol);
                    }
                }
            }

            // 2. Restore inventory quantities from existing lines
            String restoreSql = "UPDATE product_item SET qty_in_stock = qty_in_stock + ? WHERE id = ?";
            try (PreparedStatement rstmt = conn.prepareStatement(restoreSql)) {
                for (com.amalitech.smartecommerce.model.OrderLine ol : existingLines) {
                    rstmt.setInt(1, ol.getQty());
                    rstmt.setObject(2, ol.getProductItemId());
                    rstmt.addBatch();
                }
                rstmt.executeBatch();
            }

            // 3. Remove existing order lines
            String deleteLines = "DELETE FROM order_line WHERE order_id = ?";
            try (PreparedStatement dstmt = conn.prepareStatement(deleteLines)) {
                dstmt.setObject(1, orderId);
                dstmt.executeUpdate();
            }

            // 4. Insert new order lines and decrement inventory
            String insertLineSql = "INSERT INTO order_line (id, product_item_id, order_id, qty, price) VALUES (?, ?, ?, ?, ?)";
            String decStockSql = "UPDATE product_item SET qty_in_stock = qty_in_stock - ? WHERE id = ? AND qty_in_stock >= ?";

            for (OrderLine ol : newOrderLines) {
                if (ol.getId() == null) ol.setId(java.util.UUID.randomUUID());
                ol.setOrderId(orderId);

                try (PreparedStatement lineStmt = conn.prepareStatement(insertLineSql)) {
                    lineStmt.setObject(1, ol.getId());
                    lineStmt.setObject(2, ol.getProductItemId());
                    lineStmt.setObject(3, ol.getOrderId());
                    lineStmt.setInt(4, ol.getQty());
                    lineStmt.setObject(5, ol.getPrice());
                    lineStmt.executeUpdate();
                }

                try (PreparedStatement decStmt = conn.prepareStatement(decStockSql)) {
                    decStmt.setInt(1, ol.getQty());
                    decStmt.setObject(2, ol.getProductItemId());
                    decStmt.setInt(3, ol.getQty());
                    int rows = decStmt.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Insufficient stock for product_item: " + ol.getProductItemId());
                    }
                }
            }

            // 5. Update order total from sum of line totals
            double total = 0.0;
            for (OrderLine ol : newOrderLines) {
                total += ol.getPrice() * ol.getQty();
            }
            // Add shipping cost if present on the order
            double shippingCost = 0.0;
            String getShippingSql = "SELECT shipping_method_id FROM customer_order WHERE id = ?";
            try (PreparedStatement smStmt = conn.prepareStatement(getShippingSql)) {
                smStmt.setObject(1, orderId);
                try (ResultSet rsSm = smStmt.executeQuery()) {
                    if (rsSm.next()) {
                        java.util.UUID smId = (java.util.UUID) rsSm.getObject("shipping_method_id");
                        if (smId != null) {
                            String priceSql = "SELECT price FROM shipping_method WHERE id = ?";
                            try (PreparedStatement pStmt = conn.prepareStatement(priceSql)) {
                                pStmt.setObject(1, smId);
                                try (ResultSet rsPrice = pStmt.executeQuery()) {
                                    if (rsPrice.next()) {
                                        shippingCost = rsPrice.getDouble("price");
                                    }
                                }
                            }
                        }
                    }
                } catch (SQLException ex) {
                    // non-fatal: log and continue without shipping cost
                    LOGGER.log(Level.WARNING, "Failed to fetch shipping cost for order {0}: {1}", new Object[]{orderId, ex.getMessage()});
                }
            }

            double finalTotal = total + shippingCost;
            String updateOrderSql = "UPDATE customer_order SET order_total = ? WHERE id = ?";
            try (PreparedStatement ustmt = conn.prepareStatement(updateOrderSql)) {
                ustmt.setObject(1, finalTotal);
                ustmt.setObject(2, orderId);
                ustmt.executeUpdate();
            }

            conn.commit();

            // Refresh inventory cache for affected product_ids
            try {
                InventoryCache invCache = InventoryCache.getInstance();
                Set<UUID> productItemIds = new HashSet<>();
                for (OrderLine ol : newOrderLines) productItemIds.add(ol.getProductItemId());
                for (OrderLine ol : existingLines) productItemIds.add(ol.getProductItemId());

                if (!productItemIds.isEmpty()) {
                    StringBuilder inClause = new StringBuilder();
                    int idx = 0;
                    for (int i = 0; i < productItemIds.size(); i++) { if (i > 0) inClause.append(","); inClause.append("?"); }
                    String selectQtySql = "SELECT product_id, qty_in_stock FROM product_item WHERE id IN (" + inClause + ")";
                    try (PreparedStatement qStmt = conn.prepareStatement(selectQtySql)) {
                        int j=1;
                        for (UUID pid : productItemIds) qStmt.setObject(j++, pid);
                        try (ResultSet rs = qStmt.executeQuery()) {
                            while (rs.next()) {
                                UUID productId = (UUID) rs.getObject("product_id");
                                int qty = rs.getInt("qty_in_stock");
                                if (productId != null && invCache.containsProductId(productId)) {
                                    invCache.updateQuantity(productId, qty);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to refresh inventory cache after modifying order: {0}", ex.getMessage());
            }

            return findById(orderId);
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed: {0}", ex.getMessage()); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
                        try { conn.close(); } catch (SQLException ignore) {}
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error checking connection closed state after modifyOrderLines", e);
                }
            }
            long durationMicros = (System.nanoTime() - start) / 1000;
            perf.recordDbOperation("Order.modifyOrderLines", durationMicros);
        }
    }


    @Override
    public List<CustomerDashboardController.OrderItemDetail> getOrderItemsForCustomer(UUID orderId) {
        List<CustomerDashboardController.OrderItemDetail> items = new ArrayList<>();

        String sql = """
            SELECT p.name AS product_name, ol.qty, ol.price
            FROM order_line ol
            JOIN product_item pi ON ol.product_item_id = pi.id
            JOIN product p ON pi.product_id = p.id
            WHERE ol.order_id = ?
            ORDER BY p.name
            """;

        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, orderId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("product_name");
                    int qty = rs.getInt("qty");
                    double price = rs.getDouble("price");
                    items.add(new CustomerDashboardController.OrderItemDetail(
                            productName != null ? productName : "Unknown Product",
                            qty,
                            price
                    ));
                }
            }
        } catch (java.sql.SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching order items: {0}", e.getMessage());
        }

        return items;
    }

    @Override
    public List<OrderLine> getOrderLinesRaw(UUID orderId) {
        List<OrderLine> lines = new ArrayList<>();
        String sql = "SELECT id, product_item_id, order_id, qty, price FROM order_line WHERE order_id = ? ORDER BY id";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderLine ol = new OrderLine();
                    ol.setId((UUID) rs.getObject("id"));
                    ol.setProductItemId((UUID) rs.getObject("product_item_id"));
                    ol.setOrderId((UUID) rs.getObject("order_id"));
                    ol.setQty(rs.getInt("qty"));
                    ol.setPrice(rs.getDouble("price"));
                    lines.add(ol);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching raw order lines for order: " + orderId, e);
        }
        return lines;
    }

    @Override
    public List<Order> getOrdersByUser(UUID user_id) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM customer_order WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, user_id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching orders for user: " + user_id, e);
        }
        return orders;

    }


    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        UUID userId = (UUID) rs.getObject("user_id");
        LocalDate orderDate = rs.getObject("order_date", LocalDate.class);
        UUID paymentMethodId = (UUID) rs.getObject("payment_method_id");
        UUID shippingAddressId = (UUID) rs.getObject("shipping_address_id");
        UUID shippingMethodId = (UUID) rs.getObject("shipping_method_id");
        Double orderTotal = rs.getObject("order_total") != null ? ((Number) rs.getObject("order_total")).doubleValue() : null;
        UUID orderStatus = (UUID) rs.getObject("order_status");
        return new Order(id, userId, orderDate, paymentMethodId, shippingAddressId, shippingMethodId, orderTotal, orderStatus);
    }
}

