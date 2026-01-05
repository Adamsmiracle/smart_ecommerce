package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class PaymentType {
    private UUID id;
    private String value;

    public PaymentType() {}

    public PaymentType(UUID id, String value) {
        this.id = id;
        this.value = value;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

