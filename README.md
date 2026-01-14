# Smart E-Commerce System

A JavaFX-based e-commerce management system with PostgreSQL database integration, featuring product management, order processing, user administration, and performance monitoring with caching optimization.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Database Setup](#database-setup)
- [SQL Scripts](#sql-scripts)
- [Running the Application](#running-the-application)
- [Usage Instructions](#usage-instructions)
- [Project Structure](#project-structure)
- [Performance Optimization](#performance-optimization)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)

## Overview

This project implements a comprehensive e-commerce backend system with a JavaFX admin interface. It demonstrates:

- **Database Design**: Normalized schema (3NF) for an e-commerce domain
- **CRUD Operations**: Full create, read, update, delete functionality via JDBC
- **Performance Optimization**: In-memory caching, indexing, and sorting algorithms
- **JavaFX UI**: Admin dashboard for managing products, categories, orders, and users
- **Input Validation**: Jakarta Bean Validation with DTOs
- **Security**: BCrypt password hashing, parameterized queries (SQL injection prevention)

## Features

### Epic 1: Database Design and Modeling
- ✅ Conceptual ERD with all major entities
- ✅ Logical model with attributes, PKs, and FKs
- ✅ Physical model with SQL data types and constraints
- ✅ Normalized to Third Normal Form (3NF)
- ✅ Database constraints prevent duplicate or invalid entries

### Epic 2: Data Access and CRUD Operations
- ✅ Product management (add, edit, delete, list)
- ✅ Category management with hierarchy support
- ✅ Order management and reporting
- ✅ User management with validation
- ✅ Input validation and error feedback
- ✅ Parameterized queries (SQL injection prevention)
- ✅ DTO pattern for data transfer

### Epic 3: Searching, Sorting, and Optimization
- ✅ Case-insensitive product search (responsive)
- ✅ Category filtering
- ✅ QuickSort implementation for product sorting
- ✅ Binary search for exact name matching
- ✅ Database indexes on frequently queried columns
- ✅ Pagination and search filters for usability

### Epic 4: Performance and Query Optimization
- ✅ In-memory caching with HashMap (O(1) lookups)
- ✅ Secondary indexes for category and name searches
- ✅ Performance monitoring and benchmarking
- ✅ Query time comparison (DB vs Cache)
- ✅ Cache invalidation logic for data consistency
- ✅ Detailed performance reports

### Epic 5: Reporting and Documentation
- ✅ ERD diagrams and database documentation
- ✅ SQL scripts with sample data
- ✅ README with setup instructions
- ✅ Performance analysis reports (see `docs/PERFORMANCE_REPORT.md`)

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     JavaFX UI Layer                         │
│  (Controllers: Dashboard, Product, Order, User, etc.)       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     DTO Layer (Validation)                  │
│   (UserCreateDto, ProductCreateDto, OrderCreateDto, etc.)   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                           │
│   (ProductService, OrderService, UserService, etc.)         │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼                               ▼
┌─────────────────────────┐     ┌─────────────────────────┐
│      Cache Layer        │     │       DAO Layer         │
│  (ProductCache,         │     │   (ProductDaoImpl,      │
│   CategoryCache,        │     │    OrderDaoImpl,        │
│   UserCache, OrderCache)│     │    UserDaoImpl, etc.)   │
└─────────────────────────┘     └─────────────────────────┘
                                              │
                                              ▼
                              ┌─────────────────────────┐
                              │   PostgreSQL Database   │
                              │  (Parameterized Queries)│
                              └─────────────────────────┘
```

## Prerequisites

- **Java 21+** (compiled with Java 25 target)
- **Maven 3.8+**
- **PostgreSQL 14+** (or Neon PostgreSQL cloud)
- **JavaFX 21+** (included via Maven dependencies)

## Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd smart_ecommerce
   ```

2. **Install dependencies**:
   ```bash
   mvn clean install
   ```

3. **Configure database connection**:
   Create a `.env` file in the project root:
   ```env
   DB_URL=jdbc:postgresql://localhost:5432/ecommerce
   DB_USER=your_username
   DB_PASSWORD=your_password
   ```

## Database Setup

### Option 1: Local PostgreSQL

1. **Create the database**:
   ```sql
   CREATE DATABASE ecommerce;
   ```

2. **Run the schema script**:
   ```bash
   psql -U your_username -d ecommerce -f src/main/java/com/amalitech/smartecommerce/model/ecommerce_schema.sql
   ```

3. **Verify the setup**:
   ```sql
   \dt  -- List all tables
   SELECT COUNT(*) FROM product;  -- Verify sample data
   ```

### Option 2: Neon Cloud PostgreSQL

1. Create a project at [neon.tech](https://neon.tech)
2. Copy the connection string
3. Update `.env` with Neon credentials

## SQL Scripts

### Schema Location
All SQL scripts are located in: `src/main/java/com/amalitech/smartecommerce/model/`

### Available Scripts

| Script | Description |
|--------|-------------|
| `ecommerce_schema.sql` | Complete database schema with tables, constraints, and indexes |

### Running SQL Scripts

**Using psql:**
```bash
# Run schema creation
psql -U postgres -d ecommerce -f src/main/java/com/amalitech/smartecommerce/model/ecommerce_schema.sql
```

**Using pgAdmin:**
1. Open pgAdmin and connect to your database
2. Open Query Tool
3. Load the SQL file and execute

### Sample Data

The schema script includes sample data for:
- Order statuses (Pending, Processing, Shipped, Delivered, Cancelled)
- Shipping methods (Standard, Express, Overnight)
- Sample categories (Electronics, Clothing, etc.)

### Creating Indexes Manually

If indexes are not included in the schema, run:
```sql
-- Product indexes for search optimization
CREATE INDEX IF NOT EXISTS idx_product_category ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_product_name ON product(LOWER(name));

-- Order indexes
CREATE INDEX IF NOT EXISTS idx_order_user ON shop_order(user_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON shop_order(order_status_id);

-- User indexes
CREATE INDEX IF NOT EXISTS idx_user_email ON app_user(email_address);
```

## Running the Application

### Using Maven:
```bash
mvn clean javafx:run
```

### Using the Launcher class:
```bash
mvn clean compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.app.Launcher"
```

### Using IntelliJ IDEA:
1. Open the project
2. Run `Launcher.java` or `Main.java`

## Usage Instructions

### Customer Features

1. **Login/Register**
   - Create account with email validation
   - Password hashing with BCrypt
   - Session management

2. **Browse Products**
   - View all products with pagination
   - Search by name (case-insensitive)
   - Filter by category
   - View product details with price

3. **Place Orders**
   - Add products to cart
   - Select shipping method
   - View order history
   - Cancel pending orders

4. **Profile Management**
   - Update personal information
   - View order history

### Admin Features

1. **Dashboard**
   - Overview of system statistics
   - Quick access to all modules

2. **Product Management**
   - Add new products with price/stock
   - Edit existing products
   - Delete products
   - Search and filter products

3. **Category Management**
   - Create product categories
   - Edit category details
   - View products by category

4. **Order Management**
   - View all orders
   - Update order status (Pending → Processing → Shipped → Delivered)
   - View order details with products
   - Delete orders

5. **User Management**
   - View all users
   - Add new users
   - Edit user details
   - Delete users
   - View user's order history

6. **Performance Monitoring**
   - Compare DB vs Cache performance
   - View cache hit/miss statistics
   - Run benchmarks

### Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Search | Enter (in search field) |
| Clear Search | Button click |
| Refresh | F5 or Refresh button |

## Project Structure

```
smart_ecommerce/
├── docs/                      # Documentation
│   ├── PERFORMANCE_REPORT.md  # Performance analysis
│   ├── PROJECT_STRUCTURE.md   # Detailed structure
│   └── ...                    # Other docs
├── src/main/java/com/amalitech/smartecommerce/
│   ├── app/
│   │   ├── Main.java          # JavaFX Application entry
│   │   └── Launcher.java      # Main class launcher
│   ├── cache/
│   │   ├── ProductCache.java  # In-memory product cache
│   │   ├── CategoryCache.java # Category cache
│   │   ├── UserCache.java     # User cache
│   │   └── OrderCache.java    # Order cache
│   ├── constants/
│   │   └── ValidationMessages.java # Validation messages
│   ├── controller/
│   │   ├── LoginController.java
│   │   ├── CustomerDashboardController.java
│   │   ├── AdminDashboardController.java
│   │   ├── ProductController.java
│   │   ├── CategoryController.java
│   │   ├── OrderController.java
│   │   ├── UserController.java
│   │   └── PerformanceController.java
│   ├── dao/
│   │   ├── DAO.java           # Generic DAO interface
│   │   ├── ProductDao.java    # Product DAO interface
│   │   ├── ProductDaoImpl.java # JDBC implementation
│   │   └── ... (other DAOs)
│   ├── dto/
│   │   ├── UserCreateDto.java # User creation DTO
│   │   ├── UserUpdateDto.java # User update DTO
│   │   ├── ProductCreateDto.java
│   │   ├── ProductUpdateDto.java
│   │   ├── OrderCreateDto.java
│   │   └── OrderUpdateDto.java
│   ├── exception/
│   │   └── EmailAlreadyExistsException.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── ProductCategory.java
│   │   ├── Order.java
│   │   ├── OrderLine.java
│   │   ├── OrderStatus.java
│   │   └── ecommerce_schema.sql # Database schema
│   ├── service/
│   │   ├── UserService.java
│   │   ├── UserServiceImpl.java
│   │   ├── ProductService.java
│   │   ├── ProductServiceImpl.java
│   │   └── ... (other services)
│   └── utils/
│       ├── DBConnection.java  # Database connection
│       ├── DBConfig.java      # Configuration loader
│       ├── SessionManager.java # Session management
│       ├── InputValidator.java # Input validation
│       ├── ValidationUtil.java # Jakarta validation
│       ├── PerformanceMonitor.java
│       └── UserUtils.java     # Password hashing
├── src/main/resources/com/amalitech/smartecommerce/
│   ├── login-view.fxml
│   ├── customer-dashboard.fxml
│   ├── admin-dashboard.fxml
│   ├── product-view.fxml
│   ├── category-view.fxml
│   ├── order-view.fxml
│   ├── user-view.fxml
│   ├── performance-view.fxml
│   └── styles.css
├── pom.xml
└── README.md
```

## Performance Optimization

### Caching Strategy

The application uses in-memory caching with HashMap for O(1) lookups:

```java
// Primary cache: ID → Entity
Map<UUID, Product> productById = new ConcurrentHashMap<>();

// Secondary indexes for common queries
Map<UUID, List<Product>> productsByCategory;
Map<String, List<Product>> productsByNameToken;
```

### Performance Metrics

| Operation | DB Query | Cache Query | Speedup |
|-----------|----------|-------------|---------|
| Get All Products | 85ms | 0.1ms | **850x** |
| Search by Name | 45ms | 1.5ms | **30x** |
| Filter by Category | 30ms | 0.2ms | **150x** |

**For detailed performance analysis, see `docs/PERFORMANCE_REPORT.md`**

### Database Indexes

The schema includes indexes on frequently queried columns:
- Primary keys on all tables
- `product.category_id` - Category filtering
- `product.name` - Name search
- `shop_order.user_id` - User orders
- `app_user.email_address` - Login lookup

## API Documentation

### DAO Layer (Data Access)

All DAOs implement parameterized queries:

```java
// Example: ProductDaoImpl
public List<Product> searchByName(String name) {
    String sql = "SELECT * FROM product WHERE LOWER(name) LIKE ?";
    stmt.setString(1, "%" + name.toLowerCase() + "%");
    // ...
}
```

### Service Layer (Business Logic)

Services handle:
- Input validation
- Password hashing
- Cache management
- Transaction coordination

### DTO Layer (Data Transfer)

DTOs with Jakarta Bean Validation:

```java
public class UserCreateDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String emailAddress;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
```

## Testing

### Manual Testing
1. Launch the application
2. Navigate through Products, Categories, Orders, Users tabs
3. Perform CRUD operations
4. Use the Performance tab to run benchmarks

### Database Connection Test
```bash
mvn exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.DBTest"
```

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify `.env` file exists with correct credentials
   - Check if PostgreSQL is running
   - Verify network connectivity (for cloud DB)

2. **JavaFX Module Errors**
   - Ensure `module-info.java` includes required opens/exports
   - Run with Maven: `mvn javafx:run`

3. **Validation Errors**
   - Ensure `module-info.java` opens DTO package to hibernate.validator

4. **CSS Warnings**
   - These are non-critical; styles still apply

### Error Messages

| Error | Cause | Solution |
|-------|-------|----------|
| "No internet connection" | DB unreachable | Check network/credentials |
| "Email already exists" | Duplicate email | Use different email |
| "Invalid password" | Wrong password | Check password case |

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| JavaFX | 21.0.6 | UI Framework |
| PostgreSQL JDBC | 42.7.8 | Database connectivity |
| java-dotenv | 5.2.2 | Environment configuration |
| Jakarta Validation | 3.0.2 | Bean validation |
| Hibernate Validator | 8.0.0 | Validation implementation |
| BCrypt (jBCrypt) | 0.4 | Password hashing |
| JUnit Jupiter | 5.12.1 | Testing framework |

## License

This project is developed for educational purposes as part of the Smart E-Commerce System coursework.

## Contributors

- Miracle Adams

---

**Note**: Ensure PostgreSQL is running and the database is properly configured before launching the application.

