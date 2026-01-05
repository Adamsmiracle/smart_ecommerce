package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class ProductCategory {
    private UUID id;
    private UUID parentCategoryId;
    private String categoryName;

    public ProductCategory() {}

    public ProductCategory(UUID id, UUID parentCategoryId, String categoryName) {
        this.id = id;
        this.parentCategoryId = parentCategoryId;
        this.categoryName = categoryName;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(UUID parentCategoryId) { this.parentCategoryId = parentCategoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}

