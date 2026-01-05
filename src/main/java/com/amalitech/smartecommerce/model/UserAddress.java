package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class UserAddress {
    private UUID id;
    private UUID userId;
    private UUID addressId;

    public UserAddress() {}

    public UserAddress(UUID id, UUID userId, UUID addressId) {
        this.id = id;
        this.userId = userId;
        this.addressId = addressId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getAddressId() { return addressId; }
    public void setAddressId(UUID addressId) { this.addressId = addressId; }
}

