package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.OrderLine;
import com.amalitech.smartecommerce.model.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderStatusDao extends DAO<OrderStatus> {
    OrderStatus findByStatus(String status);
}

