# Smart E-Commerce Project - Complete Overview

## Table of Contents
1. [Project Summary](#project-summary)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Architecture Overview](#architecture-overview)
5. [Database Schema](#database-schema)
6. [Data Flow](#data-flow)
7. [Key Components](#key-components)
8. [Authentication & Session Management](#authentication--session-management)
9. [Caching Strategy](#caching-strategy)
10. [Validation](#validation)
11. [User Roles](#user-roles)
12. [Feature List](#feature-list)
13. [How to Add Features](#how-to-add-features)
14. [Common Patterns](#common-patterns)
15. [Troubleshooting](#troubleshooting)

---

## Project Summary

**Smart E-Commerce** is a desktop JavaFX application for managing an e-commerce platform. It includes:
- Customer-facing shopping experience (browse products, manage cart, place orders)
- Admin dashboard for managing products, categories, orders, users, and inventory
- In-memory caching for performance optimization
- PostgreSQL database for persistent storage
- BCrypt password hashing for security

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 25 |
| UI Framework | JavaFX 21 |
| Build Tool | Maven |
| Database | PostgreSQL |
| Password Hashing | BCrypt (jBCrypt 0.4) |
| JWT | jjwt-api 0.12.3 |
| Environment Config | java-dotenv 5.2.2 |
| Validation | Jakarta Bean Validation 3.0.2 + Hibernate Validator 8.0.0 |

---

## Project Structure

```
src/main/java/com/amalitech/smartecommerce/
├── app/                    # Application entry points
│   ├── Main.java           # JavaFX Application class
│   └── Launcher.java       # Launcher for module compatibility
│
├── model/                  # Domain entities (POJOs)
│   ├── User.java           # User entity
│   ├── Product.java        # Product entity
│   ├── ProductItem.java    # Product inventory item (has price)
│   ├── ProductCategory.java # Category entity
│   ├── Order.java          # Customer order
│   ├── OrderLine.java      # Order line item
│   ├── OrderStatus.java    # Order status lookup
│   ├── ShippingMethod.java # Shipping options
│   ├── ShoppingCart.java   # Shopping cart
│   ├── ShoppingCartItem.java # Cart items
│   └── ... (other models)
│
├── dao/                    # Data Access Objects
│   ├── DAO.java            # Generic DAO interface
│   ├── UserDao.java        # User DAO interface
│   ├── UserDaoImpl.java    # User DAO implementation
│   ├── ProductDao.java     # Product DAO interface
│   ├── ProductDaoImpl.java # Product DAO implementation
│   ├── OrderDao.java       # Order DAO interface
│   ├── OrderDaoImpl.java   # Order DAO implementation
│   └── ... (other DAOs)
│
├── service/                # Business logic layer
│   ├── UserService.java    # User service interface
│   ├── UserServiceImpl.java # User service implementation
│   ├── ProductService.java # Product service interface
│   ├── ProductServiceImpl.java # Product service implementation
│   ├── OrderService.java   # Order service interface
│   ├── OrderServiceImpl.java # Order service implementation
│   └── ... (other services)
│
├── controller/             # JavaFX controllers
│   ├── LoginController.java # Login/Registration
│   ├── AdminDashboardController.java # Admin dashboard
│   ├── CustomerDashboardController.java # Customer dashboard
│   ├── ProductController.java # Product management
│   ├── OrderController.java # Order management
│   ├── UserController.java # User management
│   ├── CategoryController.java # Category management
│   └── InventoryController.java # Inventory management
│
├── cache/                  # In-memory caching
│   ├── UserCache.java      # User cache (singleton)
│   ├── ProductCache.java   # Product cache (singleton)
│   ├── OrderCache.java     # Order cache (singleton)
│   └── CategoryCache.java  # Category cache (singleton)
│
├── utils/                  # Utility classes
│   ├── DBConnection.java   # Database connection
│   ├── DBConfig.java       # Database configuration
│   ├── SessionManager.java # User session management
│   ├── CartManager.java    # Shopping cart state
│   ├── InputValidator.java # Input validation utilities
│   ├── ValidationUtil.java # Jakarta Bean Validation
│   ├── UserUtils.java      # Password hashing utilities
│   ├── JwtUtils.java       # JWT token utilities
│   └── PerformanceMonitor.java # Performance tracking
│
├── dto/                    # Data Transfer Objects
│   └── UserDto.java        # User DTO with validation
│
└── exception/              # Custom exceptions
    └── EmailAlreadyExistsException.java
```

### Resources
```
src/main/resources/com/amalitech/smartecommerce/
├── login-view.fxml         # Login/Register screen
├── admin-dashboard.fxml    # Admin main dashboard
├── customer-dashboard.fxml # Customer main dashboard
├── product-view.fxml       # Product management
├── category-view.fxml      # Category management
├── order-view.fxml         # Order management
├── user-view.fxml          # User management
├── inventory-view.fxml     # Inventory management
└── styles.css              # Application styles
```

---

## Architecture Overview

The application follows a **layered architecture**:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ FXML Views  │  │ Controllers  │  │ CSS Styles       │   │
│  └─────────────┘  └──────────────┘  └──────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                    SERVICE LAYER                             │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │UserService  │  │ProductService│  │ OrderService     │   │
│  └─────────────┘  └──────────────┘  └──────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                    CACHING LAYER                             │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ UserCache   │  │ProductCache  │  │ OrderCache       │   │
│  └─────────────┘  └──────────────┘  └──────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                    DATA ACCESS LAYER                         │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ UserDao     │  │ ProductDao   │  │ OrderDao         │   │
│  └─────────────┘  └──────────────┘  └──────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                    DATABASE                                  │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                   PostgreSQL                         │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Responsibility |
|-------|---------------|
| **Presentation** | UI rendering, user input, event handling |
| **Controller** | Handle UI events, coordinate service calls |
| **Service** | Business logic, validation, orchestration |
| **Cache** | In-memory data for fast lookups |
| **DAO** | Database CRUD operations |
| **Database** | Persistent data storage |

---

## Database Schema

### Core Tables

#### Users
```sql
app_user (
    id UUID PRIMARY KEY,
    email_address VARCHAR NOT NULL UNIQUE,
    first_name VARCHAR,
    last_name VARCHAR,
    phone_number VARCHAR,
    password VARCHAR  -- BCrypt hashed
)
```

#### Products
```sql
product (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL,  -- FK to product_category
    name VARCHAR NOT NULL,
    description TEXT,
    product_image TEXT
)

product_item (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,   -- FK to product
    qty_in_stock INTEGER NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    image VARCHAR
)
```

#### Orders
```sql
customer_order (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,       -- FK to app_user
    order_date DATE NOT NULL,
    payment_method_id UUID,      -- FK to use_payment_method
    shipping_address_id UUID,    -- FK to address
    shipping_method_id UUID,     -- FK to shipping_method
    order_total DOUBLE PRECISION NOT NULL,
    order_status UUID            -- FK to order_status
)

order_line (
    id UUID PRIMARY KEY,
    product_item_id UUID NOT NULL,  -- FK to product_item
    order_id UUID NOT NULL,         -- FK to customer_order
    qty INTEGER NOT NULL,
    price DOUBLE PRECISION
)
```

#### Categories & Lookup Tables
```sql
product_category (
    id UUID PRIMARY KEY,
    parent_category_id UUID,  -- Self-referencing for hierarchy
    category_name VARCHAR NOT NULL
)

order_status (
    id UUID PRIMARY KEY,
    status VARCHAR NOT NULL  -- e.g., 'Pending', 'Completed', 'Cancelled'
)

shipping_method (
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL,
    price DOUBLE PRECISION NOT NULL
)
```

### Entity Relationships

```
app_user
    │
    ├──< customer_order (one user has many orders)
    │       │
    │       └──< order_line (one order has many lines)
    │               │
    │               └──> product_item (each line references a product item)
    │                       │
    │                       └──> product (each item belongs to a product)
    │                               │
    │                               └──> product_category
    │
    ├──< shopping_cart (one user has one cart)
    │       │
    │       └──< shopping_cart_item (one cart has many items)
    │
    └──< user_review (one user can write many reviews)
```

---

## Data Flow

### Example: Product Data Flow (Database to UI)

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Database   │───>│  ProductDao  │───>│ProductService│───>│  Controller  │
│              │    │   (JDBC)     │    │  (Business)  │    │   (JavaFX)   │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
                                               │
                                               v
                                        ┌──────────────┐
                                        │ ProductCache │
                                        │  (In-Memory) │
                                        └──────────────┘
```

### Function Call Stack: Creating a Product

```java
// 1. Controller receives UI event
ProductController.handleCreateProduct()
    │
    ├── Validates input (InputValidator)
    │
    ├── Creates Product object
    │
    └── Calls ProductService.createProductWithPrice(product, price, stock)
            │
            ├── Validates business rules
            │
            ├── Sets UUID if null
            │
            └── Calls ProductDao.create(product)
                    │
                    ├── Executes INSERT SQL
                    │
                    └── Returns created Product
            │
            └── Creates ProductItem via createProductItem()
            │
            └── Updates ProductCache.put(product)
```

### Function Call Stack: Deleting an Order

```java
// 1. Controller receives delete request
OrderController.handleDeleteOrder(orderId)
    │
    ├── Shows confirmation dialog
    │
    ├── Removes from UI immediately (optimistic update)
    │
    └── Calls OrderService.deleteOrder(orderId) in background Task
            │
            ├── Validates orderId not null
            │
            └── Calls OrderDao.delete(orderId)
                    │
                    ├── BEGIN TRANSACTION
                    │
                    ├── DELETE FROM user_review WHERE ordered_product_id IN 
                    │   (SELECT id FROM order_line WHERE order_id = ?)
                    │
                    ├── DELETE FROM order_line WHERE order_id = ?
                    │
                    ├── DELETE FROM customer_order WHERE id = ?
                    │
                    ├── COMMIT TRANSACTION
                    │
                    └── Returns deleted Order
            │
            └── Updates OrderCache.remove(orderId)
```

---

## Key Components

### 1. Generic DAO Interface
```java
public interface DAO<T> {
    T findById(UUID id);
    List<T> findAll();
    T create(T t);      // Returns created entity
    T update(T t);      // Returns updated entity
    T delete(UUID id);  // Returns deleted entity
}
```

### 2. Session Manager (Singleton)
```java
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private boolean isAdmin;
    
    public static SessionManager getInstance() { ... }
    public User getCurrentUser() { ... }
    public void setCurrentUser(User user) { ... }
    public boolean isLoggedIn() { ... }
    public void logout() { ... }
}
```

### 3. Cache Pattern (Singleton with ConcurrentHashMap)
```java
public class ProductCache {
    private static ProductCache instance;
    private final Map<UUID, Product> productById;          // O(1) lookup
    private final Map<UUID, List<Product>> productsByCategory;  // Secondary index
    
    public static synchronized ProductCache getInstance() { ... }
    public void loadAll(List<Product> products) { ... }
    public Product getById(UUID id) { ... }
    public void put(Product product) { ... }
    public void remove(UUID id) { ... }
}
```

### 4. Input Validator
```java
public class InputValidator {
    public static boolean isValidEmail(String email) { ... }
    public static boolean isValidPhone(String phone) { ... }
    public static boolean isValidName(String name) { ... }
    public static boolean isValidPassword(String password) { ... }
    public static boolean isStrongPassword(String password) { ... }
    public static String getEmailError(String email) { ... }
    public static String getNameError(String name, String fieldName) { ... }
}
```

---

## Authentication & Session Management

### Login Flow
```
1. User enters email/password
2. LoginController validates input format
3. Calls UserService.getUserByEmail(email)
4. Verifies password with UserUtils.verifyPassword(plain, hashed)
5. If valid: SessionManager.getInstance().setCurrentUser(user)
6. Navigate to appropriate dashboard (Admin or Customer)
```

### Password Handling
```java
// Hashing (on registration/update)
String hashedPassword = UserUtils.hashPassword(plainPassword);

// Verification (on login)
boolean valid = UserUtils.verifyPassword(plainPassword, hashedPassword);

// Migration (legacy passwords to BCrypt)
if (UserUtils.needsHashMigration(storedPassword)) {
    String newHash = UserUtils.hashPassword(plainPassword);
    user.setPassword(newHash);
    userService.updateUser(user);
}
```

### Admin Detection
- Admin is identified by specific email: `admin@smartecommerce.com`
- On login, `SessionManager.setAdmin(true/false)` is called
- Different dashboard loaded based on role

---

## Caching Strategy

### Cache Structure
Each cache is a **singleton** using **ConcurrentHashMap** for thread safety.

| Cache | Primary Index | Secondary Indexes |
|-------|---------------|-------------------|
| UserCache | `id -> User` | `email -> User` |
| ProductCache | `id -> Product` | `categoryId -> List<Product>`, `nameToken -> List<Product>` |
| OrderCache | `id -> Order` | `userId -> List<Order>` |
| CategoryCache | `id -> ProductCategory` | `parentId -> List<ProductCategory>` |

### Cache Operations
```java
// Load from database (on app start or refresh)
List<Product> products = productService.getAllProducts();
productCache.loadAll(products);

// Read (check cache first)
Product product = productCache.getById(id);
if (product == null) {
    product = productService.getProductById(id);
    productCache.put(product);
}

// Create (update DB then cache)
Product created = productService.createProduct(product);
productCache.put(created);

// Delete (update UI, then DB, then cache)
orderTable.getItems().remove(selectedOrder);  // Optimistic UI
orderService.deleteOrder(id);                 // Background
orderCache.remove(id);                        // Sync cache
```

### Cache Statistics
```java
// Available on each cache
long hits = productCache.getCacheHits();
long misses = productCache.getCacheMisses();
double hitRate = (double) hits / (hits + misses);
```

---

## Validation

### Controller Layer (Format Validation)
```java
// Using InputValidator
String emailError = InputValidator.getEmailError(email);
if (emailError != null) {
    showError(lblError, emailError);
    return;
}
```

### Service Layer (Business Validation)
```java
// In UserServiceImpl.createUser()
if (user == null) return null;
if (email == null || email.trim().isEmpty()) return null;
if (userDao.findByEmail(email) != null) {
    throw new EmailAlreadyExistsException(email);
}
```

### DTO Layer (Jakarta Bean Validation)
```java
public class UserDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String emailAddress;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}

// Validate
Set<String> errors = ValidationUtil.validate(userDto);
if (!errors.isEmpty()) {
    // Show errors to user
}
```

---

## User Roles

| Role | Access | Dashboard |
|------|--------|-----------|
| Customer | Browse products, cart, orders, profile | `customer-dashboard.fxml` |
| Admin | Manage products, categories, orders, users, inventory | `admin-dashboard.fxml` |

### Admin Features
- View/Edit/Delete Products
- Manage Categories (hierarchical)
- View/Update Order Status
- Manage Users
- View Inventory
- Performance Monitoring

### Customer Features
- Browse Products by Category
- Search Products
- Add to Cart
- Place Orders
- View Order History
- Cancel Orders
- Edit Profile

---

## Feature List

### Implemented ✅
- [x] User Registration with BCrypt password hashing
- [x] User Login with password verification
- [x] Admin Dashboard with statistics
- [x] Customer Dashboard with product browsing
- [x] Product CRUD operations
- [x] Category CRUD operations (hierarchical)
- [x] Order Management (create, view, update status, delete)
- [x] Shopping Cart
- [x] Order Status workflow (Pending -> Completed -> Cancelled)
- [x] Shipping Methods
- [x] In-memory caching (User, Product, Order, Category)
- [x] Input validation (format + business rules)
- [x] Optimistic UI updates (delete from UI immediately)
- [x] Async data loading with loading indicators
- [x] Session Management

### Potential Additions 🔮
- [ ] User roles in database (instead of hardcoded admin email)
- [ ] Payment integration
- [ ] Product reviews/ratings
- [ ] Wishlist
- [ ] Discount codes/coupons
- [ ] Email notifications
- [ ] Order tracking
- [ ] Product images (file upload)
- [ ] Pagination for large datasets
- [ ] Advanced search/filters
- [ ] Reports/Analytics
- [ ] Multi-language support
- [ ] Dark mode

---

## How to Add Features

### Adding a New Entity

1. **Create Model** (`model/NewEntity.java`)
```java
public class NewEntity {
    private UUID id;
    private String name;
    // getters, setters, constructors
}
```

2. **Create DAO Interface** (`dao/NewEntityDao.java`)
```java
public interface NewEntityDao extends DAO<NewEntity> {
    // Additional methods if needed
}
```

3. **Create DAO Implementation** (`dao/NewEntityDaoImpl.java`)
```java
public class NewEntityDaoImpl implements NewEntityDao {
    @Override
    public NewEntity findById(UUID id) { ... }
    @Override
    public List<NewEntity> findAll() { ... }
    @Override
    public NewEntity create(NewEntity entity) { ... }
    @Override
    public NewEntity update(NewEntity entity) { ... }
    @Override
    public NewEntity delete(UUID id) { ... }
}
```

4. **Create Service Interface** (`service/NewEntityService.java`)
```java
public interface NewEntityService {
    NewEntity getById(UUID id);
    List<NewEntity> getAll();
    NewEntity create(NewEntity entity);
    NewEntity update(NewEntity entity);
    NewEntity delete(UUID id);
}
```

5. **Create Service Implementation** (`service/NewEntityServiceImpl.java`)
```java
public class NewEntityServiceImpl implements NewEntityService {
    private final NewEntityDao dao = new NewEntityDaoImpl();
    // Implement methods with validation
}
```

6. **Create Cache (optional)** (`cache/NewEntityCache.java`)
```java
public class NewEntityCache {
    private static NewEntityCache instance;
    private final Map<UUID, NewEntity> byId = new ConcurrentHashMap<>();
    // loadAll, getById, put, remove methods
}
```

7. **Create Controller** (`controller/NewEntityController.java`)
```java
public class NewEntityController implements Initializable {
    private final NewEntityService service = new NewEntityServiceImpl();
    // FXML fields, initialize(), event handlers
}
```

8. **Create FXML View** (`resources/.../new-entity-view.fxml`)

9. **Update module-info.java** if needed

10. **Add database table** (migration script)

### Adding a New Feature to Existing Entity

1. Update Model with new field
2. Update DAO SQL queries
3. Update Service with business logic
4. Update Controller with UI logic
5. Update FXML with new UI elements
6. Update Cache if applicable

---

## Common Patterns

### Async Data Loading
```java
Task<Void> loadTask = new Task<>() {
    private List<Product> products;

    @Override
    protected Void call() throws Exception {
        products = productService.getAllProducts();
        return null;
    }

    @Override
    protected void succeeded() {
        Platform.runLater(() -> {
            displayProducts(products);
            setStatus("Loaded");
        });
    }

    @Override
    protected void failed() {
        Platform.runLater(() -> setStatus("Error"));
    }
};
new Thread(loadTask).start();
```

### Optimistic UI Delete
```java
private void deleteOrder(Order order) {
    // 1. Remove from UI immediately
    orderTable.getItems().remove(order);
    
    // 2. Delete from database in background
    Task<Order> deleteTask = new Task<>() {
        @Override
        protected Order call() throws Exception {
            return orderService.deleteOrder(order.getId());
        }

        @Override
        protected void succeeded() {
            orderCache.remove(order.getId());
        }

        @Override
        protected void failed() {
            // Rollback: add back to UI
            Platform.runLater(() -> orderTable.getItems().add(order));
        }
    };
    new Thread(deleteTask).start();
}
```

### Transaction in DAO
```java
public Order delete(UUID id) {
    Connection conn = null;
    try {
        conn = DBConnection.getConnection();
        conn.setAutoCommit(false);  // Start transaction

        // Delete related records first
        // ... multiple DELETE statements ...

        conn.commit();  // Commit if all successful
        return deletedOrder;
    } catch (SQLException e) {
        if (conn != null) conn.rollback();  // Rollback on error
        return null;
    } finally {
        if (conn != null) conn.setAutoCommit(true);  // Reset
    }
}
```

---

## Troubleshooting

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| "Cannot resolve symbol 'jakarta'" | Module not reading validation packages | Add `requires jakarta.validation;` to module-info.java or remove module-info.java |
| CSS warnings about USE_COMPUTED_SIZE | Invalid CSS value | Remove or replace with actual pixel values |
| "Error deleting order: column does not exist" | SQL column name mismatch | Check database schema vs SQL query |
| Password verification fails | Hash format mismatch | Check if BCrypt migration is needed |
| UI not updating after DB change | Cache not synchronized | Call cache.put/remove after DB operations |
| Slow dashboard loading | No caching, DB on each request | Load cache once, read from cache |

### Debug Tips
1. Check console for SQL errors
2. Verify database connection in DBConfig
3. Check SessionManager.isLoggedIn() for auth issues
4. Use PerformanceMonitor for timing analysis
5. Check cache statistics for cache misses

---

## Quick Reference

### Package Imports
```java
// Models
import com.amalitech.smartecommerce.model.*;

// Services
import com.amalitech.smartecommerce.service.*;

// DAOs
import com.amalitech.smartecommerce.dao.*;

// Caches
import com.amalitech.smartecommerce.cache.*;

// Utils
import com.amalitech.smartecommerce.utils.*;
```

### Key Singletons
```java
SessionManager.getInstance()      // Current user session
ProductCache.getInstance()        // Product cache
UserCache.getInstance()           // User cache
OrderCache.getInstance()          // Order cache
CategoryCache.getInstance()       // Category cache
CartManager.getInstance()         // Shopping cart state
```

### Database Connection
```java
Connection conn = DBConnection.getConnection();
```

### Environment Variables
Set in `.env` file:
```
DB_URL=jdbc:postgresql://localhost:5432/ecommerce
DB_USER=postgres
DB_PASSWORD=your_password
```

---

*Generated: January 9, 2026*
*Project: Smart E-Commerce v1.0-SNAPSHOT*

