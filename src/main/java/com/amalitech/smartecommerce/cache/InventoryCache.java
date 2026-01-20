package com.amalitech.smartecommerce.cache;

import com.amalitech.smartecommerce.model.ProductItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory cache for inventory (ProductItem) data using HashMap for O(1) lookups.
 * Mirrors database indexing for fast retrieval of stock quantities and pricing.
 */
public class InventoryCache {
    private static InventoryCache instance;

    // Primary cache: productId -> ProductItem (mirrors product_id index)
    private final Map<UUID, ProductItem> inventoryByProductId;

    // Secondary cache: productItemId -> ProductItem (mirrors product_item.id)
    private final Map<UUID, ProductItem> inventoryById;

    // All inventory items list for iteration
    private List<ProductItem> allInventory;

    // Cache statistics for performance monitoring
    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long lastRefreshTime = 0;

    private InventoryCache() {
        this.inventoryByProductId = new ConcurrentHashMap<>();
        this.inventoryById = new ConcurrentHashMap<>();
        this.allInventory = new ArrayList<>();
    }

    public static InventoryCache getInstance() {
        if (instance == null) {
            instance = new InventoryCache();
        }
        return instance;
    }

    /**
     * Load all inventory items into cache.
     */
    public void loadAll(List<ProductItem> items) {
        clear();
        this.allInventory = new ArrayList<>(items);

        for (ProductItem item : items) {
            // Primary index: by product ID
            inventoryByProductId.put(item.getProductId(), item);
            // Secondary index: by product_item id
            if (item.getId() != null) inventoryById.put(item.getId(), item);
        }

        lastRefreshTime = System.currentTimeMillis();
    }

    /**
     * Get inventory by product ID - O(1) lookup.
     */
    public ProductItem getByProductId(UUID productId) {
        ProductItem item = inventoryByProductId.get(productId);
        if (item != null) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
        return item;
    }

    /**
     * Get inventory by the product_item id (primary key in product_item table). O(1).
     */
    public ProductItem getById(UUID productItemId) {
        ProductItem item = inventoryById.get(productItemId);
        if (item != null) cacheHits++; else cacheMisses++;
        return item;
    }

    /**
     * Get all inventory items.
     */
    public List<ProductItem> getAll() {
        cacheHits++;
        return new ArrayList<>(allInventory);
    }

    /**
     * Update an inventory item in the cache.
     * Called after database update to keep cache in sync.
     */
    public void update(ProductItem item) {
        if (item != null && item.getProductId() != null) {
            inventoryByProductId.put(item.getProductId(), item);
            if (item.getId() != null) inventoryById.put(item.getId(), item);
            // Update in allInventory list
            allInventory = allInventory.stream()
                .map(existing -> existing.getProductId().equals(item.getProductId()) ? item : existing)
                .collect(Collectors.toList());
        }
    }

    /**
     * Add an inventory item to the cache.
     */
    public void add(ProductItem item) {
        if (item != null && item.getProductId() != null) {
            inventoryByProductId.put(item.getProductId(), item);
            if (item.getId() != null) inventoryById.put(item.getId(), item);
            allInventory.add(item);
        }
    }

    /**
     * Remove an inventory item from the cache.
     */
    public void remove(UUID productId) {
        if (productId != null) {
            ProductItem removed = inventoryByProductId.remove(productId);
            if (removed != null && removed.getId() != null) inventoryById.remove(removed.getId());
            allInventory = allInventory.stream()
                .filter(item -> !item.getProductId().equals(productId))
                .collect(Collectors.toList());
        }
    }

    /**
     * Check if inventory exists for a product.
     */
    public boolean containsProductId(UUID productId) {
        return inventoryByProductId.containsKey(productId);
    }

    /**
     * Get quantity for a product.
     */
    public int getQuantity(UUID productId) {
        ProductItem item = inventoryByProductId.get(productId);
        return item != null ? item.getQtyInStock() : 0;
    }

    /**
     * Update quantity for a product.
     */
    public void updateQuantity(UUID productId, int newQuantity) {
        ProductItem item = inventoryByProductId.get(productId);
        if (item != null) {
            item.setQtyInStock(newQuantity);
        }
    }

    /**
     * Get price for a product.
     */
    public double getPrice(UUID productId) {
        ProductItem item = inventoryByProductId.get(productId);
        return item != null ? item.getPrice() : 0.0;
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        inventoryByProductId.clear();
        inventoryById.clear();
        allInventory.clear();
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
    public int getSize() { return allInventory.size(); }
    public long getLastRefreshTime() { return lastRefreshTime; }
}

