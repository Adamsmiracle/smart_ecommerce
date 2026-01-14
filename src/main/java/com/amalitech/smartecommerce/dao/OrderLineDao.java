package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.OrderLine;
import java.util.List;
import java.util.UUID;

public interface OrderLineDao extends DAO<OrderLine> {


    List<OrderLine> findByOrderId(UUID orderId);
}

