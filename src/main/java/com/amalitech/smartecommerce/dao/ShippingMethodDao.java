package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.ShippingMethod;

import java.util.List;
import java.util.UUID;

public interface ShippingMethodDao {
    ShippingMethod findById(UUID id);
    List<ShippingMethod> findAll();
    boolean insert(ShippingMethod shippingMethod);
    boolean update(ShippingMethod shippingMethod);
    boolean delete(UUID id);
}

