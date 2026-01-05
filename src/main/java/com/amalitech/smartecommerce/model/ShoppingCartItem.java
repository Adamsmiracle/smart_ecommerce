package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class ShoppingCartItem {
    private UUID id;
    private UUID cartId;
    private UUID productItemId;
    private Integer quantity;

    public ShoppingCartItem() {}

    public ShoppingCartItem(UUID id, UUID cartId, UUID productItemId, Integer quantity) {
        this.id = id;
        this.cartId = cartId;
        this.productItemId = productItemId;
        this.quantity = quantity;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCartId() { return cartId; }
    public void setCartId(UUID cartId) { this.cartId = cartId; }

    public UUID getProductItemId() { return productItemId; }
    public void setProductItemId(UUID productItemId) { this.productItemId = productItemId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}

