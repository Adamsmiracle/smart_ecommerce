# Smart E-Commerce Project Structure

## Overview

A JavaFX desktop e-commerce application built with:
- **Java 25** + **JavaFX 21**
- **PostgreSQL** database
- **Maven** build system
- **MVC Architecture** (Model-View-Controller)
- **DAO Pattern** for data access
- **Jakarta Bean Validation** for input validation

---

## Project Directory Structure

```
smart_ecommerce/
├── pom.xml                          # Maven configuration
├── README.md                        # Project readme
├── mvnw / mvnw.cmd                  # Maven wrapper
│
├── docs/                            # Documentation
│   ├── PROJECT_STRUCTURE.md         # This file
│   ├── PROJECT_OVERVIEW.md          # High-level overview
│   ├── PRODUCT_DATA_FLOW.md         # Product flow documentation
│   ├── ORDER_STATUS_FLOW.md         # Order status flow
│   ├── SESSION_MANAGEMENT.md        # Session handling docs
│   └── ...                          # Other documentation
│
└── src/main/
    ├── java/
    │   ├── module-info.java         # Java module definition
    │   └── com/amalitech/smartecommerce/
    │       ├── app/                 # Application entry point
    │       ├── cache/               # In-memory caching
    │       ├── constants/           # Constants & messages
    │       ├── controller/          # JavaFX controllers
    │       ├── dao/                 # Data Access Objects
    │       ├── dto/                 # Data Transfer Objects
    │       ├── exception/           # Custom exceptions
    │       ├── model/               # Entity models
    │       ├── service/             # Business logic
    │       └── utils/               # Utility classes
    │
    └── resources/com/amalitech/smartecommerce/
        ├── *.fxml                   # JavaFX view files
        └── styles.css               # CSS styling
```

---

## Package Details

### 📦 `app/` - Application Entry Point
| File | Description |
|------|-------------|
| `Main.java` | JavaFX Application class, loads initial view |
| `Launcher.java` | Application launcher (workaround for module issues) |

### 💾 `cache/` - In-Memory Caching
| File | Description |
|------|-------------|
| `CategoryCache.java` | Caches product categories |
| `OrderCache.java` | Caches orders for fast retrieval |
| `ProductCache.java` | Caches products with search indexes |
| `UserCache.java` | Caches users with email/ID lookup |

### 📝 `constants/` - Constants & Messages
| File | Description |
|------|-------------|
| `ValidationMessages.java` | Centralized validation error messages |

### 🎮 `controller/` - JavaFX Controllers
| File | Description |
|------|-------------|
| `LoginController.java` | Handles login/registration |
| `AdminDashboardController.java` | Admin panel main controller |
| `CustomerDashboardController.java` | Customer panel main controller |
| `ProductController.java` | Product management (admin) |
| `CategoryController.java` | Category management (admin) |
| `OrderController.java` | Order management (admin) |
| `UserController.java` | User management (admin) |
| `InventoryController.java` | Inventory management (admin) |
| `PerformanceController.java` | Performance monitoring (admin) |
| `DashboardController.java` | Dashboard view controller |
| `HelloController.java` | Sample/test controller |

### 🗄️ `dao/` - Data Access Objects
| File | Description |
|------|-------------|
| `DAO.java` | Generic DAO interface with CRUD operations |
| `UserDao.java` / `UserDaoImpl.java` | User database operations |
| `ProductDao.java` / `ProductDaoImpl.java` | Product database operations |
| `ProductCategoryDao.java` / `ProductCategoryDaoImpl.java` | Category database operations |
| `OrderDao.java` / `OrderDaoImpl.java` | Order database operations |
| `OrderLineDao.java` / `OrderLineDaoImpl.java` | Order line items operations |
| `OrderStatusDao.java` / `OrderStatusDaoImpl.java` | Order status operations |
| `ShippingMethodDao.java` / `ShippingMethodDaoImpl.java` | Shipping methods operations |
| `ShoppingCartDao.java` / `ShoppingCartDaoImpl.java` | Shopping cart operations |
| `UserReviewDao.java` / `UserReviewDaoImpl.java` | Product reviews operations |

### 📨 `dto/` - Data Transfer Objects
| File | Description |
|------|-------------|
| `UserCreateDto.java` | User creation input (with validation) |
| `UserUpdateDto.java` | User update input (password optional) |
| `ProductCreateDto.java` | Product creation input |
| `ProductUpdateDto.java` | Product update input |
| `OrderCreateDto.java` | Order creation input |
| `OrderUpdateDto.java` | Order update input (status change) |

> **Note**: DTO-to-Model conversion is done inline in controllers for simplicity.

### ⚠️ `exception/` - Custom Exceptions
| File | Description |
|------|-------------|
| `EmailAlreadyExistsException.java` | Thrown when email is duplicate |

### 📊 `model/` - Entity Models
| File | Description |
|------|-------------|
| `User.java` | User entity |
| `Product.java` | Product entity |
| `ProductCategory.java` | Product category entity |
| `ProductItem.java` | Product inventory item (price, stock) |
| `Order.java` | Order entity |
| `OrderLine.java` | Order line item entity |
| `OrderStatus.java` | Order status entity |
| `ShippingMethod.java` | Shipping method entity |
| `ShoppingCart.java` | Shopping cart entity |
| `ShoppingCartItem.java` | Cart item entity |
| `UserAddress.java` | User address entity |
| `UserReview.java` | Product review entity |
| `Address.java` | Address entity |
| `Country.java` | Country entity |
| `PaymentType.java` | Payment type entity |
| `UsePaymentMethod.java` | User payment method entity |
| `Variation.java` | Product variation entity |
| `VariationOption.java` | Variation option entity |
| `ProductConfiguration.java` | Product configuration entity |
| `OrderedProduct.java` | Ordered product details |
| `SchemaCreator.java` | Database schema initialization |
| `*.sql` | SQL schema/migration files |

### ⚙️ `service/` - Business Logic
| File | Description |
|------|-------------|
| `UserService.java` / `UserServiceImpl.java` | User business logic |
| `ProductService.java` / `ProductServiceImpl.java` | Product business logic |
| `ProductCategoryService.java` / `ProductCategoryServiceImpl.java` | Category business logic |
| `OrderService.java` / `OrderServiceImpl.java` | Order business logic |
| `OrderStatusService.java` / `OrderStatusServiceImpl.java` | Order status business logic |
| `ShippingMethodService.java` / `ShippingMethodServiceImpl.java` | Shipping business logic |
| `ShoppingCartService.java` / `ShoppingCartServiceImpl.java` | Cart business logic |
| `UserReviewService.java` / `UserReviewServiceImpl.java` | Review business logic |

### 🔧 `utils/` - Utility Classes
| File | Description |
|------|-------------|
| `DBConnection.java` | Database connection manager |
| `DBConfig.java` | Database configuration |
| `DBTest.java` | Database connection testing |
| `SessionManager.java` | User session management (singleton) |
| `CartManager.java` | Shopping cart management |
| `ValidationUtil.java` | Jakarta Bean Validation utility |
| `InputValidator.java` | Manual input validation helpers |
| `JwtUtils.java` | JWT token utilities |
| `UserUtils.java` | User-related utilities |
| `PerformanceMonitor.java` | Performance tracking |

---

## View Files (FXML)

| File | Description |
|------|-------------|
| `login-view.fxml` | Login/Registration screen |
| `admin-dashboard.fxml` | Admin main dashboard |
| `customer-dashboard.fxml` | Customer main dashboard |
| `product-view.fxml` | Product management view |
| `category-view.fxml` | Category management view |
| `order-view.fxml` | Order management view |
| `user-view.fxml` | User management view |
| `inventory-view.fxml` | Inventory management view |
| `performance-view.fxml` | Performance monitoring view |
| `dashboard-view.fxml` | Generic dashboard view |
| `hello-view.fxml` | Sample/test view |
| `styles.css` | Application styling |

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │    FXML     │  │ Controllers │  │    CSS      │              │
│  │   Views     │◄─┤  (JavaFX)   │  │   Styles    │              │
│  └─────────────┘  └──────┬──────┘  └─────────────┘              │
└──────────────────────────┼──────────────────────────────────────┘
                           │ Uses DTOs for input
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                         DTO LAYER                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ UserCreate  │  │ ProductCreate│ │ OrderCreate │              │
│  │    Dto      │  │    Dto      │  │    Dto      │              │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
│         │ ValidationUtil  │               │                      │
│         └────────┬────────┴───────────────┘                      │
│                  │ Jakarta Validation                            │
│                  ▼                                               │
│  ┌─────────────────────────────────────────┐                    │
│  │     Inline Mapping in Controllers       │                    │
│  │   (DTO fields → Model entity fields)    │                    │
│  └────────────────────┬────────────────────┘                    │
└───────────────────────┼─────────────────────────────────────────┘
                        │ Converts to Models
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                       SERVICE LAYER                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ UserService │  │ProductService│ │ OrderService│              │
│  │    Impl     │  │    Impl     │  │    Impl     │              │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
│         │                │               │                       │
│         │    Business Logic & Validation │                       │
│         └────────┬───────┴───────────────┘                       │
└──────────────────┼──────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CACHING LAYER                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ UserCache   │  │ProductCache │  │ OrderCache  │              │
│  │  (O(1))     │  │   (O(1))    │  │   (O(1))    │              │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
└─────────┼────────────────┼────────────────┼─────────────────────┘
          │                │                │
          ▼                ▼                ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DAO LAYER                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │  UserDao    │  │ ProductDao  │  │  OrderDao   │              │
│  │   Impl      │  │    Impl     │  │    Impl     │              │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
│         │                │               │                       │
│         │    PreparedStatement (SQL)     │                       │
│         └────────┬───────┴───────────────┘                       │
└──────────────────┼──────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE LAYER                              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    PostgreSQL                           │    │
│  │   Users, Products, Orders, Categories, etc.             │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Examples

### 1. User Registration Flow
```
User fills form → LoginController
                       │
                       ▼
              UserCreateDto (validated)
                       │
                       ▼
              Inline mapping (DTO → User)
                       │
                       ▼
              UserService.createUser()
                       │
                       ├──► Hash password (BCrypt)
                       │
                       ▼
              UserDao.create()
                       │
                       ▼
              INSERT INTO users → PostgreSQL
```

### 2. Product Listing Flow (with Cache)
```
CustomerDashboard loads → ProductService.getAllProducts()
                                    │
                                    ▼
                          Check ProductCache
                                    │
                         ┌──────────┴──────────┐
                         │                     │
                    Cache HIT            Cache MISS
                         │                     │
                         │                     ▼
                         │           ProductDao.findAll()
                         │                     │
                         │                     ▼
                         │           Load into Cache
                         │                     │
                         └─────────┬───────────┘
                                   │
                                   ▼
                          Return List<Product>
```

### 3. Order Creation Flow
```
Customer checkout → OrderCreateDto
                          │
                          ▼
                  ValidationUtil.validate()
                          │
                          ▼
                  Inline mapping (DTO → Order)
                          │
                          ▼
                  OrderService.createOrder()
                          │
                          ├──► Create Order record
                          ├──► Create OrderLine records
                          ├──► Update inventory
                          │
                          ▼
                  OrderDao.create()
                          │
                          ▼
                  INSERT INTO shop_order → PostgreSQL
```

---

## Key Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 25 | Programming language |
| JavaFX | 21.0.6 | Desktop UI framework |
| PostgreSQL | - | Database |
| Maven | - | Build tool |
| BCrypt | 0.4 | Password hashing |
| Jakarta Validation | 3.0.2 | Input validation |
| Hibernate Validator | 8.0.0 | Validation implementation |
| JWT | 0.12.3 | Token utilities |
| java-dotenv | 5.2.2 | Environment configuration |

---

## How to Add New Features

### Adding a New Entity

1. **Create Model** in `model/`
   ```java
   public class NewEntity {
       private UUID id;
       // fields, getters, setters
   }
   ```

2. **Create DAO Interface** in `dao/`
   ```java
   public interface NewEntityDao extends DAO<NewEntity> {
       // custom methods
   }
   ```

3. **Create DAO Implementation** in `dao/`
   ```java
   public class NewEntityDaoImpl implements NewEntityDao {
       // JDBC implementation
   }
   ```

4. **Create Service** in `service/`
   ```java
   public interface NewEntityService { }
   public class NewEntityServiceImpl implements NewEntityService { }
   ```

5. **Create DTOs** (if user input needed) in `dto/`
   ```java
   public class NewEntityCreateDto { /* with @NotBlank etc. */ }
   public class NewEntityMapper { }
   ```

6. **Create Controller** in `controller/`
   ```java
   public class NewEntityController implements Initializable { }
   ```

7. **Create FXML View** in `resources/`

8. **Add Cache** (optional) in `cache/`

### Adding Validation to a DTO

```java
public class MyDto {
    @NotBlank(message = ValidationMessages.FIELD_REQUIRED)
    @Size(max = 100, message = "Max 100 characters")
    private String name;
    
    @Email(message = ValidationMessages.EMAIL_INVALID)
    private String email;
    
    @Positive(message = "Must be positive")
    private double price;
}
```

Then validate:
```java
Set<String> errors = ValidationUtil.validate(dto);
if (!errors.isEmpty()) {
    showError(errors.iterator().next());
    return;
}
```

---

## Database Schema

Main tables:
- `users` - User accounts
- `product` - Products
- `product_category` - Categories
- `product_item` - Product inventory (price, stock)
- `shop_order` - Orders
- `order_line` - Order items
- `order_status` - Order statuses (Pending, Processing, etc.)
- `shipping_method` - Shipping options
- `shopping_cart` / `shopping_cart_item` - Cart
- `user_address` / `address` - Addresses
- `user_review` - Product reviews

---

## Running the Application

```bash
# Compile
mvn clean compile

# Run
mvn javafx:run

# Package
mvn clean package
```

---

## Environment Variables

Create `.env` file in project root:
```
DB_URL=jdbc:postgresql://localhost:5432/ecommerce
DB_USER=your_username
DB_PASSWORD=your_password
```

