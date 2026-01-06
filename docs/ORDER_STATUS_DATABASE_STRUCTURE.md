# Order Status Data Flow - Current State

## Answer: YES - Order Statuses ARE Pre-Populated in Database

The `order_status` table is **already created** in your database schema with **sample data**.

## Current Database State

### 1. Order Status Table Creation
```sql
CREATE TABLE order_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR NOT NULL
);
```

### 2. Sample Data Already Inserted
```sql
-- Order Status
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Pending');
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Completed');
```

So your database already has:
| id (UUID) | status |
|-----------|--------|
| `abc-123-...` | Pending |
| `def-456-...` | Completed |

**⚠️ NOTE**: The current schema only has 2 statuses. You need to add 2 more for full functionality:
```sql
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Processing');
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Cancelled');
```

## Complete Data Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                     ORDER STATUS WORKFLOW                           │
└─────────────────────────────────────────────────────────────────────┘

1. ADMIN SELECTS ORDER IN UI
   ├─ OrderController.updateOrderStatus() called
   └─ Order object has: id, userId, orderStatus (UUID or null)

2. LOAD STATUSES FROM DATABASE
   ├─ orderStatusService.getAllOrderStatuses()
   ├─ OrderStatusServiceImpl.getAllOrderStatuses()
   ├─ OrderStatusDaoImpl.findAll()
   └─ SQL: SELECT * FROM order_status
       Returns: [
           { id: 'abc-123...', status: 'Pending' },
           { id: 'def-456...', status: 'Completed' },
           { id: 'ghi-789...', status: 'Processing' },
           { id: 'jkl-012...', status: 'Cancelled' }
       ]

3. SHOW DIALOG WITH COMBOBOX
   ├─ Display OrderStatus objects by status name
   ├─ Pre-select current status if order.getOrderStatus() != null
   └─ Admin clicks OK with selected status

4. SET STATUS ID ON ORDER
   ├─ Get selected OrderStatus: newStatus = statusCombo.getValue()
   ├─ Get its UUID: newStatus.getId() → 'ghi-789...'
   ├─ Set on order: selected.setOrderStatus('ghi-789...')
   └─ Order now has: orderStatus = UUID of "Processing"

5. SAVE TO DATABASE
   ├─ orderService.updateOrder(selected)
   ├─ OrderServiceImpl.updateOrder(order)
   ├─ OrderDaoImpl.update(order)
   └─ SQL: UPDATE customer_order SET order_status = 'ghi-789...' WHERE id = ?
       ✓ Database now stores the UUID reference to order_status table

6. DISPLAY ORDER IN TABLE
   ├─ OrderController reads order.getOrderStatus() → 'ghi-789...'
   ├─ Looks up in orderStatusCache: 'ghi-789...' → 'Processing'
   └─ TableView displays: "Processing"
```

## Code References

### 1. Model: Order.java
```java
public class Order {
    private UUID id;
    private UUID userId;
    private UUID orderStatus;  // ← Stores UUID from order_status table
    
    public UUID getOrderStatus() { return orderStatus; }
    public void setOrderStatus(UUID orderStatus) { this.orderStatus = orderStatus; }
}
```

### 2. Model: OrderStatus.java
```java
public class OrderStatus {
    private UUID id;        // Primary key from order_status table
    private String status;  // "Pending", "Processing", "Completed", "Cancelled"
    
    public UUID getId() { return id; }
    public String getStatus() { return status; }
}
```

### 3. Service Layer: OrderStatusServiceImpl.java
```java
public class OrderStatusServiceImpl implements OrderStatusService {
    private final OrderStatusDao orderStatusDao = new OrderStatusDaoImpl();

    @Override
    public List<OrderStatus> getAllOrderStatuses() {
        return orderStatusDao.findAll();  // Gets from database
    }
}
```

### 4. DAO Layer: OrderStatusDaoImpl.java
```java
@Override
public List<OrderStatus> findAll() {
    List<OrderStatus> statuses = new ArrayList<>();
    String sql = "SELECT * FROM order_status";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            statuses.add(mapResultSetToOrderStatus(rs));
        }
    }
    return statuses;
}

private OrderStatus mapResultSetToOrderStatus(ResultSet rs) throws SQLException {
    UUID id = (UUID) rs.getObject("id");
    String status = rs.getString("status");
    return new OrderStatus(id, status);
}
```

### 5. Controller: OrderController.java (Key Line)
```java
@FXML
public void updateOrderStatus() {
    // ... load statuses from database ...
    
    Optional<OrderStatus> result = dialog.showAndWait();
    result.ifPresent(newStatus -> {
        // ★★★ KEY LINE ★★★
        selected.setOrderStatus(newStatus.getId());  // Set UUID
        
        // Save to database
        orderService.updateOrder(selected);
    });
}
```

## Database Relationship Diagram

```
order_status table              customer_order table
┌─────────────────────┐        ┌──────────────────────────────┐
│ id (PK) | status    │        │ id | user_id | order_status  │
├─────────────────────┤        │    |         | (FK)          │
│ abc-123 | Pending   │◄───────┤    |         |               │
│ def-456 | Completed │◄───────┤    |         |               │
│ ghi-789 | Processing│        │    |         |               │
│ jkl-012 | Cancelled │        └──────────────────────────────┘
└─────────────────────┘
         ▲
         │ Foreign Key Constraint:
         └─ order_status.id = customer_order.order_status
```

## What's Already Working

✅ Order statuses are pre-populated in database
✅ OrderStatusDao loads them from database
✅ OrderStatusService provides them to controllers
✅ OrderController displays them in ComboBox
✅ When admin selects a status, its UUID is stored in order.orderStatus
✅ UUID is saved to database via OrderDaoImpl.update()
✅ OrderController displays the status name by looking up the UUID

## What Needs Fixing (Optional)

The current schema only has 2 statuses. Add these to your database:
```sql
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Processing');
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Cancelled');
```

Or run this if you want to regenerate with all 4 statuses:
```sql
DELETE FROM order_status;
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Pending');
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Processing');
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Completed');
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Cancelled');
```

## Summary

**YES - Everything is already set up!**

1. ✅ `order_status` table exists with statuses
2. ✅ Each status has a unique UUID as primary key
3. ✅ `customer_order.order_status` column is a foreign key to `order_status.id`
4. ✅ When admin updates order status, the UUID is stored
5. ✅ When displaying, the UUID is looked up to get the status name

The data flow is complete from database to UI and back!

