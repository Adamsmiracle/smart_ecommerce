package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class VariationOption {
    private UUID id;
    private UUID variationId;
    private String value;

    public VariationOption() {}

    public VariationOption(UUID id, UUID variationId, String value) {
        this.id = id;
        this.variationId = variationId;
        this.value = value;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getVariationId() { return variationId; }
    public void setVariationId(UUID variationId) { this.variationId = variationId; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

