package com.amalitech.smartecommerce.dto;

import com.amalitech.smartecommerce.constants.ValidationMessages;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a new order.
 * Used when customer places an order.
 */
public class OrderCreateDto {

    @NotNull(message = ValidationMessages.ORDER_USER_REQUIRED)
    private UUID userId;
    @NotNull(message = ValidationMessages.SHIPPING_METHOD_REQUIRED)
    private UUID shippingMethodId;
    private UUID shippingAddressId;
    private UUID paymentMethodId;
    @Positive(message = ValidationMessages.ORDER_TOTAL_POSITIVE)
    private double orderTotal;

    // List of items in the order (product ID and quantity)
    private List<OrderItemDto> items;

    public OrderCreateDto() {}

    public OrderCreateDto(UUID userId, UUID shippingMethodId, UUID shippingAddressId, UUID paymentMethodId, double orderTotal, List<OrderItemDto> items) {
        this.userId = userId;
        this.shippingMethodId = shippingMethodId;
        this.shippingAddressId = shippingAddressId;
        this.paymentMethodId = paymentMethodId;
        this.orderTotal = orderTotal;
        this.items = items;
    }

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getShippingMethodId() { return shippingMethodId; }
    public void setShippingMethodId(UUID shippingMethodId) { this.shippingMethodId = shippingMethodId; }

    public UUID getShippingAddressId() { return shippingAddressId; }
    public void setShippingAddressId(UUID shippingAddressId) { this.shippingAddressId = shippingAddressId; }

    public UUID getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(UUID paymentMethodId) { this.paymentMethodId = paymentMethodId; }

    public double getOrderTotal() { return orderTotal; }
    public void setOrderTotal(double orderTotal) { this.orderTotal = orderTotal; }

    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    /**
     * Inner class for order line items.
     */
    public static class OrderItemDto {
        private UUID productId;
        private int quantity;
        private double price;

        public OrderItemDto() {}

        public OrderItemDto(UUID productId, int quantity, double price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }
}
