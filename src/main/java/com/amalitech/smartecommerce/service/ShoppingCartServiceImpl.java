package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.ShoppingCartDao;
import com.amalitech.smartecommerce.dao.ShoppingCartDaoImpl;
import com.amalitech.smartecommerce.model.ShoppingCart;

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
    public List<ShoppingCart> getAllShoppingCarts() {
        return shoppingCartDao.findAll();
    }

    @Override
    public List<ShoppingCart> getShoppingCartsByUserId(UUID userId) {
        return shoppingCartDao.findByUserId(userId);
    }

    @Override
    public boolean createShoppingCart(ShoppingCart cart) {
        return shoppingCartDao.insert(cart);
    }

    @Override
    public boolean updateShoppingCart(ShoppingCart cart) {
        return shoppingCartDao.update(cart);
    }

    @Override
    public boolean deleteShoppingCart(UUID id) {
        return shoppingCartDao.delete(id);
    }
}

