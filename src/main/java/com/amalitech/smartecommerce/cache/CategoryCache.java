package com.amalitech.smartecommerce.cache;

import com.amalitech.smartecommerce.model.ProductCategory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for product categories.
 */
public class CategoryCache {
    private static CategoryCache instance;

    private final Map<UUID, ProductCategory> categoryById;
    private final Map<UUID, List<ProductCategory>> categoriesByParent;
    private List<ProductCategory> allCategories;

    private long cacheHits = 0;
    private long cacheMisses = 0;

    private CategoryCache() {
        this.categoryById = new ConcurrentHashMap<>();
        this.categoriesByParent = new ConcurrentHashMap<>();
        this.allCategories = new ArrayList<>();
    }

    public static synchronized CategoryCache getInstance() {
        if (instance == null) {
            instance = new CategoryCache();
        }
        return instance;
    }

    public void loadAll(List<ProductCategory> categories) {
        clear();
        this.allCategories = new ArrayList<>(categories);

        for (ProductCategory category : categories) {
            categoryById.put(category.getId(), category);

            UUID parentId = category.getParentCategoryId();
            if (parentId != null) {
                categoriesByParent
                    .computeIfAbsent(parentId, k -> new ArrayList<>())
                    .add(category);
            }
        }
    }

    public ProductCategory getById(UUID id) {
        ProductCategory category = categoryById.get(id);
        if (category != null) cacheHits++; else cacheMisses++;
        return category;
    }

    public List<ProductCategory> getAll() {
        cacheHits++;
        return new ArrayList<>(allCategories);
    }

    public List<ProductCategory> getSubcategories(UUID parentId) {
        List<ProductCategory> subs = categoriesByParent.get(parentId);
        if (subs != null) {
            cacheHits++;
            return new ArrayList<>(subs);
        }
        cacheMisses++;
        return new ArrayList<>();
    }

    public List<ProductCategory> getRootCategories() {
        return allCategories.stream()
            .filter(c -> c.getParentCategoryId() == null)
            .toList();
    }

    public void put(ProductCategory category) {
        categoryById.put(category.getId(), category);
        allCategories.add(category);

        if (category.getParentCategoryId() != null) {
            categoriesByParent
                .computeIfAbsent(category.getParentCategoryId(), k -> new ArrayList<>())
                .add(category);
        }
    }

    public void remove(UUID id) {
        ProductCategory category = categoryById.remove(id);
        if (category != null) {
            allCategories.remove(category);
            if (category.getParentCategoryId() != null) {
                List<ProductCategory> siblings = categoriesByParent.get(category.getParentCategoryId());
                if (siblings != null) siblings.remove(category);
            }
        }
    }

    public void update(ProductCategory category) {
        remove(category.getId());
        put(category);
    }

    public void clear() {
        categoryById.clear();
        categoriesByParent.clear();
        allCategories.clear();
    }

    public long getCacheHits() { return cacheHits; }
    public long getCacheMisses() { return cacheMisses; }
    public int getSize() { return allCategories.size(); }
}

