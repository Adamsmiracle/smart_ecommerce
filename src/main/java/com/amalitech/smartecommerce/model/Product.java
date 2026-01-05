package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class Product {
    private UUID id;
    private UUID categoryId;
    private String name;
    private String description;
    private String productImage;

    public Product() {}

    public Product(UUID id, UUID categoryId, String name, String description, String productImage) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.productImage = productImage;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
}

