package com.amalitech.smartecommerce.service;

import java.util.List;
import java.util.UUID;
import com.amalitech.smartecommerce.model.ShoppingCart;


public interface ShoppingCartService {
    boolean deleteShoppingCart(UUID id);

    boolean updateShoppingCart(ShoppingCart cart);

    boolean createShoppingCart(ShoppingCart cart);

    List<ShoppingCart> getShoppingCartsByUserId(UUID userId);

    List<ShoppingCart> getAllShoppingCarts();

    ShoppingCart getShoppingCartById(UUID id);

}


