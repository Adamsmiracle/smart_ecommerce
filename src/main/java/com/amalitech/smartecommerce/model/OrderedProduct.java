package com.amalitech.smartecommerce.model;

import java.util.UUID;

// Placeholder for ordered_product table, as referenced by user_review. Adjust fields as needed if table is defined.
public class OrderedProduct {
    private UUID id;
    // Add other fields if the table is defined in your schema

    public OrderedProduct() {}

    public OrderedProduct(UUID id) {
        this.id = id;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
}

