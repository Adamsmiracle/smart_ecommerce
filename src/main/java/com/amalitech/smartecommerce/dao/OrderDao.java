package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.Order;
import java.util.List;
import java.util.UUID;

public interface OrderDao {
    Order findById(UUID id);
    List<Order> findAll();
    boolean insert(Order order);
    boolean update(Order order);
    boolean delete(UUID id);
}

