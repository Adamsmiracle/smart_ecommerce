package com.amalitech.smartecommerce.dto;

import com.amalitech.smartecommerce.constants.ValidationMessages;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for updating an existing order.
 * Typically used by admin to update order status.
 */
public class OrderUpdateDto {

    @NotNull(message = ValidationMessages.ORDER_ID_REQUIRED)
    private UUID id;

    @NotNull(message = "Order status is required")
    private UUID orderStatusId;

    private UUID shippingMethodId;

    private UUID shippingAddressId;

    public OrderUpdateDto() {}

    public OrderUpdateDto(UUID id, UUID orderStatusId, UUID shippingMethodId, UUID shippingAddressId) {
        this.id = id;
        this.orderStatusId = orderStatusId;
        this.shippingMethodId = shippingMethodId;
        this.shippingAddressId = shippingAddressId;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrderStatusId() { return orderStatusId; }
    public void setOrderStatusId(UUID orderStatusId) { this.orderStatusId = orderStatusId; }

    public UUID getShippingMethodId() { return shippingMethodId; }
    public void setShippingMethodId(UUID shippingMethodId) { this.shippingMethodId = shippingMethodId; }

    public UUID getShippingAddressId() { return shippingAddressId; }
    public void setShippingAddressId(UUID shippingAddressId) { this.shippingAddressId = shippingAddressId; }
}
