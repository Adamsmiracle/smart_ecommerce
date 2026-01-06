package com.amalitech.smartecommerce.cache;

import com.amalitech.smartecommerce.model.Order;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory cache for orders with O(1) lookups.
 * Provides fast retrieval by ID and user ID, plus search capabilities.
 */
public class OrderCache {
    private static OrderCache instance;

    // Primary cache: id -> Order (mirrors primary key index)
    private final Map<UUID, Order> orderById;

    // Secondary index: userId -> List<Order> (mirrors user_id index)
    private final Map<UUID, List<Order>> ordersByUserId;

    // All orders list for iteration
    private List<Order> allOrders;

    // Cache statistics
    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long lastRefreshTime = 0;

    private OrderCache() {
        this.orderById = new ConcurrentHashMap<>();
        this.ordersByUserId = new ConcurrentHashMap<>();
        this.allOrders = new ArrayList<>();
    }

    public static synchronized OrderCache getInstance() {
        if (instance == null) {
            instance = new OrderCache();
        }
        return instance;
    }

    /**
     * Load all orders into cache, building indexes.
     */
    public void loadAll(List<Order> orders) {
        clear();
        this.allOrders = new ArrayList<>(orders);

        for (Order order : orders) {
            // Primary index
            orderById.put(order.getId(), order);

            // User ID index
            if (order.getUserId() != null) {
                ordersByUserId
                    .computeIfAbsent(order.getUserId(), k -> new ArrayList<>())
                    .add(order);
            }
        }

        lastRefreshTime = System.currentTimeMillis();
    }

    /**
     * Get order by ID - O(1) lookup.
     */
    public Order getById(UUID id) {
        Order order = orderById.get(id);
        if (order != null) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
        return order;
    }

    /**
     * Get all orders for a specific user - O(1) lookup.
     */
    public List<Order> getByUserId(UUID userId) {
        if (userId == null) {
            cacheMisses++;
            return new ArrayList<>();
        }
        List<Order> orders = ordersByUserId.get(userId);
        if (orders != null) {
            cacheHits++;
            return new ArrayList<>(orders);
        }
        cacheMisses++;
        return new ArrayList<>();
    }

    /**
     * Get all orders.
     */
    public List<Order> getAll() {
        cacheHits++;
        return new ArrayList<>(allOrders);
    }

    /**
     * Search orders by order ID (partial match).
     */
    public List<Order> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAll();
        }

        String searchTerm = query.toLowerCase().trim();
        cacheHits++;

        return allOrders.stream()
            .filter(order -> {
                String orderId = order.getId() != null ? order.getId().toString().toLowerCase() : "";
                return orderId.contains(searchTerm);
            })
            .collect(Collectors.toList());
    }

    /**
     * Get orders within a date range.
     */
    public List<Order> getOrdersByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        cacheHits++;

        return allOrders.stream()
            .filter(order -> {
                if (order.getOrderDate() == null) return false;
                boolean afterStart = startDate == null || !order.getOrderDate().isBefore(startDate);
                boolean beforeEnd = endDate == null || !order.getOrderDate().isAfter(endDate);
                return afterStart && beforeEnd;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get orders with specific status.
     */
    public List<Order> getOrdersByStatus(UUID statusId) {
        cacheHits++;

        return allOrders.stream()
            .filter(order -> order.getOrderStatus() != null && order.getOrderStatus().equals(statusId))
            .collect(Collectors.toList());
    }

    /**
     * Add an order to the cache.
     */
    public void put(Order order) {
        if (order == null || order.getId() == null) return;

        orderById.put(order.getId(), order);

        if (order.getUserId() != null) {
            ordersByUserId
                .computeIfAbsent(order.getUserId(), k -> new ArrayList<>())
                .add(order);
        }

        // Add to list if not already present
        if (!allOrders.contains(order)) {
            allOrders.add(order);
        }
    }

    /**
     * Remove an order from the cache.
     */
    public void remove(UUID id) {
        Order order = orderById.remove(id);
        if (order != null) {
            allOrders.remove(order);
            if (order.getUserId() != null) {
                List<Order> userOrders = ordersByUserId.get(order.getUserId());
                if (userOrders != null) {
                    userOrders.remove(order);
                }
            }
        }
    }

    /**
     * Update an order in the cache.
     */
    public void update(Order order) {
        if (order == null || order.getId() == null) return;

        // Get the old order to remove old user index
        Order oldOrder = orderById.get(order.getId());
        if (oldOrder != null && oldOrder.getUserId() != null) {
            List<Order> oldUserOrders = ordersByUserId.get(oldOrder.getUserId());
            if (oldUserOrders != null) {
                oldUserOrders.remove(oldOrder);
            }
        }

        // Update primary index
        orderById.put(order.getId(), order);

        // Update user index
        if (order.getUserId() != null) {
            ordersByUserId
                .computeIfAbsent(order.getUserId(), k -> new ArrayList<>())
                .add(order);
        }

        // Update in list
        int index = -1;
        for (int i = 0; i < allOrders.size(); i++) {
            if (allOrders.get(i).getId().equals(order.getId())) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            allOrders.set(index, order);
        } else {
            allOrders.add(order);
        }
    }

    /**
     * Get count of orders for a user.
     */
    public int getOrderCountForUser(UUID userId) {
        if (userId == null) return 0;
        List<Order> userOrders = ordersByUserId.get(userId);
        return userOrders != null ? userOrders.size() : 0;
    }

    /**
     * Get total amount spent by a user (sum of all order totals).
     */
    public double getTotalSpentByUser(UUID userId) {
        if (userId == null) return 0.0;
        List<Order> userOrders = ordersByUserId.get(userId);
        if (userOrders == null) return 0.0;

        return userOrders.stream()
            .mapToDouble(o -> o.getOrderTotal() != null ? o.getOrderTotal() : 0.0)
            .sum();
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        orderById.clear();
        ordersByUserId.clear();
        allOrders.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }

    /**
     * Get cache hit rate as percentage.
     */
    public double getHitRate() {
        long total = cacheHits + cacheMisses;
        return total > 0 ? (cacheHits * 100.0 / total) : 0;
    }

    // Getters for statistics
    public long getCacheHits() { return cacheHits; }
    public long getCacheMisses() { return cacheMisses; }
    public int getSize() { return allOrders.size(); }
    public long getLastRefreshTime() { return lastRefreshTime; }
}

