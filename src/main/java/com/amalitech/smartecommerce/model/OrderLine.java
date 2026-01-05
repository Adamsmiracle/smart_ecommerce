package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class OrderLine {
    private UUID id;
    private UUID productItemId;
    private UUID orderId;
    private int qty;
    private Double price;

    public OrderLine() {}

    public OrderLine(UUID id, UUID productItemId, UUID orderId, int qty, Double price) {
        this.id = id;
        this.productItemId = productItemId;
        this.orderId = orderId;
        this.qty = qty;
        this.price = price;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProductItemId() { return productItemId; }
    public void setProductItemId(UUID productItemId) { this.productItemId = productItemId; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

