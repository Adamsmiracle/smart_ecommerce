package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class Variation {
    private UUID id;
    private UUID categoryId;
    private String name;

    public Variation() {}

    public Variation(UUID id, UUID categoryId, String name) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

