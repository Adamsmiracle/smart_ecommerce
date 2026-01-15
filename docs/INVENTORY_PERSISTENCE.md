# Inventory Persistence Implementation

## Overview
This document describes how inventory quantity updates are persisted to the database using the ProductItem model and asynchronous database operations.

## Architecture

### Components Involved

1. **InventoryController** - UI Controller that manages inventory display and user interactions
2. **ProductService/ProductServiceImpl** - Business logic layer that orchestrates CRUD operations
3. **ProductItemDao** - Data Access Object for ProductItem persistence
4. **ProductItem** - Model representing inventory item with quantity and price

## Data Flow

### 1. Loading Inventory

```
InventoryController.loadInventory()
    ↓
ProductCache.getAll() → ProductService.getAllProducts()
    ↓
For each Product:
    - Create ProductItem instance
    - Set product ID and quantity
    - Store in productItemMap<UUID, ProductItem>
    - Display in UI
```

**Key Code:**
```java
ProductItem productItem = new ProductItem();
productItem.setProductId(product.getId());
productItem.setQtyInStock(quantity);
productItemMap.put(product.getId(), productItem);
```

### 2. Updating Quantity in UI

When user edits quantity:

```
User clicks "Edit qty" button
    ↓
showEditQuantityDialog(InventoryItem)
    ↓
Parse new quantity from input
    ↓
Update UI immediately:
    - inventoryQuantities.put(productId, newQuantity)
    - item.setQuantity(newQuantity)
    - tblInventory.refresh()
    - updateSummary()
    ↓
Persist to database:
    - updateProductQuantity(productId, newQuantity)
```

### 3. Database Persistence (Asynchronous)

```
updateProductQuantity(UUID productId, int newQuantity)
    ↓
Create Task<Boolean> that:
    ↓
1. Gets ProductItem from map: productItemMap.get(productId)
2. Updates quantity: productItem.setQtyInStock(newQuantity)
3. Persists via service:
    ProductItem updated = productService.updateProductStock(productItem)
    ↓
4. ProductService delegates to DAO:
    productItemDao.updateProductQuantity(productItem)
    ↓
5. DAO executes SQL UPDATE:
    UPDATE product_item SET qty_in_stock = ? WHERE product_id = ?
    ↓
6. Return to UI thread with result
    ↓
If failed: Show warning/error to user
```

## Key Design Patterns

### 1. **Immediate UI Feedback + Asynchronous Persistence**
- **Why**: Users expect instant visual feedback
- **Implementation**: Update UI first, then persist in background thread
- **Benefit**: Application feels responsive even if database is slow

### 2. **Task-Based Async Operations**
- **Why**: Prevents UI freezing during database operations
- **Implementation**: JavaFX Task<T> with background thread execution
- **Code**:
```java
Task<Boolean> updateTask = new Task<>() {
    @Override
    protected Boolean call() throws Exception {
        // Database operation runs in background
        return productService.updateProductStock(productItem) != null;
    }
    
    @Override
    protected void succeeded() {
        // Runs on UI thread after completion
        Platform.runLater(() -> { /* handle result */ });
    }
};
new Thread(updateTask).start();
```

### 3. **ProductItem Mapping**
- **Why**: Maintain reference to ProductItem objects for persistence
- **Implementation**: `Map<UUID, ProductItem> productItemMap`
- **Benefit**: Quick lookup when updating, ensures data consistency

## Database Operations

### Update Product Quantity

**SQL:**
```sql
UPDATE product_item SET qty_in_stock = ? WHERE product_id = ?
```

**DAO Method:**
```java
public ProductItem updateProductQuantity(ProductItem productItem) {
    String sql = "UPDATE product_item SET qty_in_stock = ? WHERE product_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, productItem.getQtyInStock());
        stmt.setObject(2, productItem.getProductId());
        int rowsAffected = stmt.executeUpdate();
        
        if (rowsAffected > 0) {
            return productItem;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}
```

## UI Update Methods

### 1. Edit Quantity Dialog
```java
private void showEditQuantityDialog(InventoryItem item) {
    // User input → validation → immediate UI update → async persistence
}
```

### 2. Add Stock Dialog
```java
private void showAddStockDialog() {
    // Select product → enter quantity → adjustQuantity() → async persistence
}
```

### 3. Quantity Adjustment
```java
private void adjustQuantity(InventoryItem item, int adjustment) {
    // Calculate new quantity
    // Update UI immediately
    // Persist asynchronously
}
```

## Error Handling

### Scenarios Handled

1. **Database Update Fails**
   - UI already updated ✓
   - User notified with error alert
   - Application remains stable

2. **Invalid Input**
   - Validated before UI update
   - Error message displayed
   - Database operation never initiated

3. **Connection Issues**
   - Exception caught in DAO
   - Error logged to console
   - User informed via alert

## Summary Statistics

The inventory summary (total, in stock, low stock, out of stock) is updated whenever quantity changes:

```java
private void updateSummary() {
    int total = allInventoryItems.size();
    long inStock = allInventoryItems.stream()
        .filter(i -> i.getQuantity() > 10).count();
    long lowStock = allInventoryItems.stream()
        .filter(i -> i.getQuantity() > 0 && i.getQuantity() <= 10).count();
    long outOfStock = allInventoryItems.stream()
        .filter(i -> i.getQuantity() == 0).count();
    
    // Update UI labels
}
```

## Performance Considerations

1. **Caching**: Products are cached to avoid repeated database queries
2. **Async Operations**: Background thread prevents UI blocking
3. **In-Memory Tracking**: ProductItem map avoids redundant lookups
4. **Batch Operations**: Future enhancement opportunity for bulk updates

## Testing Checklist

- [ ] Edit quantity updates UI immediately
- [ ] Database update completes in background
- [ ] Invalid quantities rejected
- [ ] Summary statistics update correctly
- [ ] Add stock dialog increments quantity
- [ ] Error alerts display on DB failure
- [ ] Multiple rapid updates handled gracefully


