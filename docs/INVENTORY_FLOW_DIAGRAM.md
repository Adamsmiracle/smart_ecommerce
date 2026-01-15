# Inventory Quantity Update - Complete Flow Diagram

## Sequence Diagram: Edit Quantity Flow

```
User                    InventoryController         ProductService          ProductItemDao          Database
 |                             |                         |                      |                    |
 |------ Click "Edit qty" ----->|                        |                      |                    |
 |                             |                        |                      |                    |
 |         Show Dialog         |                        |                      |                    |
 |<-------------- Dialog Input ----------|              |                      |                    |
 |                             |                        |                      |                    |
 |      Parse & Validate       |                        |                      |                    |
 |------ Valid Input --------->|                        |                      |                    |
 |                             |                        |                      |                    |
 |        Update UI:           |                        |                      |                    |
 |        - Update item qty    |                        |                      |                    |
 |        - Refresh table      |                        |                      |                    |
 |        - Update summary     |                        |                      |                    |
 |<--------- UI Updated --------|                        |                      |                    |
 |                             |                        |                      |                    |
 |                             |---- Task (Async) ----->|                      |                    |
 |                             |  updateProductStock()  |                      |                    |
 |                             |                        |--- updateProductQuantity() ->|             |
 |                             |                        |    SQL UPDATE             |-- Execute --|
 |                             |                        |    (Background Thread)    |              |
 |                             |                        |                          |<- Success/Fail -|
 |                             |                        |<------- Result ----------|              |
 |                             |<------- Boolean -------|                         |              |
 |                             |                        |                         |              |
 |                    On Success:                       |                         |              |
 |                  (Silent - OK)                       |                         |              |
 |                             |                        |                         |              |
 |                    On Failure:                       |                         |              |
 |<-- Alert: "DB Error" -------|                        |                         |              |
 |
 Note: Total latency felt by user = Dialog input time (< 100ms)
       Database persistence happens in background
```

## Class Interaction Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                      InventoryController                             │
├─────────────────────────────────────────────────────────────────────┤
│ Fields:                                                              │
│  - productItemMap<UUID, ProductItem>     [Tracks items for DB]      │
│  - inventoryQuantities<UUID, Integer>    [In-memory cache]          │
│  - TableView<InventoryItem> tblInventory [UI display]               │
│                                                                      │
│ Methods:                                                             │
│  + loadInventory()                       [Populate maps + UI]        │
│  + showEditQuantityDialog()              [Get user input]            │
│  + adjustQuantity()                      [Update UI + DB]            │
│  + updateProductQuantity()               [Async persistence]         │
│                                                                      │
│ Async Task:                                                          │
│  - Gets ProductItem from map                                        │
│  - Calls ProductService.updateProductStock()                        │
│  - Returns result on UI thread                                      │
└────────────┬────────────────────────────────────────────────────────┘
             │
             │ uses
             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      ProductService                                  │
├─────────────────────────────────────────────────────────────────────┤
│ Interface:                                                           │
│  + updateProductStock(ProductItem)                                  │
│                                                                      │
│ Implementation (ProductServiceImpl):                                 │
│  - Delegates to ProductItemDao                                      │
│  - Returns ProductItem on success, null on failure                  │
└────────────┬────────────────────────────────────────────────────────┘
             │
             │ uses
             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      ProductItemDao                                  │
├─────────────────────────────────────────────────────────────────────┤
│ Methods:                                                             │
│  + findById(UUID): ProductItem                                      │
│  + updateProductQuantity(ProductItem): ProductItem                 │
│    - Executes: UPDATE product_item SET qty_in_stock = ?            │
│                WHERE product_id = ?                                 │
│    - Returns updated ProductItem or null                            │
└────────────┬────────────────────────────────────────────────────────┘
             │
             │ uses JDBC
             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                               │
├─────────────────────────────────────────────────────────────────────┤
│ Table: product_item                                                  │
│  - id (UUID)                                                         │
│  - product_id (UUID) [Foreign Key]                                  │
│  - qty_in_stock (INTEGER)          ◄─── UPDATED HERE               │
│  - price (DOUBLE)                                                    │
│  - image (VARCHAR)                                                   │
└─────────────────────────────────────────────────────────────────────┘
```

## Data Flow - Detailed Steps

```
STEP 1: LOAD INVENTORY
┌─────────────────────────────────────────────────────────┐
│ loadInventory() runs in background Task                 │
│                                                          │
│ For each Product from cache/DB:                         │
│  1. Get or generate quantity (100 random initially)    │
│  2. Create ProductItem instance                         │
│  3. productItemMap.put(productId, productItem)         │
│  4. Create InventoryItem for UI display                │
│  5. Add to inventoryList observable list               │
└─────────────────────────────────────────────────────────┘

STEP 2: USER EDITS QUANTITY
┌─────────────────────────────────────────────────────────┐
│ User Input: "New quantity = 50"                         │
│                                                          │
│ Action Chain:                                           │
│  1. Parse input string to integer ✓ (validation)       │
│  2. Check >= 0 ✓ (business validation)                 │
│  3. inventoryQuantities.put(productId, 50)             │
│  4. item.setQuantity(50)                               │
│  5. tblInventory.refresh()                             │
│  6. updateSummary()                                    │
│     → UI now shows 50 (IMMEDIATE FEEDBACK)             │
│                                                          │
│  7. updateProductQuantity(productId, 50) ASYNC         │
└─────────────────────────────────────────────────────────┘

STEP 3: ASYNC DATABASE UPDATE
┌─────────────────────────────────────────────────────────┐
│ Running in: new Thread().start()                        │
│                                                          │
│ Task<Boolean> call():                                   │
│  1. ProductItem item = productItemMap.get(productId)   │
│  2. item.setQtyInStock(50)                             │
│  3. ProductItem updated =                              │
│      productService.updateProductStock(item)           │
│  4. return (updated != null)                           │
│                                                          │
│ If true:                                               │
│  → succeeded() called on UI thread                     │
│  → User sees success (or nothing if all OK)           │
│                                                          │
│ If false/exception:                                    │
│  → failed() called on UI thread                        │
│  → Show error alert to user                            │
│  → But UI changes are ALREADY COMMITTED                │
└─────────────────────────────────────────────────────────┘

STEP 4: DATABASE EXECUTION
┌─────────────────────────────────────────────────────────┐
│ In ProductItemDao.updateProductQuantity():             │
│                                                          │
│ SQL: UPDATE product_item                               │
│      SET qty_in_stock = 50                             │
│      WHERE product_id = 'abc-123-def'                  │
│                                                          │
│ Result: 1 row updated ✓                                │
│         Product with ID abc-123-def now has qty = 50   │
└─────────────────────────────────────────────────────────┘
```

## State Consistency

### Before Update
```
In-Memory:                Database:
┌──────────────┐         ┌──────────────┐
│ product_id   │         │ product_id   │
│ qty: 25      │         │ qty: 25      │
└──────────────┘         └──────────────┘
                           (Consistent ✓)
```

### During Update - UI Updated First
```
In-Memory:                Database:
┌──────────────┐         ┌──────────────┐
│ product_id   │         │ product_id   │
│ qty: 50 ◄──  │         │ qty: 25      │
└──────────────┘         └──────────────┘
   (Immediate)          (Database update pending)
   (User sees 50)       (Inconsistent temporarily)
```

### After Database Update Completes
```
In-Memory:                Database:
┌──────────────┐         ┌──────────────┐
│ product_id   │         │ product_id   │
│ qty: 50      │         │ qty: 50 ◄──  │
└──────────────┘         └──────────────┘
   (Consistent ✓)        (Async update)
```

## Exception Handling Flows

```
VALID INPUT
  └─> Update UI
       └─> Schedule Async Task
            └─> Success: Silent (UI already updated)
            └─> Failure: Show error alert (UI kept updated)

INVALID INPUT
  └─> Show error alert immediately
  └─> No UI update
  └─> No database operation

DATABASE ERROR
  └─> UI already updated
  └─> Exception caught in DAO
  └─> Task failed() called
  └─> Error alert displayed to user
  └─> Application remains stable
```

## Performance Metrics

| Operation | Thread | Time | User Feels |
|-----------|--------|------|-----------|
| Input validation | UI | <1ms | Instant response |
| UI update | UI | <10ms | Immediate visual feedback |
| Show dialog | UI | <50ms | Responsive dialog |
| DB query (async) | Background | 50-500ms | No blocking |
| Network latency | Background | 0-100ms | Invisible to user |
| **Total perceived latency** | | <50ms | **Very responsive** |


