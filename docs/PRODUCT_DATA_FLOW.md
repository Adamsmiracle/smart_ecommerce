# Product Data Flow: Database to Frontend

## Flow Overview

```
Database → DAO → Service → Controller → FXML View
```

## Step-by-Step Flow

### 1. **Database Layer** (SQL)
```sql
-- Products and ProductItems tables
SELECT p.*, pi.price, pi.quantity
FROM products p
JOIN product_items pi ON p.id = pi.product_id;
```

### 2. **Model Classes**

#### Product.java
```java
package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class Product {
    private UUID id;
    private UUID categoryId;
    private String name;
    private String description;
    private String productImage;

    public Product() {}

    public Product(UUID id, UUID categoryId, String name, String description, String productImage) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.productImage = productImage;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
}
```

#### ProductItem.java
```java
package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class ProductItem {
    private UUID id;
    private UUID productId;
    private double price;
    private int quantity;
    private Product product;

    public ProductItem() {}

    public ProductItem(UUID id, UUID productId, double price, int quantity) {
        this.id = id;
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
```

### 3. **DAO Layer** (Data Access)

```java
// ProductDAO.java
package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductItem;
import com.amalitech.smartecommerce.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductDAO {

    /**
     * Retrieves all products from the database
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Retrieves a single product by ID
     * @param id Product UUID
     * @return Product object or null if not found
     */
    public Product getProductById(UUID id) {
        String sql = "SELECT * FROM products WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all product items (variants) for a specific product
     * @param productId Product UUID
     * @return List of ProductItems
     */
    public List<ProductItem> getProductItemsByProductId(UUID productId) {
        List<ProductItem> items = new ArrayList<>();
        String sql = "SELECT * FROM product_items WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, productId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ProductItem item = new ProductItem();
                item.setId(UUID.fromString(rs.getString("id")));
                item.setProductId(UUID.fromString(rs.getString("product_id")));
                item.setPrice(rs.getDouble("price"));
                item.setQuantity(rs.getInt("quantity"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Maps a ResultSet row to a Product object
     * @param rs ResultSet from database query
     * @return Product object
     * @throws SQLException
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(UUID.fromString(rs.getString("id")));
        product.setCategoryId(UUID.fromString(rs.getString("category_id")));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setProductImage(rs.getString("product_image"));
        return product;
    }
}
```

### 4. **Service Layer** (Business Logic)

```java
// ProductService.java
package com.amalitech.smartecommerce.service;

import com.amalitech.smartecommerce.dao.ProductDAO;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductItem;

import java.util.List;
import java.util.UUID;

public class ProductService {
    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    /**
     * Gets all products
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    /**
     * Gets a single product by ID
     * @param id Product UUID
     * @return Product object
     */
    public Product getProductById(UUID id) {
        return productDAO.getProductById(id);
    }

    /**
     * Gets all product items/variants for a product
     * @param productId Product UUID
     * @return List of ProductItems
     */
    public List<ProductItem> getProductItems(UUID productId) {
        return productDAO.getProductItemsByProductId(productId);
    }

    /**
     * Gets the price of the first product item variant
     * @param productId Product UUID
     * @return Price of the product
     */
    public double getProductPrice(UUID productId) {
        List<ProductItem> items = getProductItems(productId);
        if (!items.isEmpty()) {
            return items.get(0).getPrice();
        }
        return 0.0;
    }

    /**
     * Searches products by name or description
     * @param keyword Search keyword
     * @return List of matching products
     */
    public List<Product> searchProducts(String keyword) {
        return productDAO.getAllProducts().stream()
            .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(keyword.toLowerCase()))
            .toList();
    }
}
```

### 5. **Controller Layer** (UI Logic)

```java
// ProductController.java
package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductItem;
import com.amalitech.smartecommerce.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    @FXML private FlowPane productGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Label productCountLabel;

    private final ProductService productService = new ProductService();
    private ObservableList<Product> productList;

    /**
     * Initializes the controller and loads products
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadProducts();
        setupSearchListener();
    }

    /**
     * Loads all products from database
     */
    private void loadProducts() {
        List<Product> products = productService.getAllProducts();
        productList = FXCollections.observableArrayList(products);
        displayProducts(products);
        updateProductCount(products.size());
    }

    /**
     * Displays products in the grid view
     * @param products List of products to display
     */
    private void displayProducts(List<Product> products) {
        productGrid.getChildren().clear();

        for (Product product : products) {
            VBox productCard = createProductCard(product);
            productGrid.getChildren().add(productCard);
        }
    }

    /**
     * Creates a product card UI component
     * @param product Product to display
     * @return VBox containing product card
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");

        // Product name label
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");

        // Product price (fetched from ProductItem)
        double price = productService.getProductPrice(product.getId());
        Label priceLabel = new Label(String.format("$%.2f", price));
        priceLabel.getStyleClass().add("product-price");

        // Add to cart button
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.setOnAction(e -> handleAddToCart(product));

        card.getChildren().addAll(nameLabel, priceLabel, addToCartBtn);
        return card;
    }

    /**
     * Sets up search field listener
     */
    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Product> filtered = productService.searchProducts(newVal);
            displayProducts(filtered);
            updateProductCount(filtered.size());
        });
    }

    /**
     * Handles search button click
     */
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        List<Product> results = productService.searchProducts(keyword);
        displayProducts(results);
    }

    /**
     * Handles add to cart action
     * @param product Product to add to cart
     */
    @FXML
    private void handleAddToCart(Product product) {
        System.out.println("Added to cart: " + product.getName());
    }

    /**
     * Updates the product count label
     * @param count Number of products displayed
     */
    private void updateProductCount(int count) {
        productCountLabel.setText("Showing " + count + " products");
    }
}
```

### 6. **FXML View**

```xml
<!-- product-view.fxml -->
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<BorderPane xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.amalitech.smartecommerce.controller.ProductController"
            styleClass="main-container">

    <top>
        <HBox spacing="10" styleClass="search-bar">
            <TextField fx:id="searchField" promptText="Search products..."/>
            <Button text="Search" onAction="#handleSearch"/>
            <ComboBox fx:id="categoryFilter" promptText="Category"/>
        </HBox>
    </top>

    <center>
        <ScrollPane fitToWidth="true">
            <FlowPane fx:id="productGrid" hgap="15" vgap="15" styleClass="product-grid"/>
        </ScrollPane>
    </center>

    <bottom>
        <Label fx:id="productCountLabel" text="Showing 0 products"/>
    </bottom>

</BorderPane>
```

## Call Flow Diagram

```
User Opens Product Page
        ↓
ProductController.initialize()
        ↓
loadProducts()
        ↓
ProductService.getAllProducts()
        ↓
ProductDAO.getAllProducts()
        ↓
DatabaseConnection.getConnection()
        ↓
Execute SQL Query → ResultSet
        ↓
mapResultSetToProduct() → List<Product>
        ↓
Return to Controller
        ↓
displayProducts() → createProductCard()
        ↓
ProductService.getProductPrice() → ProductDAO.getProductItemsByProductId()
        ↓
Render in FXML FlowPane
```

## Sequence Diagram

```
Frontend User
    |
    | Clicks View Products
    |
    v
ProductController
    |
    | initialize()
    |
    v
ProductService
    |
    | getAllProducts()
    |
    v
ProductDAO
    |
    | SELECT * FROM products
    |
    v
Database
    |
    | Returns ResultSet
    |
    v
ProductDAO
    |
    | mapResultSetToProduct()
    |
    v
ProductService
    |
    | Returns List<Product>
    |
    v
ProductController
    |
    | displayProducts()
    | createProductCard()
    |
    v
FXML View
    |
    | Renders Product Grid
    |
    v
Frontend User
```

## Summary

The product data flows through the application layers:

1. **Database Layer**: Stores product and pricing information in `products` and `product_items` tables
2. **DAO Layer**: Retrieves and maps raw database results to Java model objects
3. **Service Layer**: Applies business logic, data transformations, and caching strategies
4. **Controller Layer**: Orchestrates the UI and handles user interactions
5. **FXML View Layer**: Displays the final UI components to the user

This layered architecture maintains **separation of concerns** and makes the codebase:
- **Maintainable**: Changes to database don't affect UI
- **Testable**: Each layer can be tested independently
- **Scalable**: Easy to add new features without modifying existing layers
- **Reusable**: Service layer can be used by multiple controllers

