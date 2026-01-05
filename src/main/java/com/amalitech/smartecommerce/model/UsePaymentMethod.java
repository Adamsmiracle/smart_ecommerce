package com.amalitech.smartecommerce.model;

import java.time.LocalDate;
import java.util.UUID;

public class UsePaymentMethod {
    private UUID id;
    private UUID userId;
    private UUID paymentTypeId;
    private String providerProvider;
    private String accountNumber;
    private LocalDate expiryDate;
    private Boolean isDefault;

    public UsePaymentMethod() {}

    public UsePaymentMethod(UUID id, UUID userId, UUID paymentTypeId, String providerProvider, String accountNumber, LocalDate expiryDate, Boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.paymentTypeId = paymentTypeId;
        this.providerProvider = providerProvider;
        this.accountNumber = accountNumber;
        this.expiryDate = expiryDate;
        this.isDefault = isDefault;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getPaymentTypeId() { return paymentTypeId; }
    public void setPaymentTypeId(UUID paymentTypeId) { this.paymentTypeId = paymentTypeId; }

    public String getProviderProvider() { return providerProvider; }
    public void setProviderProvider(String providerProvider) { this.providerProvider = providerProvider; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
