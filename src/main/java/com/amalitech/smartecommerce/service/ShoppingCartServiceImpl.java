package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.ShoppingCartDao;
import com.amalitech.smartecommerce.dao.ShoppingCartDaoImpl;
import com.amalitech.smartecommerce.model.ShoppingCart;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartDao shoppingCartDao;

    public ShoppingCartServiceImpl() {
        this.shoppingCartDao = new ShoppingCartDaoImpl();
    }

    public ShoppingCartServiceImpl(ShoppingCartDao shoppingCartDao) {
        this.shoppingCartDao = shoppingCartDao;
    }

    @Override
    public ShoppingCart getShoppingCartById(UUID id) {
        return shoppingCartDao.findById(id);
    }

    @Override
    public List<ShoppingCart> getAllShoppingCarts() throws SQLException {
        return shoppingCartDao.findAll();
    }

    @Override
    public List<ShoppingCart> getShoppingCartsByUserId(UUID userId) {
        return shoppingCartDao.findByUserId(userId);
    }

    @Override
    public ShoppingCart createShoppingCart(ShoppingCart cart) throws SQLException {
        return shoppingCartDao.create(cart);
    }

    @Override
    public ShoppingCart updateShoppingCart(ShoppingCart cart) throws SQLException {
        return shoppingCartDao.update(cart);
    }

    @Override
    public ShoppingCart deleteShoppingCart(UUID id) throws SQLException {
        return shoppingCartDao.delete(id);
    }
}

