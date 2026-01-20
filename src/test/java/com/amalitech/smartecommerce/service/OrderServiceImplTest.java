package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.OrderDao;
import com.amalitech.smartecommerce.model.Order;
import com.amalitech.smartecommerce.model.OrderLine;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceImplTest {

    static class StubOrderDao implements OrderDao {
        Order lastModifiedOrder = null;
        boolean modifyCalled = false;

        @Override
        public Order delete(UUID id) { return null; }

        @Override
        public Order update(Order order) { return null; }

        @Override
        public List<Order> getOrdersByUser(UUID user_id) { return null; }

        @Override
        public List<com.amalitech.smartecommerce.controller.CustomerDashboardController.OrderItemDetail> getOrderItemsForCustomer(UUID orderId) { return null; }

        @Override
        public Order createWithLines(Order order, List<OrderLine> orderLines) throws SQLException { return null; }

        @Override
        public Order modifyOrderLines(UUID orderId, List<OrderLine> newOrderLines) throws SQLException {
            modifyCalled = true;
            // Return updated order
            Order o = new Order(orderId, UUID.randomUUID(), LocalDate.now(), null, null, null, 10.0, null);
            lastModifiedOrder = o;
            return o;
        }

        @Override
        public List<Order> findAll() { return null; }

        @Override
        public Order findById(UUID id) {
            // Return a pending order (no shipping method, no status)
            return new Order(id, UUID.randomUUID(), LocalDate.now(), null, null, null, 5.0, null);
        }

        @Override
        public Order create(Order order) { return null; }

        @Override
        public List<com.amalitech.smartecommerce.model.OrderLine> getOrderLinesRaw(UUID orderId) { return new ArrayList<>(); }
    }

    @Test
    void modifyOrderLines_delegatesToDao_whenOrderEditable() throws SQLException {
        StubOrderDao stub = new StubOrderDao();
        OrderServiceImpl service = new OrderServiceImpl(stub);

        UUID orderId = UUID.randomUUID();
        List<OrderLine> lines = new ArrayList<>();
        Order result = service.modifyOrderLines(orderId, lines);

        assertTrue(stub.modifyCalled, "DAO.modifyOrderLines should be called");
        assertNotNull(result, "Result should not be null");
        assertEquals(orderId, result.getId());
    }
}
