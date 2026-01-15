package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.cache.CategoryCache;
import com.amalitech.smartecommerce.cache.InventoryCache;
import com.amalitech.smartecommerce.cache.OrderCache;
import com.amalitech.smartecommerce.cache.ProductCache;
import com.amalitech.smartecommerce.cache.UserCache;
import com.amalitech.smartecommerce.model.Order;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductCategory;
import com.amalitech.smartecommerce.model.User;
import com.amalitech.smartecommerce.service.*;
import com.amalitech.smartecommerce.utils.PerformanceMonitor;
import com.amalitech.smartecommerce.utils.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Admin dashboard controller with async loading for better performance.
 */
public class AdminDashboardController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;

    @FXML private Button btnDashboard;
    @FXML private Button btnProducts;
    @FXML private Button btnCategories;
    @FXML private Button btnInventory;
    @FXML private Button btnOrders;
    @FXML private Button btnUsers;
//    @FXML private Button btnPerformance;

    @FXML private Label lblProductCount;
    @FXML private Label lblCategoryCount;
    @FXML private Label lblOrderCount;
    @FXML private Label lblUserCount;

    @FXML private Label lblCacheHitRate;
    @FXML private Label lblCacheSize;
    @FXML private Label lblLastRefresh;

    @FXML private Label lblStatus;
    @FXML private Label lblConnectionStatus;
    @FXML private Label lblUserName;

    private final ProductService productService = new ProductServiceImpl();
    private final ProductCategoryService categoryService = new ProductCategoryServiceImpl();
    private final OrderService orderService = new OrderServiceImpl();
    private final UserService userService = new UserServiceImpl();

    private final ProductCache productCache = ProductCache.getInstance();
    private final CategoryCache categoryCache = CategoryCache.getInstance();
    private final OrderCache orderCache = OrderCache.getInstance();
    private final UserCache userCache = UserCache.getInstance();

    private Node productView;
    private Node categoryView;
    private Node inventoryView;
    private Node orderView;
    private Node userView;
    private Node performanceView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set user name
        if (SessionManager.getInstance().isLoggedIn()) {
            lblUserName.setText(SessionManager.getInstance().getUserDisplayName());
        }

        // Load data asynchronously
        loadDataAsync();
        setActiveButton(btnDashboard);
    }

    private void loadDataAsync() {
        setStatus("Loading dashboard data...");

        // Show loading state in stat cards
        lblProductCount.setText("...");
        lblCategoryCount.setText("...");
        lblOrderCount.setText("...");
        lblUserCount.setText("...");


        Task<Void> loadTask = new Task<>() {
            private int productCount, categoryCount, orderCount, userCount;
            private List<Product> products;
            private List<ProductCategory> categories;
            private List<Order> orders;
            private List<User> users;

            @Override
            protected Void call() throws Exception {

                // Check caches first - if empty, load from database
                products = productCache.getSize() > 0 ? productCache.getAll() : productService.getAllProducts();
                if (productCache.getSize() == 0) {
                    productCache.loadAll(products);
                }

                categories = categoryCache.getSize() > 0 ? categoryCache.getAll() : categoryService.getAllCategories();
                if (categoryCache.getSize() == 0) {
                    categoryCache.loadAll(categories);
                }

                orders = orderCache.getSize() > 0 ? orderCache.getAll() : orderService.getAllOrders();
                if (orderCache.getSize() == 0) {
                    orderCache.loadAll(orders);
                }

                users = userCache.getSize() > 0 ? userCache.getAll() : userService.getAllUsers();
                if (userCache.getSize() == 0) {
                    userCache.loadAll(users);
                }

                productCount = products.size();
                categoryCount = categories.size();
                orderCount = orders.size();
                userCount = users.size();

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    lblProductCount.setText(String.valueOf(productCount));
                    lblCategoryCount.setText(String.valueOf(categoryCount));
                    lblOrderCount.setText(String.valueOf(orderCount));
                    lblUserCount.setText(String.valueOf(userCount));
//                    updateCacheStatus();
                    setStatus("Dashboard loaded successfully");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setStatus("Error loading data: " + getException().getMessage());
                    lblConnectionStatus.setText("● Disconnected");
                    lblConnectionStatus.setStyle("-fx-text-fill: #e74c3c;");
                });
            }
        };

        new Thread(loadTask).start();
    }

//    private void updateCacheStatus() {
//        ProductCache cache = ProductCache.getInstance();
//        lblCacheHitRate.setText(String.format("%.1f%%", cache.getHitRate()));
//        lblCacheSize.setText(String.valueOf(cache.getSize()));
//
//        long lastRefresh = cache.getLastRefreshTime();
//        if (lastRefresh > 0) {
//            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//            lblLastRefresh.setText(sdf.format(new Date(lastRefresh)));
//        }
//    }

    @FXML
    public void showDashboard() {
        showView(dashboardView);
        loadDataAsync();
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
            System.err.println("ERROR: Failed to load product view");
            e.printStackTrace();
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
            System.err.println("ERROR: Failed to load category view");
            e.printStackTrace();
            setStatus("Error loading category view: " + e.getMessage());
        }
    }

    @FXML
    public void showInventory() {
        System.out.println("DEBUG: showInventory() called");
        try {
            if (inventoryView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/inventory-view.fxml"));
                inventoryView = loader.load();
            }
            showView(inventoryView);
            setActiveButton(btnInventory);
            setStatus("Viewing inventory");
        } catch (IOException e) {
            System.err.println("ERROR: Failed to load inventory view");
            e.printStackTrace();
            setStatus("Error loading inventory view: " + e.getMessage());
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
            System.err.println("ERROR: Failed to load order view");
            e.printStackTrace();
            setStatus("Error loading order view: " + e.getMessage());
        }
    }

    @FXML
    public void showUsers() {
        System.out.println("DEBUG: showUsers() called");
        try {
            if (userView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/user-view.fxml"));
                userView = loader.load();
            }
            showView(userView);
            setActiveButton(btnUsers);
            setStatus("Viewing users");
        } catch (IOException e) {
            System.err.println("ERROR: Failed to load user view");
            e.printStackTrace();
            setStatus("Error loading user view: " + e.getMessage());
        }
    }

//    @FXML
//    public void showPerformance() {
//        System.out.println("DEBUG: showPerformance() called");
//        try {
//            if (performanceView == null) {
//                System.out.println("DEBUG: Loading performance-view.fxml...");
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/performance-view.fxml"));
//                performanceView = loader.load();
//                System.out.println("DEBUG: performance-view.fxml loaded successfully");
//            }
//            showView(performanceView);
////            setActiveButton(btnPerformance);
//            setStatus("Viewing performance metrics");
//        } catch (IOException e) {
//            System.err.println("ERROR: Failed to load performance view");
//            e.printStackTrace();
//            setStatus("Error loading performance view: " + e.getMessage());
//        }
//    }

    @FXML
    public void quickAddProduct() {
        showProducts();
    }

    @FXML
    public void quickAddCategory() {
        showCategories();
    }

//    @FXML
//    public void refreshCache() {
//        setStatus("Refreshing caches...");
//        loadDataAsync();
//    }

    @FXML
    public void handleLogout() {
        // Clear session
        SessionManager.getInstance().logout();

        // Clear all caches to prevent old data from persisting
        productCache.clear();
        categoryCache.clear();
        orderCache.clear();
        userCache.clear();
        InventoryCache.getInstance().clear();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 500, 600);
            scene.getStylesheets().add(getClass().getResource("/com/amalitech/smartecommerce/styles.css").toExternalForm());

            Stage stage = (Stage) lblUserName.getScene().getWindow();
            stage.setTitle("Smart E-Commerce - Login");
            stage.setScene(scene);
            stage.setMinWidth(500);
            stage.setMinHeight(600);
        } catch (IOException e) {
            setStatus("Error logging out: " + e.getMessage());
        }
    }

    private void showView(Node view) {
        if (view == null) {
            System.err.println("ERROR: Attempted to show null view");
            return;
        }
        if (contentArea == null) {
            System.err.println("ERROR: contentArea is null!");
            return;
        }
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void setActiveButton(Button activeBtn) {
        // Remove active class from ALL nav buttons
        btnDashboard.getStyleClass().remove("nav-button-active");
        btnProducts.getStyleClass().remove("nav-button-active");
        btnCategories.getStyleClass().remove("nav-button-active");
        btnInventory.getStyleClass().remove("nav-button-active");
        btnOrders.getStyleClass().remove("nav-button-active");
        btnUsers.getStyleClass().remove("nav-button-active");
//        btnPerformance.getStyleClass().remove("nav-button-active");

        // Add active class to the selected button
        activeBtn.getStyleClass().add("nav-button-active");
    }

    private void setStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
    }
}

