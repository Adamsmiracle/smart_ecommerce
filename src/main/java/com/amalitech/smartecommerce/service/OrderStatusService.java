package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.model.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderStatusService {
    OrderStatus getOrderStatusById(UUID id);
    List<OrderStatus> getAllOrderStatuses();
    OrderStatus getOrderStatusByName(String status);
    OrderStatus createOrderStatus(OrderStatus orderStatus);
    OrderStatus updateOrderStatus(OrderStatus orderStatus);
    OrderStatus deleteOrderStatus(UUID id);
}

