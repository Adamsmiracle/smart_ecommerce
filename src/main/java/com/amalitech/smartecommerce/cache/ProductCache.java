package com.amalitech.smartecommerce.cache;

import com.amalitech.smartecommerce.model.Product;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory cache for products using HashMap for O(1) lookups.
 * Mirrors database indexing concepts for fast retrieval.
 */
public class ProductCache {
    private static ProductCache instance;

    // Primary cache: id -> Product (mirrors primary key index)
    private final Map<UUID, Product> productById;

    // Secondary index: categoryId -> List<Product> (mirrors category_id index)
    private final Map<UUID, List<Product>> productsByCategory;

    // Secondary index: name (lowercase) -> List<Product> (mirrors name index for search)
    private final Map<String, List<Product>> productsByNameToken;

    // All products list for iteration
    private List<Product> allProducts;

    // Cache statistics for performance monitoring
    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long lastRefreshTime = 0;

    private ProductCache() {
        this.productById = new ConcurrentHashMap<>();
        this.productsByCategory = new ConcurrentHashMap<>();
        this.productsByNameToken = new ConcurrentHashMap<>();
        this.allProducts = new ArrayList<>();
    }

    public static  ProductCache getInstance() {
        if (instance == null) {
            instance = new ProductCache();
        }
        return instance;
    }

    /**
     * Load all products into cache, building indexes.
     */
    public void loadAll(List<Product> products) {
        clear();
        this.allProducts = new ArrayList<>(products);

        for (Product product : products) {
            // Primary index
            productById.put(product.getId(), product);

            // Category index
            productsByCategory
                .computeIfAbsent(product.getCategoryId(), k -> new ArrayList<>())
                .add(product);

            // Name token index (for search)
            indexProductName(product);
        }

        lastRefreshTime = System.currentTimeMillis();
    }

    private void indexProductName(Product product) {
        if (product.getName() != null) {
            String[] tokens = product.getName().toLowerCase().split("\\s+");
            for (String token : tokens) {
                productsByNameToken
                    .computeIfAbsent(token, k -> new ArrayList<>())
                    .add(product);
            }
        }
    }

    /**
     * Get product by ID - O(1) lookup.
     */
    public Product getById(UUID id) {
        Product product = productById.get(id);
        if (product != null) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
        return product;
    }

    /**
     * Get all products.
     */
    public List<Product> getAll() {
        cacheHits++;
        return new ArrayList<>(allProducts);
    }

    /**
     * Get products by category - O(1) lookup.
     */
    public List<Product> getByCategory(UUID categoryId) {
        List<Product> products = productsByCategory.get(categoryId);
        if (products != null) {
            cacheHits++;
            return new ArrayList<>(products);
        }
        cacheMisses++;
        return new ArrayList<>();
    }

    /**
     * Search products by name (case-insensitive) - uses token index.
     */
    public List<Product> searchByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAll();
        }

        String lowerQuery = query.toLowerCase().trim();
        Set<Product> results = new HashSet<>();

        // Search in token index
        for (Map.Entry<String, List<Product>> entry : productsByNameToken.entrySet()) {
            if (entry.getKey().contains(lowerQuery)) {
                results.addAll(entry.getValue());
                cacheHits++;
            }
        }

        // Also do substring match on full names
        for (Product product : allProducts) {
            if (product.getName() != null &&
                product.getName().toLowerCase().contains(lowerQuery)) {
                results.add(product);
            }
        }

        return new ArrayList<>(results);
    }

    /**
     * Add product to cache.
     */
    public void put(Product product) {
        productById.put(product.getId(), product);
        allProducts.add(product);

        productsByCategory
            .computeIfAbsent(product.getCategoryId(), k -> new ArrayList<>())
            .add(product);

        indexProductName(product);
    }

    /**
     * Remove product from cache.
     */
    public void remove(UUID id) {
        Product product = productById.remove(id);
        if (product != null) {
            allProducts.remove(product);

            List<Product> categoryProducts = productsByCategory.get(product.getCategoryId());
            if (categoryProducts != null) {
                categoryProducts.remove(product);
            }

            // Remove from name index
            if (product.getName() != null) {
                String[] tokens = product.getName().toLowerCase().split("\\s+");
                for (String token : tokens) {
                    List<Product> tokenProducts = productsByNameToken.get(token);
                    if (tokenProducts != null) {
                        tokenProducts.remove(product);
                    }
                }
            }
        }
    }

    /**
     * Update product in cache.
     */
    public void update(Product product) {
        remove(product.getId());
        put(product);
    }

    /**
     * Clear all cache data.
     */
    public void clear() {
        productById.clear();
        productsByCategory.clear();
        productsByNameToken.clear();
        allProducts.clear();
    }

    /**
     * Sort products by name using QuickSort algorithm.
     */
    public List<Product> getAllSortedByName(boolean ascending) {
        List<Product> sorted = new ArrayList<>(allProducts);
        quickSort(sorted, 0, sorted.size() - 1, ascending);
        return sorted;
    }

    /**
     * Sort products by price (requires ProductItem data, simplified here).
     */
    public List<Product> getAllSortedByNameDescending() {
        return getAllSortedByName(false);
    }

    private void quickSort(List<Product> list, int low, int high, boolean ascending) {
        if (low < high) {
            int pi = partition(list, low, high, ascending);
            quickSort(list, low, pi - 1, ascending);
            quickSort(list, pi + 1, high, ascending);
        }
    }

    private int partition(List<Product> list, int low, int high, boolean ascending) {
        String pivot = list.get(high).getName() != null ? list.get(high).getName().toLowerCase() : "";
        int i = low - 1;

        for (int j = low; j < high; j++) {
            String current = list.get(j).getName() != null ? list.get(j).getName().toLowerCase() : "";
            boolean shouldSwap = ascending ? current.compareTo(pivot) < 0 : current.compareTo(pivot) > 0;

            if (shouldSwap) {
                i++;
                Collections.swap(list, i, j);
            }
        }
        Collections.swap(list, i + 1, high);
        return i + 1;
    }

    /**
     * Binary search for product by name (requires sorted list).
     */
    public Product binarySearchByName(String name) {
        List<Product> sorted = getAllSortedByName(true);
        int left = 0, right = sorted.size() - 1;
        String target = name.toLowerCase();

        while (left <= right) {
            int mid = left + (right - left) / 2;
            String midName = sorted.get(mid).getName() != null ?
                sorted.get(mid).getName().toLowerCase() : "";

            int cmp = midName.compareTo(target);
            if (cmp == 0) {
                cacheHits++;
                return sorted.get(mid);
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        cacheMisses++;
        return null;
    }

    // Performance statistics
    public long getCacheHits() { return cacheHits; }
    public long getCacheMisses() { return cacheMisses; }
    public double getHitRate() {
        long total = cacheHits + cacheMisses;
        return total > 0 ? (double) cacheHits / total * 100 : 0;
    }
    public int getSize() { return allProducts.size(); }
    public long getLastRefreshTime() { return lastRefreshTime; }

    public void resetStats() {
        cacheHits = 0;
        cacheMisses = 0;
    }
}

