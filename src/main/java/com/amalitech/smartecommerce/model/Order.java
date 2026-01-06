package com.amalitech.smartecommerce.model;

import java.time.LocalDate;
import java.util.UUID;

public class Order {
    private UUID id;
    private UUID userId;
    private LocalDate orderDate;
    private UUID paymentMethodId;
    private UUID shippingAddressId;
    private UUID shippingMethodId;
    private String shippingMethodName; // Store shipping method name for display
    private Double orderTotal;
    private UUID orderStatus;

    public Order() {}

    public Order(UUID id, UUID userId, LocalDate orderDate, UUID paymentMethodId, UUID shippingAddressId, UUID shippingMethodId, Double orderTotal, UUID orderStatus) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.paymentMethodId = paymentMethodId;
        this.shippingAddressId = shippingAddressId;
        this.shippingMethodId = shippingMethodId;
        this.orderTotal = orderTotal;
        this.orderStatus = orderStatus;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public UUID getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(UUID paymentMethodId) { this.paymentMethodId = paymentMethodId; }

    public UUID getShippingAddressId() { return shippingAddressId; }
    public void setShippingAddressId(UUID shippingAddressId) { this.shippingAddressId = shippingAddressId; }

    public UUID getShippingMethodId() { return shippingMethodId; }
    public void setShippingMethodId(UUID shippingMethodId) { this.shippingMethodId = shippingMethodId; }

    public String getShippingMethodName() { return shippingMethodName; }
    public void setShippingMethodName(String shippingMethodName) { this.shippingMethodName = shippingMethodName; }

    public Double getOrderTotal() { return orderTotal; }
    public void setOrderTotal(Double orderTotal) { this.orderTotal = orderTotal; }

    public UUID getOrderStatus() { return orderStatus; }
    public void setOrderStatus(UUID orderStatus) { this.orderStatus = orderStatus; }
}
