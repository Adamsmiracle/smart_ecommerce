package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.Order;
import java.util.List;
import java.util.UUID;

public interface OrderDao {
    Order findUserOrderById(UUID id);
    List<Order> findAll();
    boolean create(Order order);
    boolean update(Order order);
    boolean delete(UUID id);
}

