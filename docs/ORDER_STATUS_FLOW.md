# How Order Status ID is Added to an Order

## Flow Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           UPDATE ORDER STATUS FLOW                           │
└─────────────────────────────────────────────────────────────────────────────┘

1. Admin clicks "Update Status" button
            │
            ▼
2. OrderController.updateOrderStatus() is called
            │
            ▼
3. Load available statuses from database
   ┌─────────────────────────────────────────────────┐
   │ List<OrderStatus> statuses =                    │
   │     orderStatusService.getAllOrderStatuses();   │
   │                                                 │
   │ Returns from order_status table:                │
   │   - UUID: abc123..., Status: "Pending"          │
   │   - UUID: def456..., Status: "Processing"       │
   │   - UUID: ghi789..., Status: "Completed"        │
   │   - UUID: jkl012..., Status: "Cancelled"        │
   └─────────────────────────────────────────────────┘
            │
            ▼
4. Show dialog with ComboBox of statuses
            │
            ▼
5. Admin selects a status (e.g., "Completed")
            │
            ▼
6. Get the OrderStatus object with its UUID
   ┌─────────────────────────────────────────────────┐
   │ OrderStatus newStatus = statusCombo.getValue(); │
   │                                                 │
   │ newStatus.getId()     → UUID: ghi789...         │
   │ newStatus.getStatus() → "Completed"             │
   └─────────────────────────────────────────────────┘
            │
            ▼
7. Set the status UUID on the Order
   ┌─────────────────────────────────────────────────┐
   │ selected.setOrderStatus(newStatus.getId());     │
   │                                                 │
   │ // This sets the order_status field in Order    │
   │ // to the UUID from the order_status table      │
   └─────────────────────────────────────────────────┘
            │
            ▼
8. Save to database via OrderService
   ┌─────────────────────────────────────────────────┐
   │ orderService.updateOrder(selected);             │
   │                                                 │
   │ // OrderDaoImpl.update() executes:              │
   │ UPDATE customer_order SET                       │
   │   ...,                                          │
   │   order_status = 'ghi789...'  ← UUID reference  │
   │ WHERE id = 'order_id...'                        │
   └─────────────────────────────────────────────────┘
```

## Key Code Sections

### 1. OrderStatus Model (`model/OrderStatus.java`)
```java
public class OrderStatus {
    private UUID id;      // Primary key in order_status table
    private String status; // "Pending", "Processing", "Completed", "Cancelled"
    
    // Getters and setters...
}
```

### 2. Order Model (`model/Order.java`)
```java
public class Order {
    private UUID id;
    private UUID userId;
    private UUID orderStatus;  // ← Foreign key to order_status.id
    // ... other fields
    
    public UUID getOrderStatus() { return orderStatus; }
    public void setOrderStatus(UUID orderStatus) { this.orderStatus = orderStatus; }
}
```

### 3. The Key Line in `updateOrderStatus()` (OrderController.java)
```java
result.ifPresent(newStatus -> {
    // Store original for rollback
    UUID originalStatusId = selected.getOrderStatus();

    // ★★★ THIS IS WHERE THE STATUS ID IS SET ★★★
    selected.setOrderStatus(newStatus.getId());  // Set UUID from OrderStatus
    
    // Update cache for display
    orderStatusCache.put(newStatus.getId(), newStatus.getStatus());
    tblOrders.refresh();

    // Save to database
    Task<Boolean> updateTask = new Task<>() {
        @Override
        protected Boolean call() throws Exception {
            return orderService.updateOrder(selected);  // Saves to DB
        }
        // ...
    };
});
```

### 4. Database Update (`dao/OrderDaoImpl.java`)
```java
@Override
public boolean update(Order order) {
    String sql = "UPDATE customer_order SET " +
        "user_id = ?, order_date = ?, payment_method_id = ?, " +
        "shipping_address_id = ?, shipping_method_id = ?, " +
        "order_total = ?, order_status = ? " +  // ← order_status column
        "WHERE id = ?";
    
    // ...
    stmt.setObject(7, order.getOrderStatus());  // Sets the UUID
    // ...
}
```

## Database Schema Relationship

```sql
-- order_status table (lookup table)
CREATE TABLE order_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR NOT NULL  -- e.g., "Pending", "Completed"
);

-- customer_order table
CREATE TABLE customer_order (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    order_status UUID,  -- ← Foreign key reference
    -- ... other columns
    
    CONSTRAINT fk_order_status_id_customer_order 
        FOREIGN KEY (order_status) REFERENCES order_status(id)
);
```

## Summary

1. **OrderStatus table** contains predefined statuses with UUIDs
2. **Order.orderStatus** field holds a UUID (foreign key)
3. When admin updates status:
   - Select OrderStatus from ComboBox → get its UUID
   - Call `order.setOrderStatus(orderStatus.getId())` 
   - Call `orderService.updateOrder(order)` → saves UUID to database
4. When displaying status:
   - Read `order.getOrderStatus()` → get UUID
   - Look up in `orderStatusCache` or database → get status name

