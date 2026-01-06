package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.ShippingMethodDao;
import com.amalitech.smartecommerce.dao.ShippingMethodDaoImpl;
import com.amalitech.smartecommerce.model.ShippingMethod;

import java.util.List;
import java.util.UUID;

public class ShippingMethodServiceImpl implements ShippingMethodService {

    private final ShippingMethodDao shippingMethodDao = new ShippingMethodDaoImpl();

    @Override
    public ShippingMethod getShippingMethodById(UUID id) {
        return shippingMethodDao.findById(id);
    }

    @Override
    public List<ShippingMethod> getAllShippingMethods() {
        return shippingMethodDao.findAll();
    }

    @Override
    public boolean createShippingMethod(ShippingMethod shippingMethod) {
        return shippingMethodDao.insert(shippingMethod);
    }

    @Override
    public boolean updateShippingMethod(ShippingMethod shippingMethod) {
        return shippingMethodDao.update(shippingMethod);
    }

    @Override
    public boolean deleteShippingMethod(UUID id) {
        return shippingMethodDao.delete(id);
    }
}

