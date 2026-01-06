# Smart E-Commerce System

A JavaFX-based e-commerce management system with PostgreSQL database integration, featuring product management, order processing, user administration, and performance monitoring with caching optimization.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Performance Optimization](#performance-optimization)
- [Testing](#testing)
- [Screenshots](#screenshots)

## Overview

This project implements a comprehensive e-commerce backend system with a JavaFX admin interface. It demonstrates:

- **Database Design**: Normalized schema (3NF) for an e-commerce domain
- **CRUD Operations**: Full create, read, update, delete functionality via JDBC
- **Performance Optimization**: In-memory caching, indexing, and sorting algorithms
- **JavaFX UI**: Admin dashboard for managing products, categories, orders, and users

## Features

### Epic 1: Database Design and Modeling
- ✅ Conceptual ERD with all major entities
- ✅ Logical model with attributes, PKs, and FKs
- ✅ Physical model with SQL data types and constraints
- ✅ Normalized to Third Normal Form (3NF)

### Epic 2: Data Access and CRUD Operations
- ✅ Product management (add, edit, delete, list)
- ✅ Category management with hierarchy support
- ✅ Order management and reporting
- ✅ User management with validation
- ✅ Input validation and error feedback
- ✅ Parameterized queries (SQL injection prevention)

### Epic 3: Searching, Sorting, and Optimization
- ✅ Case-insensitive product search
- ✅ Category filtering
- ✅ QuickSort implementation for product sorting
- ✅ Binary search for exact name matching
- ✅ Database indexes on frequently queried columns

### Epic 4: Performance and Query Optimization
- ✅ In-memory caching with HashMap (O(1) lookups)
- ✅ Secondary indexes for category and name searches
- ✅ Performance monitoring and benchmarking
- ✅ Query time comparison (DB vs Cache)
- ✅ Detailed performance reports

### Epic 5: Reporting and Documentation
- ✅ ERD diagrams and database documentation
- ✅ SQL scripts with sample data
- ✅ README with setup instructions
- ✅ Performance analysis reports

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     JavaFX UI Layer                         │
│  (DashboardController, ProductController, OrderController)  │
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
│   CategoryCache)        │     │    OrderDaoImpl, etc.)  │
└─────────────────────────┘     └─────────────────────────┘
                                              │
                                              ▼
                              ┌─────────────────────────┐
                              │   PostgreSQL Database   │
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

1. **Create the database**:
   ```sql
   CREATE DATABASE ecommerce;
   ```

2. **Run the schema script**:
   ```bash
   psql -U your_username -d ecommerce -f src/main/java/com/amalitech/smartecommerce/model/ecommerce_schema.sql
   ```

   Or in psql:
   ```sql
   \i src/main/java/com/amalitech/smartecommerce/model/ecommerce_schema.sql
   ```

3. **Verify the setup**:
   ```sql
   \dt  -- List all tables
   SELECT COUNT(*) FROM product;  -- Verify sample data
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

## Project Structure

```
smart_ecommerce/
├── src/main/java/com/amalitech/smartecommerce/
│   ├── app/
│   │   ├── Main.java              # JavaFX Application entry point
│   │   └── Launcher.java          # Main class launcher
│   ├── cache/
│   │   ├── ProductCache.java      # In-memory product cache
│   │   └── CategoryCache.java     # In-memory category cache
│   ├── controller/
│   │   ├── DashboardController.java
│   │   ├── ProductController.java
│   │   ├── CategoryController.java
│   │   ├── OrderController.java
│   │   ├── UserController.java
│   │   └── PerformanceController.java
│   ├── dao/
│   │   ├── ProductDao.java        # Product DAO interface
│   │   ├── ProductDaoImpl.java    # JDBC implementation
│   │   └── ... (other DAOs)
│   ├── exception/
│   │   └── EmailAlreadyExistsException.java
│   ├── model/
│   │   ├── Product.java
│   │   ├── ProductCategory.java
│   │   ├── Order.java
│   │   ├── User.java
│   │   └── ecommerce_schema.sql   # Database schema
│   ├── service/
│   │   ├── ProductService.java
│   │   ├── ProductServiceImpl.java
│   │   └── ... (other services)
│   └── utils/
│       ├── DBConnection.java      # Database connection manager
│       ├── DBConfig.java          # Configuration loader
│       ├── PerformanceMonitor.java
│       └── UserUtils.java
├── src/main/resources/com/amalitech/smartecommerce/
│   ├── dashboard-view.fxml
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

The application uses in-memory caching to reduce database load:

```java
// HashMap for O(1) primary key lookups (mirrors DB primary index)
Map<UUID, Product> productById = new ConcurrentHashMap<>();

// Secondary index for category-based lookups
Map<UUID, List<Product>> productsByCategory = new ConcurrentHashMap<>();

// Token-based index for name search optimization
Map<String, List<Product>> productsByNameToken = new ConcurrentHashMap<>();
```

### Sorting Algorithm

QuickSort implementation for product sorting:
- Average Time Complexity: O(n log n)
- Space Complexity: O(log n)

### Database Indexes

The schema includes indexes on frequently queried columns:
- Primary keys on all tables
- Foreign key indexes for join optimization
- Additional indexes on `product.category_id`, `customer_order.user_id`, etc.

### Performance Metrics

Run the built-in benchmark from the Performance tab to see:
- Database vs Cache query time comparison
- Cache hit/miss ratio
- Speedup factor
- Detailed operation logs

## Testing

### Manual Testing
1. Launch the application
2. Navigate through Products, Categories, Orders, Users tabs
3. Perform CRUD operations
4. Use the Performance tab to run benchmarks

### Database Connection Test
```java
// Run DBTest.java to verify database connectivity
mvn exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.DBTest"
```

## Database Schema (ERD Summary)

### Main Entities:
- **app_user**: Customer/admin accounts
- **product**: Product catalog
- **product_category**: Hierarchical categories
- **product_item**: Specific SKUs with price/stock
- **customer_order**: Order headers
- **order_line**: Order details/items
- **shopping_cart**: User shopping carts
- **user_review**: Product reviews

### Key Relationships:
- User → Orders (1:N)
- Product → Category (N:1)
- Product → ProductItems (1:N)
- Order → OrderLines (1:N)
- ProductItem → Variations via ProductConfiguration (N:M)

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| JavaFX | 21.0.6 | UI Framework |
| PostgreSQL JDBC | 42.7.8 | Database connectivity |
| java-dotenv | 5.2.2 | Environment configuration |
| JUnit Jupiter | 5.12.1 | Testing framework |

## License

This project is developed for educational purposes as part of the Smart E-Commerce System coursework.

## Contributors

- [Your Name]

---

**Note**: Ensure PostgreSQL is running and the database is properly configured before launching the application.

