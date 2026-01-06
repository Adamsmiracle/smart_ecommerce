package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.OrderDao;
import com.amalitech.smartecommerce.dao.OrderDaoImpl;
import com.amalitech.smartecommerce.model.Order;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;

    public OrderServiceImpl() {
        this.orderDao = new OrderDaoImpl();
    }

    public OrderServiceImpl(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    // find an order by id
    @Override
    public Order getOrderById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Provide order id");
        return orderDao.findUserOrderById(id);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderDao.findAll();
    }

    @Override
    public boolean createOrder(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        if (order.getUserId() == null) throw new IllegalArgumentException("Order must have a userId");
        if (order.getOrderTotal() == null || order.getOrderTotal() < 0) throw new IllegalArgumentException("Order total must be non-negative");

        // ensure id and orderDate are set
        if (order.getId() == null) order.setId(UUID.randomUUID());
        if (order.getOrderDate() == null) order.setOrderDate(LocalDate.now());

        return orderDao.create(order);
    }

    @Override
    public boolean updateOrder(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        if (order.getId() == null) throw new IllegalArgumentException("Order id is required for update");
        if (order.getOrderTotal() == null || order.getOrderTotal() < 0) throw new IllegalArgumentException("Order total must be non-negative");
        return orderDao.update(order);
    }

    @Override
    public boolean deleteOrder(UUID id) {
        if (id == null) throw new IllegalArgumentException("Order id cannot be null");
        return orderDao.delete(id);
    }
}
