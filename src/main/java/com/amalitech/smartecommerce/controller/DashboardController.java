package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.cache.CategoryCache;
import com.amalitech.smartecommerce.cache.ProductCache;
import com.amalitech.smartecommerce.model.Order;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductCategory;
import com.amalitech.smartecommerce.model.User;
import com.amalitech.smartecommerce.service.*;
import com.amalitech.smartecommerce.utils.PerformanceMonitor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Main dashboard controller handling navigation and overview statistics.
 */
public class DashboardController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;

    // Navigation buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnProducts;
    @FXML private Button btnCategories;
    @FXML private Button btnOrders;
    @FXML private Button btnUsers;
//    @FXML private Button btnPerformance;

    // Statistics labels
    @FXML private Label lblProductCount;
    @FXML private Label lblCategoryCount;
    @FXML private Label lblOrderCount;
    @FXML private Label lblUserCount;

    // Cache status labels
    @FXML private Label lblCacheHitRate;
    @FXML private Label lblCacheSize;
    @FXML private Label lblLastRefresh;

    // Status bar
    @FXML private Label lblStatus;
    @FXML private Label lblConnectionStatus;

    // Services
    private final ProductService productService = new ProductServiceImpl();
    private final ProductCategoryService categoryService = new ProductCategoryServiceImpl();
    private final OrderService orderService = new OrderServiceImpl();
    private final UserService userService = new UserServiceImpl();

    // Caches
    private final ProductCache productCache = ProductCache.getInstance();
    private final CategoryCache categoryCache = CategoryCache.getInstance();

    // Loaded views cache
    private Node productView;
    private Node categoryView;
    private Node orderView;
    private Node userView;
    private Node performanceView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeCaches();
        loadStatistics();
        updateCacheStatus();
        setStatus("Dashboard loaded successfully");
    }

    private void initializeCaches() {
        try {
            // Load products into cache
            long start = System.currentTimeMillis();
            List<Product> products = productService.getAllProducts();
            productCache.loadAll(products);
            long productLoadTime = System.currentTimeMillis() - start;

            // Load categories into cache
            start = System.currentTimeMillis();
            List<ProductCategory> categories = categoryService.getAllCategories();
            categoryCache.loadAll(categories);
            long categoryLoadTime = System.currentTimeMillis() - start;

            PerformanceMonitor.getInstance().recordDbOperation("Initial Product Load", productLoadTime);
            PerformanceMonitor.getInstance().recordDbOperation("Initial Category Load", categoryLoadTime);

            setStatus(String.format("Caches initialized: %d products, %d categories",
                products.size(), categories.size()));
        } catch (Exception e) {
            setStatus("Error initializing caches: " + e.getMessage());
            lblConnectionStatus.setText("Database: Error");
            lblConnectionStatus.setStyle("-fx-text-fill: red;");
        }
    }

    private void loadStatistics() {
        try {
            // Use cache for fast statistics
            lblProductCount.setText(String.valueOf(productCache.getSize()));
            lblCategoryCount.setText(String.valueOf(categoryCache.getSize()));

            // Load order count from DB
            List<Order> orders = orderService.getAllOrders();
            lblOrderCount.setText(String.valueOf(orders.size()));

            // Load user count from DB
            List<User> users = userService.getAllUsers();
            lblUserCount.setText(String.valueOf(users.size()));

        } catch (Exception e) {
            setStatus("Error loading statistics: " + e.getMessage());
        }
    }

    private void updateCacheStatus() {
        ProductCache cache = ProductCache.getInstance();
        lblCacheHitRate.setText(String.format("Hit Rate: %.1f%%", cache.getHitRate()));
        lblCacheSize.setText(String.format("Cached Items: %d", cache.getSize()));

        long lastRefresh = cache.getLastRefreshTime();
        if (lastRefresh > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            lblLastRefresh.setText("Last Refresh: " + sdf.format(new Date(lastRefresh)));
        }
    }

    @FXML
    public void showDashboard() {
        showView(dashboardView);
        loadStatistics();
        updateCacheStatus();
        setActiveButton(btnDashboard);
    }

    @FXML
    public void showProducts() {
        try {
            if (productView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/product-view.fxml"));
                productView = loader.load();
            }
            showView(productView);
            setActiveButton(btnProducts);
            setStatus("Viewing products");
        } catch (IOException e) {
            setStatus("Error loading product view: " + e.getMessage());
        }
    }

    @FXML
    public void showCategories() {
        try {
            if (categoryView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/category-view.fxml"));
                categoryView = loader.load();
            }
            showView(categoryView);
            setActiveButton(btnCategories);
            setStatus("Viewing categories");
        } catch (IOException e) {
            setStatus("Error loading category view: " + e.getMessage());
        }
    }

    @FXML
    public void showOrders() {
        try {
            if (orderView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/order-view.fxml"));
                orderView = loader.load();
            }
            showView(orderView);
            setActiveButton(btnOrders);
            setStatus("Viewing orders");
        } catch (IOException e) {
            setStatus("Error loading order view: " + e.getMessage());
        }
    }

    @FXML
    public void showUsers() {
        try {
            if (userView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/user-view.fxml"));
                userView = loader.load();
            }
            showView(userView);
            setActiveButton(btnUsers);
            setStatus("Viewing users");
        } catch (IOException e) {
            setStatus("Error loading user view: " + e.getMessage());
        }
    }

    @FXML
    public void showPerformance() {
        try {
            if (performanceView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/performance-view.fxml"));
                performanceView = loader.load();
            }
            showView(performanceView);
//            setActiveButton(btnPerformance);
            setStatus("Viewing performance metrics");
        } catch (IOException e) {
            setStatus("Error loading performance view: " + e.getMessage());
        }
    }

    @FXML
    public void showAddProduct() {
        showProducts();
        // The ProductController will handle the add dialog
    }

    @FXML
    public void showAddCategory() {
        showCategories();
        // The CategoryController will handle the add dialog
    }

    @FXML
    public void refreshCache() {
        setStatus("Refreshing caches...");
        initializeCaches();
        updateCacheStatus();
        loadStatistics();
        setStatus("Caches refreshed successfully");
    }

    private void showView(Node view) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void setActiveButton(Button activeBtn) {
        // Remove active class from all buttons
        btnDashboard.getStyleClass().remove("nav-button-active");
        btnProducts.getStyleClass().remove("nav-button-active");
        btnCategories.getStyleClass().remove("nav-button-active");
        btnOrders.getStyleClass().remove("nav-button-active");
        btnUsers.getStyleClass().remove("nav-button-active");
//        btnPerformance.getStyleClass().remove("nav-button-active");

        // Add active class to current button
        activeBtn.getStyleClass().add("nav-button-active");
    }

    private void setStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
    }
}

