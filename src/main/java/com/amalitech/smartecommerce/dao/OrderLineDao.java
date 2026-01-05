package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.OrderLine;
import java.util.List;
import java.util.UUID;

public interface OrderLineDao {
    OrderLine findById(UUID id);
    List<OrderLine> findAll();
    List<OrderLine> findByOrderId(UUID orderId);
    boolean insert(OrderLine orderLine);
    boolean update(OrderLine orderLine);
    boolean delete(UUID id);
}

