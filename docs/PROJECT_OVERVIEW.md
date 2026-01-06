# Smart E-Commerce - Project Overview

## 📋 Table of Contents
1. [Project Summary](#project-summary)
2. [Architecture](#architecture)
3. [Database Schema](#database-schema)
4. [Package Structure](#package-structure)
5. [Key Features](#key-features)
6. [User Flows](#user-flows)
7. [Technology Stack](#technology-stack)
8. [Feature Status](#feature-status)
9. [Future Enhancements](#future-enhancements)

---

## 🎯 Project Summary

**Smart E-Commerce** is a JavaFX desktop application for managing an e-commerce platform. It features:
- **Customer Portal**: Browse products, add to cart, place orders
- **Admin Portal**: Manage products, categories, orders, users, and inventory
- **Caching System**: In-memory caching for performance optimization
- **Performance Monitoring**: Track database vs cache query times

---

## 🏗️ Architecture

### Design Pattern: **MVC + Service Layer + DAO Pattern**

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │   FXML      │  │ Controllers │  │      CSS Styles         │  │
│  │   Views     │◄─┤  (JavaFX)   │  │                         │  │
│  └─────────────┘  └──────┬──────┘  └─────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────────┐
│                     SERVICE LAYER                                │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ ProductService  │  │  OrderService   │  │   UserService   │  │
│  │ CategoryService │  │  CartService    │  │  StatusService  │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
└───────────┼────────────────────┼────────────────────┼───────────┘
            │                    │                    │
┌───────────┼────────────────────┼────────────────────┼───────────┐
│           │         CACHING LAYER                   │           │
│  ┌────────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐  │
│  │  ProductCache   │  │  CategoryCache  │  │   UserCache    │  │
│  │ (HashMap-based) │  │ (HashMap-based) │  │ (HashMap-based)│  │
│  └────────┬────────┘  └────────┬────────┘  └───────┬────────┘  │
└───────────┼────────────────────┼────────────────────┼───────────┘
            │                    │                    │
┌───────────┼────────────────────┼────────────────────┼───────────┐
│                       DATA ACCESS LAYER (DAO)                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │   ProductDao    │  │    OrderDao     │  │    UserDao      │  │
│  │   CategoryDao   │  │   OrderLineDao  │  │   ReviewDao     │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
└───────────┼────────────────────┼────────────────────┼───────────┘
            │                    │                    │
            └────────────────────┼────────────────────┘
                                 │
┌────────────────────────────────┼────────────────────────────────┐
│                         DATABASE                                 │
│                    PostgreSQL (UUID-based)                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │
│  │ app_user │ │ product  │ │  order   │ │ shipping_method  │   │
│  │ address  │ │ category │ │order_line│ │   order_status   │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Database Schema

### Core Tables

| Table | Description |
|-------|-------------|
| `app_user` | User accounts (customers & admins) |
| `product` | Product catalog |
| `product_category` | Hierarchical categories |
| `product_item` | Product variants with price/stock |
| `customer_order` | Order headers |
| `order_line` | Order item details |
| `order_status` | Status lookup (Pending, Processing, Completed, Cancelled) |
| `shipping_method` | Shipping options with prices |
| `shopping_cart` | User shopping carts |
| `shopping_cart_item` | Cart items |
| `user_review` | Product reviews |
| `address` | Addresses |
| `country` | Country lookup |
| `payment_type` | Payment method types |
| `use_payment_method` | User's saved payment methods |

### Key Relationships
```
app_user ──┬── customer_order ──┬── order_line ── product_item ── product
           │                    ├── order_status
           │                    ├── shipping_method
           │                    └── address
           ├── shopping_cart ── shopping_cart_item ── product_item
           ├── user_address ── address ── country
           └── use_payment_method ── payment_type
```

---

## 📦 Package Structure

```
com.amalitech.smartecommerce/
│
├── app/                          # Application entry point
│   ├── Main.java                 # JavaFX Application launcher
│   └── Launcher.java             # Module launcher
│
├── model/                        # Data models (POJOs)
│   ├── User.java
│   ├── Product.java
│   ├── ProductCategory.java
│   ├── Order.java
│   ├── OrderLine.java
│   ├── OrderStatus.java
│   ├── ShippingMethod.java
│   ├── ShoppingCart.java
│   ├── ShoppingCartItem.java
│   ├── ProductItem.java
│   ├── UserReview.java
│   ├── Address.java
│   ├── Country.java
│   └── ... (other models)
│
├── dao/                          # Data Access Objects
│   ├── UserDao.java / UserDaoImpl.java
│   ├── ProductDao.java / ProductDaoImpl.java
│   ├── ProductCategoryDao.java / ProductCategoryDaoImpl.java
│   ├── OrderDao.java / OrderDaoImpl.java
│   ├── OrderStatusDao.java / OrderStatusDaoImpl.java
│   ├── ShippingMethodDao.java / ShippingMethodDaoImpl.java
│   └── ... (other DAOs)
│
├── service/                      # Business logic layer
│   ├── UserService.java / UserServiceImpl.java
│   ├── ProductService.java / ProductServiceImpl.java
│   ├── ProductCategoryService.java / ProductCategoryServiceImpl.java
│   ├── OrderService.java / OrderServiceImpl.java
│   ├── OrderStatusService.java / OrderStatusServiceImpl.java
│   ├── ShippingMethodService.java / ShippingMethodServiceImpl.java
│   └── ... (other services)
│
├── controller/                   # JavaFX Controllers
│   ├── LoginController.java          # Login/Register
│   ├── CustomerDashboardController.java  # Customer UI
│   ├── AdminDashboardController.java     # Admin UI
│   ├── ProductController.java        # Product CRUD
│   ├── CategoryController.java       # Category CRUD
│   ├── OrderController.java          # Order management
│   ├── UserController.java           # User management
│   ├── InventoryController.java      # Inventory tracking
│   └── PerformanceController.java    # Performance metrics
│
├── cache/                        # In-memory caching
│   ├── ProductCache.java         # Product cache with indexes
│   ├── CategoryCache.java        # Category cache
│   └── UserCache.java            # User cache
│
├── utils/                        # Utilities
│   ├── DBConnection.java         # Database connection pool
│   ├── DBConfig.java             # DB configuration
│   ├── SessionManager.java       # User session singleton
│   ├── CartManager.java          # Shopping cart singleton
│   ├── PerformanceMonitor.java   # Query timing
│   ├── JwtUtils.java             # JWT utilities
│   └── UserUtils.java            # Password hashing (BCrypt)
│
└── exception/                    # Custom exceptions
    └── EmailAlreadyExistsException.java
```

---

## ✨ Key Features

### 🔐 Authentication
- **Customer Login/Register**: Email + password with BCrypt hashing
- **Admin Login**: Separate admin portal access
- **Session Management**: Singleton SessionManager tracks logged-in user

### 🛒 Customer Features
- **Product Browsing**: View products by category, search by name
- **Shopping Cart**: Add/remove items, update quantities
- **Checkout**: Select shipping method, place orders
- **Order History**: View past orders and status
- **Profile Management**: Edit personal information

### 👨‍💼 Admin Features
- **Dashboard**: Overview stats (products, orders, users, revenue)
- **Product Management**: CRUD operations with pagination
- **Category Management**: Hierarchical tree view
- **Order Management**: View orders, update status (Pending → Processing → Completed/Cancelled)
- **User Management**: View users, view their orders, delete users
- **Inventory Tracking**: Stock levels, low stock alerts
- **Performance Monitoring**: Cache hit rates, query times

### ⚡ Performance Features
- **In-Memory Caching**: HashMap-based caches for products/categories/users
- **Indexed Lookups**: O(1) by ID, fast search by name tokens
- **Optimistic UI Updates**: UI updates immediately, DB sync in background
- **Async Loading**: Background threads for data fetching
- **Loading Indicators**: Spinners while fetching data

---

## 🔄 User Flows

### Customer Flow
```
Login/Register → Customer Dashboard → Browse Products → Add to Cart → Checkout → Select Shipping → Place Order → View Orders
```

### Admin Flow
```
Admin Login → Admin Dashboard → Manage Products/Categories/Orders/Users/Inventory → View Performance
```

---

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| **Language** | Java 21+ |
| **UI Framework** | JavaFX 21 |
| **Database** | PostgreSQL |
| **Password Hashing** | BCrypt (jbcrypt) |
| **Build Tool** | Maven |
| **Config** | java-dotenv (.env files) |

### Dependencies (from pom.xml)
- `javafx-controls`, `javafx-fxml`, `javafx-web`
- `org.controlsfx:controlsfx`
- `org.postgresql:postgresql`
- `org.mindrot:jbcrypt`
- `io.jsonwebtoken:jjwt-api`
- `io.github.cdimascio:java-dotenv`

---

## 📊 Feature Status

| Feature | Status | Notes |
|---------|--------|-------|
| User Authentication | ✅ Complete | Login, Register, Sessions |
| Product Catalog | ✅ Complete | CRUD, Search, Pagination |
| Categories | ✅ Complete | Hierarchical, Tree view |
| Shopping Cart | ✅ Complete | In-memory cart manager |
| Order Placement | ✅ Complete | With shipping selection |
| Order Management | ✅ Complete | Status updates from DB |
| User Management | ✅ Complete | CRUD, View orders |
| Inventory | ✅ Complete | Simulated quantities |
| Caching | ✅ Complete | Products, Categories, Users |
| Performance Monitor | ✅ Complete | Query timing, Cache stats |
| Responsive UI | ✅ Complete | FlowPane, ScrollPane |
| Payment Integration | ❌ Not Started | Placeholder only |
| Product Reviews | ⚠️ Partial | Model exists, no UI |
| Product Variants | ⚠️ Partial | Schema exists, no UI |
| Address Management | ⚠️ Partial | Model exists, no UI |
| Email Notifications | ❌ Not Started | - |
| Reports/Analytics | ⚠️ Partial | Basic export only |

---

## 🚀 Future Enhancements

### High Priority
1. **Payment Integration** - Stripe, PayPal, Mobile Money
2. **Product Reviews UI** - Allow customers to rate/review products
3. **Product Variants** - Size, Color, etc. with ProductItem
4. **Address Management** - Save/select shipping addresses
5. **Email Notifications** - Order confirmations, status updates

### Medium Priority
6. **Wishlist** - Save products for later
7. **Discount/Coupons** - Promo codes system
8. **Advanced Search** - Filters (price range, rating, etc.)
9. **Reports Dashboard** - Sales charts, analytics
10. **Export to PDF/Excel** - Order reports, inventory reports

### Low Priority
11. **Multi-language Support** - i18n
12. **Dark Mode** - Theme switcher
13. **Barcode/QR Scanner** - For inventory
14. **Bulk Import** - CSV product import
15. **API Layer** - REST API for mobile app

---

## 📁 FXML Views

| View | Controller | Purpose |
|------|------------|---------|
| `login-view.fxml` | LoginController | Login/Register/Admin login |
| `customer-dashboard.fxml` | CustomerDashboardController | Customer portal |
| `admin-dashboard.fxml` | AdminDashboardController | Admin portal |
| `product-view.fxml` | ProductController | Product CRUD |
| `category-view.fxml` | CategoryController | Category CRUD |
| `order-view.fxml` | OrderController | Order management |
| `user-view.fxml` | UserController | User management |
| `inventory-view.fxml` | InventoryController | Inventory tracking |
| `performance-view.fxml` | PerformanceController | Performance metrics |

---

## 🔧 How to Add New Features

### Adding a New Entity (e.g., Wishlist)

1. **Create Model**: `model/Wishlist.java`
2. **Create DAO**: `dao/WishlistDao.java` + `WishlistDaoImpl.java`
3. **Create Service**: `service/WishlistService.java` + `WishlistServiceImpl.java`
4. **Create Controller**: `controller/WishlistController.java`
5. **Create View**: `resources/.../wishlist-view.fxml`
6. **Add Navigation**: Update dashboard controllers
7. **Update Cache** (if needed): `cache/WishlistCache.java`

### Adding a New Admin View

1. Create FXML file in `resources/com/amalitech/smartecommerce/`
2. Create Controller in `controller/`
3. Add button in `admin-dashboard.fxml`
4. Add navigation method in `AdminDashboardController`
5. Add CSS styles in `styles.css`

---

## 🔑 Key Classes to Know

| Class | Purpose |
|-------|---------|
| `SessionManager` | Singleton - tracks logged-in user |
| `CartManager` | Singleton - manages shopping cart |
| `ProductCache` | Singleton - in-memory product cache |
| `CategoryCache` | Singleton - in-memory category cache |
| `UserCache` | Singleton - in-memory user cache |
| `DBConnection` | Database connection factory |
| `UserUtils` | Password hashing with BCrypt |
| `PerformanceMonitor` | Query timing and statistics |

---

## 📝 Configuration

### Environment Variables (.env)
```
DB_URL=jdbc:postgresql://localhost:5432/ecommerce
DB_USER=your_username
DB_PASSWORD=your_password
```

### Admin Credentials (Hardcoded - change in production!)
```java
// In LoginController.java
ADMIN_EMAIL = "admin@smartecommerce.com"
ADMIN_PASSWORD = "admin123"
```

---

This overview should help you understand the project structure and add new features! 🎉

