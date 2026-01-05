package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class ProductConfiguration {
    private UUID productItemId;
    private UUID variationOptionId;

    public ProductConfiguration() {}

    public ProductConfiguration(UUID productItemId, UUID variationOptionId) {
        this.productItemId = productItemId;
        this.variationOptionId = variationOptionId;
    }

    public UUID getProductItemId() { return productItemId; }
    public void setProductItemId(UUID productItemId) { this.productItemId = productItemId; }

    public UUID getVariationOptionId() { return variationOptionId; }
    public void setVariationOptionId(UUID variationOptionId) { this.variationOptionId = variationOptionId; }
}

