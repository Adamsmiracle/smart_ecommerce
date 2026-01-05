package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.OrderDao;
import com.amalitech.smartecommerce.dao.OrderDaoImpl;
import com.amalitech.smartecommerce.model.Order;

import java.util.List;
import java.util.UUID;

public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;

    public OrderServiceImpl() {
        this.orderDao = new OrderDaoImpl();
    }

    public OrderServiceImpl(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @Override
    public Order getOrderById(UUID id) {
        return orderDao.findById(id);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderDao.findAll();
    }

    @Override
    public boolean createOrder(Order order) {
        return orderDao.insert(order);
    }

    @Override
    public boolean updateOrder(Order order) {
        return orderDao.update(order);
    }

    @Override
    public boolean deleteOrder(UUID id) {
        return orderDao.delete(id);
    }
}

