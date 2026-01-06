package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.model.ShippingMethod;

import java.util.List;
import java.util.UUID;

public interface ShippingMethodService {
    ShippingMethod getShippingMethodById(UUID id);
    List<ShippingMethod> getAllShippingMethods();
    boolean createShippingMethod(ShippingMethod shippingMethod);
    boolean updateShippingMethod(ShippingMethod shippingMethod);
    boolean deleteShippingMethod(UUID id);
}

