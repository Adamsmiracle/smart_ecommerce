package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class ShoppingCart {
    private UUID id;
    private UUID userId;

    public ShoppingCart() {}

    public ShoppingCart(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}

