package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class ShippingMethod {
    private UUID id;
    private String name;
    private double price;

    public ShippingMethod() {}

    public ShippingMethod(UUID id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}

