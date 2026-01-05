package com.amalitech.smartecommerce.service;

import java.util.UUID;
import java.util.List;
import com.amalitech.smartecommerce.model.OrderLine;


public interface OrderLineService {
    boolean deleteOrderLine(UUID id);
    boolean updateOrderLine(OrderLine orderLine);
    boolean createOrderLine(OrderLine orderLine);
    List<OrderLine> getOrderLinesByOrderId(UUID orderId);
    List<OrderLine> getAllOrderLines();
    OrderLine getOrderLineById(UUID id);
    }