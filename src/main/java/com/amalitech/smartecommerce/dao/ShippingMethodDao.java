package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.ShippingMethod;

import java.util.List;
import java.util.UUID;

public interface ShippingMethodDao extends DAO<ShippingMethod>{
    double getShippingCost(UUID shipment_method_id);

}

