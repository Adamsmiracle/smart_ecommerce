package com.amalitech.smartecommerce.service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import com.amalitech.smartecommerce.model.ShoppingCart;


public interface ShoppingCartService {
    ShoppingCart deleteShoppingCart(UUID id) throws SQLException;

    ShoppingCart updateShoppingCart(ShoppingCart cart) throws SQLException;

    ShoppingCart createShoppingCart(ShoppingCart cart) throws SQLException;

    List<ShoppingCart> getShoppingCartsByUserId(UUID userId);

    List<ShoppingCart> getAllShoppingCarts() throws SQLException;

    ShoppingCart getShoppingCartById(UUID id);

}


