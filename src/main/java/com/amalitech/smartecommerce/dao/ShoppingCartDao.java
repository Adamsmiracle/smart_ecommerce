package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.ShoppingCart;
import java.util.List;
import java.util.UUID;

public interface ShoppingCartDao {
    ShoppingCart findById(UUID id);
    List<ShoppingCart> findAll();
    List<ShoppingCart> findByUserId(UUID userId);
    boolean insert(ShoppingCart cart);
    boolean update(ShoppingCart cart);
    boolean delete(UUID id);
}

