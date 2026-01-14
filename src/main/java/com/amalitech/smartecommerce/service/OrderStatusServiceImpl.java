package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.OrderStatusDao;
import com.amalitech.smartecommerce.dao.OrderStatusDaoImpl;
import com.amalitech.smartecommerce.model.OrderStatus;

import java.util.List;
import java.util.UUID;

public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderStatusDao orderStatusDao = new OrderStatusDaoImpl();

    @Override
    public OrderStatus getOrderStatusById(UUID id) {
        return orderStatusDao.findById(id);
    }

    @Override
    public List<OrderStatus> getAllOrderStatuses() {
        return orderStatusDao.findAll();
    }

    @Override
    public OrderStatus getOrderStatusByName(String status) {
        return orderStatusDao.findByStatus(status);
    }

    @Override
    public OrderStatus createOrderStatus(OrderStatus orderStatus) {
        return orderStatusDao.create(orderStatus);
    }

    @Override
    public OrderStatus updateOrderStatus(OrderStatus orderStatus) {
        return orderStatusDao.update(orderStatus);
    }

    @Override
    public OrderStatus deleteOrderStatus(UUID id) {
        return orderStatusDao.delete(id);
    }
}

