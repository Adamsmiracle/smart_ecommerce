package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.ShoppingCart;
import java.util.List;
import java.util.UUID;

public interface ShoppingCartDao extends DAO<ShoppingCart> {
    List<ShoppingCart> findByUserId(UUID userId);
}

