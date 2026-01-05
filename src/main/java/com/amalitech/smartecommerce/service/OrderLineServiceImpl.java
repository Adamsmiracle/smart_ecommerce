package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.OrderLineDao;
import com.amalitech.smartecommerce.dao.OrderLineDaoImpl;
import com.amalitech.smartecommerce.model.OrderLine;

import java.util.List;
import java.util.UUID;

public class OrderLineServiceImpl implements OrderLineService {
    private final OrderLineDao orderLineDao;

    public OrderLineServiceImpl() {
        this.orderLineDao = new OrderLineDaoImpl();
    }

    public OrderLineServiceImpl(OrderLineDao orderLineDao) {
        this.orderLineDao = orderLineDao;
    }

    @Override
    public OrderLine getOrderLineById(UUID id) {
        return orderLineDao.findById(id);
    }

    @Override
    public List<OrderLine> getAllOrderLines() {
        return orderLineDao.findAll();
    }

    @Override
    public List<OrderLine> getOrderLinesByOrderId(UUID orderId) {
        return orderLineDao.findByOrderId(orderId);
    }

    @Override
    public boolean createOrderLine(OrderLine orderLine) {
        return orderLineDao.insert(orderLine);
    }

    @Override
    public boolean updateOrderLine(OrderLine orderLine) {
        return orderLineDao.update(orderLine);
    }

    @Override
    public boolean deleteOrderLine(UUID id) {
        return orderLineDao.delete(id);
    }
}

