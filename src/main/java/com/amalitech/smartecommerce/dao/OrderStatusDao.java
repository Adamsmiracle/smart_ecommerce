package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderStatusDao {
    OrderStatus findById(UUID id);
    List<OrderStatus> findAll();
    OrderStatus findByStatus(String status);
    boolean insert(OrderStatus orderStatus);
    boolean update(OrderStatus orderStatus);
    boolean delete(UUID id);
}

