package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.model.ShippingMethod;

import java.util.List;
import java.util.UUID;

public interface ShippingMethodService {
    ShippingMethod getShippingMethodById(UUID id);
    List<ShippingMethod> getAllShippingMethods();
    ShippingMethod createShippingMethod(ShippingMethod shippingMethod);
    ShippingMethod updateShippingMethod(ShippingMethod shippingMethod);
    ShippingMethod deleteShippingMethod(UUID id);
}

