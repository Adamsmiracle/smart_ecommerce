package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.controller.CustomerDashboardController;
import com.amalitech.smartecommerce.dao.OrderDao;
import com.amalitech.smartecommerce.dao.OrderDaoImpl;
import com.amalitech.smartecommerce.model.Order;
import com.amalitech.smartecommerce.model.OrderStatus;
import com.amalitech.smartecommerce.service.OrderStatusService;
import com.amalitech.smartecommerce.service.OrderStatusServiceImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;
    private static final Logger LOGGER = Logger.getLogger(OrderServiceImpl.class.getName());
    private final OrderStatusService orderStatusService = new OrderStatusServiceImpl();

    public OrderServiceImpl() {
        this.orderDao = new OrderDaoImpl();
    }

    public OrderServiceImpl(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    // find an order by id
    @Override
    public Order getOrderById(UUID id) {
        if (id == null) throw new IllegalArgumentException("Provide order id");
        return orderDao.findById(id);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderDao.findAll();
    }


//    Get all order by the user id
    @Override
    public List<Order> getAllOrdersByUser(UUID user_id) {
        return orderDao.getOrdersByUser(user_id);
    }

    @Override
    public Order createOrderWithLines(Order order, java.util.List<com.amalitech.smartecommerce.model.OrderLine> orderLines) throws SQLException {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        if (orderLines == null) throw new IllegalArgumentException("Order lines cannot be null");

        // ensure id and orderDate are set
        if (order.getId() == null) order.setId(UUID.randomUUID());
        if (order.getOrderDate() == null) order.setOrderDate(LocalDate.now());

        // Ensure default order status is 'Pending' when not provided
        if (order.getOrderStatus() == null) {
            try {
                OrderStatus pending = orderStatusService.getOrderStatusByName("Pending");
                if (pending != null) {
                    order.setOrderStatus(pending.getId());
                } else {
                    LOGGER.log(Level.WARNING, "Order status 'Pending' not found in database; leaving order_status null");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to fetch 'Pending' order status: {0}", ex.getMessage());
            }
        }

        // Delegate to DAO which performs transactional create + inventory update
        return orderDao.createWithLines(order, orderLines);
    }

    @Override
    public List<CustomerDashboardController.OrderItemDetail> getOrderItems(UUID order_id) {
        return orderDao.getOrderItemsForCustomer(order_id);
    }

    @Override
    public Order updateOrder(Order order) throws SQLException {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        if (order.getId() == null) throw new IllegalArgumentException("Order id is required for update");
        if (order.getOrderTotal() == null || order.getOrderTotal() < 0) throw new IllegalArgumentException("Order total must be non-negative");
        return orderDao.update(order);
    }

    @Override
    public Order deleteOrder(UUID id) {
        if (id == null) throw new IllegalArgumentException("Order id cannot be null");
        return orderDao.delete(id);
    }

    @Override
    public Order modifyOrderLines(UUID orderId, java.util.List<com.amalitech.smartecommerce.model.OrderLine> newOrderLines) throws SQLException {
        if (orderId == null) throw new IllegalArgumentException("Order id cannot be null");
        if (newOrderLines == null) throw new IllegalArgumentException("newOrderLines cannot be null");

        // Validate current order state - only allow modification if order is not yet shipped/completed
        Order existing = orderDao.findById(orderId);
        if (existing == null) throw new IllegalArgumentException("Order not found: " + orderId);

        // Determine editability:
        // - editable when order_status is null
        // - editable when shipping method is not set (no shipping assigned yet)
        // - editable when the order_status corresponds to the 'Pending' status in DB
        boolean editable = false;
        if (existing.getOrderStatus() == null) editable = true;
        if (existing.getShippingMethodId() == null) editable = true;
        if (!editable) {
            try {
                OrderStatus os = orderStatusService.getOrderStatusById(existing.getOrderStatus());
                if (os != null && "pending".equalsIgnoreCase(os.getStatus())) {
                    editable = true;
                }
            } catch (Exception ex) {
                // If looking up order status fails, default to non-editable to be safe
                editable = false;
            }
        }
        if (!editable) {
            throw new IllegalStateException("Order is not editable in its current state. Only 'Pending' orders (or orders without shipping) can be edited.");
        }

        // Delegate to DAO which performs transactional updates and inventory adjustments
        return orderDao.modifyOrderLines(orderId, newOrderLines);
    }

    @Override
    public List<com.amalitech.smartecommerce.model.OrderLine> getOrderLinesRaw(UUID orderId) {
        return orderDao.getOrderLinesRaw(orderId);
    }
}
