package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.Order;
import java.util.List;
import java.util.UUID;


public interface OrderDao extends DAO<Order> {
     List<Order> getOrdersByUser(UUID user_id);
}







