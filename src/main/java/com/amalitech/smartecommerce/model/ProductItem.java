package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class ProductItem {
    private UUID id;
    private UUID productId;
    private int qtyInStock;
    private double price;
    private String image;

    public ProductItem() {}

    public ProductItem(UUID id, UUID productId, int qtyInStock, double price, String image) {
        this.id = id;
        this.productId = productId;
        this.qtyInStock = qtyInStock;
        this.price = price;
        this.image = image;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public int getQtyInStock() { return qtyInStock; }
    public void setQtyInStock(int qtyInStock) { this.qtyInStock = qtyInStock; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}

