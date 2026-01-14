package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.cache.CategoryCache;
import com.amalitech.smartecommerce.cache.OrderCache;
import com.amalitech.smartecommerce.cache.ProductCache;
import com.amalitech.smartecommerce.cache.UserCache;
import com.amalitech.smartecommerce.dao.OrderLineDao;
import com.amalitech.smartecommerce.dao.OrderLineDaoImpl;
import com.amalitech.smartecommerce.model.Order;
import com.amalitech.smartecommerce.model.OrderLine;
import com.amalitech.smartecommerce.model.OrderStatus;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductCategory;
import com.amalitech.smartecommerce.model.ShippingMethod;
import com.amalitech.smartecommerce.model.User;
import com.amalitech.smartecommerce.service.*;
import com.amalitech.smartecommerce.utils.CartManager;
import com.amalitech.smartecommerce.utils.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Customer dashboard controller for shopping experience.
 */
public class CustomerDashboardController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private VBox homeView;

    @FXML private Button btnHome;
    @FXML private Button btnShop;
    @FXML private Button btnOrders;
    @FXML private Button btnCart;
    @FXML private Button btnProfile;

    @FXML private Label lblUserName;
    @FXML private Label lblWelcome;
    @FXML private Label lblStatus;

    @FXML private FlowPane categoriesPane;
    @FXML private FlowPane featuredProductsPane;

    private final ProductService productService = new ProductServiceImpl();
    private final ProductCategoryService categoryService = new ProductCategoryServiceImpl();
    private final OrderService orderService = new OrderServiceImpl();
    private final UserService userService = new UserServiceImpl();
    private final ShippingMethodService shippingMethodService = new ShippingMethodServiceImpl();
    private final OrderStatusService orderStatusService = new OrderStatusServiceImpl();
    private final OrderLineDao orderLineDao = new OrderLineDaoImpl();
    private final ProductCache productCache = ProductCache.getInstance();
    private final CategoryCache categoryCache = CategoryCache.getInstance();
    private final OrderCache orderCache = OrderCache.getInstance();
    private final UserCache userCache = UserCache.getInstance();
    private final CartManager cartManager = CartManager.getInstance();

    private Node shopView;
    private Node ordersView;
    private Node cartView;
    private Node profileView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (SessionManager.getInstance().isLoggedIn()) {
            String firstName = SessionManager.getInstance().getCurrentUser().getFirstName();
            lblUserName.setText(firstName);
            lblWelcome.setText("Welcome back, " + firstName + "!");
        }

        loadHomeDataAsync();
        setActiveButton(btnHome);
    }

    private void loadHomeDataAsync() {
        setStatus("Loading...");

        // Show loading indicator
        VBox loadingView = createLoadingIndicator("Loading products...");
        categoriesPane.getChildren().clear();
        categoriesPane.getChildren().add(loadingView);
        featuredProductsPane.getChildren().clear();

        Task<Void> loadTask = new Task<>() {
            private List<ProductCategory> categories;
            private List<Product> products;

            @Override
            protected Void call() throws Exception {
                // Check if cache has data, otherwise load from DB
                if (productCache.getSize() == 0) {
                    products = productService.getAllProducts();
                    productCache.loadAll(products);
                } else {
                    products = productCache.getAll();
                }

                if (categoryCache.getSize() == 0) {
                    categories = categoryService.getAllCategories();
                    categoryCache.loadAll(categories);
                } else {
                    categories = categoryCache.getAll();
                }

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    displayCategories(categoryCache.getAll());
                    displayFeaturedProducts(productCache.getAll());
                    setStatus("Ready");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> setStatus("Error loading data"));
            }
        };

        new Thread(loadTask).start();
    }

    /**
     * Creates a loading indicator with spinner and message.
     */
    private VBox createLoadingIndicator(String message) {
        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(40));
        loadingBox.getStyleClass().add("loading-container");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.getStyleClass().add("loading-spinner");
        spinner.setPrefSize(50, 50);

        Label loadingLabel = new Label(message);
        loadingLabel.getStyleClass().add("loading-text");

        Label subLabel = new Label("Please wait...");
        subLabel.getStyleClass().add("loading-subtext");

        loadingBox.getChildren().addAll(spinner, loadingLabel, subLabel);
        return loadingBox;
    }

    private void displayCategories(List<ProductCategory> categories) {
        categoriesPane.getChildren().clear();

        int count = 0;
        for (ProductCategory category : categories) {
            if (count >= 6) break; // Show max 6 categories

            VBox categoryCard = createCategoryCard(category);
            categoriesPane.getChildren().add(categoryCard);
            count++;
        }
    }

    private VBox createCategoryCard(ProductCategory category) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("category-card");
        card.setPrefWidth(150);
        card.setPadding(new Insets(20));

        Label icon = new Label("📦");
        icon.setFont(new Font(32));

        Label name = new Label(category.getCategoryName());
        name.getStyleClass().add("category-name");
        name.setWrapText(true);

        card.getChildren().addAll(icon, name);

        card.setOnMouseClicked(e -> showCategoryProducts(category));

        return card;
    }

    private void displayFeaturedProducts(List<Product> products) {
        featuredProductsPane.getChildren().clear();

        int count = 0;
        for (Product product : products) {
            if (count >= 8) break; // Show max 8 featured products

            VBox productCard = createProductCard(product);
            featuredProductsPane.getChildren().add(productCard);
            count++;
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(200);
        card.setPadding(new Insets(15));

        // Product image placeholder
        VBox imagePlaceholder = new VBox();
        imagePlaceholder.setAlignment(Pos.CENTER);
        imagePlaceholder.setPrefHeight(120);
        imagePlaceholder.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");
        Label imageLabel = new Label("🖼️");
        imageLabel.setFont(new Font(40));
        imagePlaceholder.getChildren().add(imageLabel);

        Label name = new Label(product.getName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);

        Label description = new Label(product.getDescription() != null ?
            (product.getDescription().length() > 50 ?
                product.getDescription().substring(0, 50) + "..." :
                product.getDescription()) : "");
        description.getStyleClass().add("product-description");
        description.setWrapText(true);

        // Get and display price
        double price = getProductPrice(product.getId());
        Label priceLabel = new Label(price > 0 ? String.format("$%.2f", price) : "Price not set");
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Button viewBtn = new Button("View Details");
        viewBtn.getStyleClass().add("view-product-btn");
        viewBtn.setOnAction(e -> showProductDetails(product));

        card.getChildren().addAll(imagePlaceholder, name, description, priceLabel, viewBtn);

        return card;
    }

    private void showProductDetails(Product product) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(product.getName());
        dialog.setHeaderText(null);
        dialog.setResizable(true);

        // Main content - responsive layout
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setMinWidth(320);
        content.setMaxWidth(550);
        content.setStyle("-fx-background-color: white;");

        // Product image placeholder - responsive
        VBox imagePlaceholder = new VBox();
        imagePlaceholder.setAlignment(Pos.CENTER);
        imagePlaceholder.setMinHeight(150);
        imagePlaceholder.setPrefHeight(200);
        imagePlaceholder.setMaxHeight(250);
        imagePlaceholder.getStyleClass().add("product-detail-image");
        Label imageLabel = new Label("🖼️");
        imageLabel.setFont(new Font(70));
        imagePlaceholder.getChildren().add(imageLabel);

        // Product name - wraps on small screens
        Label name = new Label(product.getName());
        name.getStyleClass().add("product-detail-name");
        name.setWrapText(true);
        name.setMaxWidth(Double.MAX_VALUE);

        // Category badge
        ProductCategory category = categoryCache.getById(product.getCategoryId());
        Label categoryLabel = new Label(category != null ? category.getCategoryName() : "Uncategorized");
        categoryLabel.getStyleClass().add("product-detail-category");

        // Price display
        double price = getProductPrice(product.getId());
        Label priceLabel = new Label(price > 0 ? String.format("$%.2f", price) : "Price not available");
        priceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        // Description section
        VBox descBox = new VBox(8);
        descBox.getStyleClass().add("product-detail-description");

        Label descTitle = new Label("Description");
        descTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label description = new Label(product.getDescription() != null && !product.getDescription().isEmpty()
            ? product.getDescription()
            : "No description available for this product.");
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #34495e; -fx-font-size: 14px;");

        descBox.getChildren().addAll(descTitle, description);

        // Quantity selector - responsive FlowPane
        FlowPane qtyPane = new FlowPane(15, 10);
        qtyPane.setAlignment(Pos.CENTER_LEFT);

        Label qtyLabel = new Label("Quantity:");
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Spinner<Integer> quantitySpinner = new Spinner<>(1, 99, 1);
        quantitySpinner.setPrefWidth(90);
        quantitySpinner.setEditable(true);

        // Show subtotal
        Label subtotalLabel = new Label(String.format("Subtotal: $%.2f", price));
        subtotalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        // Update subtotal when quantity changes
        double finalPrice = price;
        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            subtotalLabel.setText(String.format("Subtotal: $%.2f", finalPrice * newVal));
        });

        qtyPane.getChildren().addAll(qtyLabel, quantitySpinner, subtotalLabel);

        // Add to cart button - full width, responsive
        Button addToCartBtn = new Button("🛒 Add to Cart");
        addToCartBtn.getStyleClass().add("product-detail-add-btn");
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setOnAction(e -> {
            int quantity = quantitySpinner.getValue();
            cartManager.addToCart(product, quantity);
            updateCartButton();
            double totalPrice = finalPrice * quantity;
            showAlert(Alert.AlertType.INFORMATION, "Added to Cart",
                quantity + "x " + product.getName() + " ($" + String.format("%.2f", totalPrice) + ") has been added to your cart.\n\nCart total: " + cartManager.getCartSize() + " items");
            dialog.close();
        });

        content.getChildren().addAll(imagePlaceholder, name, categoryLabel, priceLabel, descBox, qtyPane, addToCartBtn);

        scrollPane.setContent(content);
        scrollPane.setMinWidth(350);
        scrollPane.setPrefWidth(500);
        scrollPane.setMaxWidth(600);
        scrollPane.setPrefHeight(550);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        dialog.getDialogPane().setMinWidth(370);

        // Style the close button
        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        closeBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 5;");

        dialog.showAndWait();
    }

    private void updateCartButton() {
        int cartSize = cartManager.getCartSize();
        if (cartSize > 0) {
            btnCart.setText("🛒 Cart (" + cartSize + ")");
        } else {
            btnCart.setText("🛒 Cart");
        }
    }

    private void showCategoryProducts(ProductCategory category) {
        // Filter products by category
        List<Product> categoryProducts = productCache.getByCategory(category.getId());

        VBox categoryView = new VBox(20);
        categoryView.setPadding(new Insets(20));

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> showHome());
        Label title = new Label(category.getCategoryName());
        title.setFont(new Font("System Bold", 24));
        header.getChildren().addAll(backBtn, title);

        FlowPane productsPane = new FlowPane(20, 20);
        productsPane.setAlignment(Pos.TOP_LEFT);

        if (categoryProducts.isEmpty()) {
            Label emptyLabel = new Label("No products in this category yet.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            productsPane.getChildren().add(emptyLabel);
        } else {
            for (Product product : categoryProducts) {
                productsPane.getChildren().add(createProductCard(product));
            }
        }

        ScrollPane scrollPane = new ScrollPane(productsPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        categoryView.getChildren().addAll(header, scrollPane);

        showView(categoryView);
    }

    @FXML
    public void showHome() {
        showView(homeView);
        loadHomeDataAsync();
        setActiveButton(btnHome);
    }

    @FXML
    public void showShop() {
        VBox shopContent = new VBox(20);
        shopContent.setPadding(new Insets(20));

        // Search bar
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("search-field");
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("search-btn");

        ComboBox<ProductCategory> categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("All Categories");
        categoryFilter.getItems().addAll(categoryCache.getAll());
        categoryFilter.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCategoryName());
            }
        });
        categoryFilter.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "All Categories" : item.getCategoryName());
            }
        });

        searchBar.getChildren().addAll(searchField, searchBtn, categoryFilter);

        // Products grid
        FlowPane productsPane = new FlowPane(20, 20);
        productsPane.setAlignment(Pos.TOP_LEFT);

        List<Product> products = productCache.getAll();
        for (Product product : products) {
            productsPane.getChildren().add(createProductCard(product));
        }

        // Search action
        searchBtn.setOnAction(e -> {
            String query = searchField.getText();
            ProductCategory selectedCat = categoryFilter.getValue();

            List<Product> filtered;
            if (selectedCat != null) {
                filtered = productCache.getByCategory(selectedCat.getId());
            } else if (!query.isEmpty()) {
                filtered = productCache.searchByName(query);
            } else {
                filtered = productCache.getAll();
            }

            productsPane.getChildren().clear();
            for (Product p : filtered) {
                if (query.isEmpty() || (p.getName() != null && p.getName().toLowerCase().contains(query.toLowerCase()))) {
                    productsPane.getChildren().add(createProductCard(p));
                }
            }
        });

        categoryFilter.setOnAction(e -> searchBtn.fire());

        ScrollPane scrollPane = new ScrollPane(productsPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Label title = new Label("All Products");
        title.setFont(new Font("System Bold", 24));

        shopContent.getChildren().addAll(title, searchBar, scrollPane);

        showView(shopContent);
        setActiveButton(btnShop);
    }

    @FXML
    public void showMyOrders() {
        VBox ordersContent = new VBox(20);
        ordersContent.setPadding(new Insets(20));

        Label title = new Label("My Orders");
        title.setFont(new Font("System Bold", 24));
        title.setStyle("-fx-text-fill: #2c3e50;");

        // Show loading state
        ProgressIndicator loader = new ProgressIndicator();
        VBox loadingBox = new VBox();
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.getChildren().add(loader);
        VBox.setVgrow(loadingBox, Priority.ALWAYS);

        ordersContent.getChildren().addAll(title, loadingBox);
        showView(ordersContent);

        // Load orders asynchronously
        Task<List<Order>> loadTask = new Task<>() {
            @Override
            protected List<Order> call() throws Exception {
                UUID currentUserId = SessionManager.getInstance().getCurrentUser().getId();

                // Try to get from cache first
                List<Order> myOrders = orderCache.getByUserId(currentUserId);

                // If cache is empty, load from database and populate cache
                if (myOrders.isEmpty()) {
                    List<Order> allOrders = orderService.getAllOrders();
                    orderCache.loadAll(allOrders);
                    myOrders = orderCache.getByUserId(currentUserId);
                }

                return myOrders;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<Order> myOrders = getValue();

                    // Clear loading state
                    ordersContent.getChildren().clear();
                    ordersContent.getChildren().add(title);

                    if (myOrders.isEmpty()) {
                        // Empty state
                        VBox emptyState = new VBox(20);
                        emptyState.setAlignment(Pos.CENTER);

                        Label emptyIcon = new Label("📦");
                        emptyIcon.setFont(new Font(60));

                        Label placeholder = new Label("No orders yet");
                        placeholder.setStyle("-fx-font-size: 20px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

                        Label subtext = new Label("Start shopping to see your orders here!");
                        subtext.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

                        Button shopBtn = new Button("Start Shopping");
                        shopBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; " +
                            "-fx-padding: 15 35; -fx-background-radius: 25; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
                        shopBtn.setOnAction(e -> showShop());

                        emptyState.getChildren().addAll(emptyIcon, placeholder, subtext, shopBtn);
                        VBox.setVgrow(emptyState, Priority.ALWAYS);

                        ordersContent.getChildren().add(emptyState);
                    } else {
                        // Display orders
                        VBox ordersListContainer = new VBox(15);

                        for (Order order : myOrders) {
                            VBox orderCard = createOrderCard(order);
                            ordersListContainer.getChildren().add(orderCard);
                        }

                        ScrollPane scrollPane = new ScrollPane(ordersListContainer);
                        scrollPane.setFitToWidth(true);
                        scrollPane.setStyle("-fx-background-color: transparent;");
                        VBox.setVgrow(scrollPane, Priority.ALWAYS);

                        Label summaryLabel = new Label("Total Orders: " + myOrders.size());
                        summaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

                        ordersContent.getChildren().addAll(summaryLabel, scrollPane);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    ordersContent.getChildren().clear();
                    Label errorLabel = new Label("Failed to load orders. Please try again.");
                    errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                    ordersContent.getChildren().addAll(title, errorLabel);
                });
            }
        };

        new Thread(loadTask).start();
        setActiveButton(btnOrders);
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        // Header row
        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label orderIcon = new Label("🛍️");
        orderIcon.setFont(new Font(24));

        VBox orderInfo = new VBox(3);
        Label orderIdLabel = new Label("Order #" + order.getId().toString().substring(0, 8).toUpperCase());
        orderIdLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label dateLabel = new Label("Placed on: " + (order.getOrderDate() != null ? order.getOrderDate().toString() : "N/A"));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        orderInfo.getChildren().addAll(orderIdLabel, dateLabel);
        HBox.setHgrow(orderInfo, Priority.ALWAYS);

        // Status badge
        String status = determineCustomerOrderStatus(order);
        String statusColor = getStatusColor(status);

        Label statusBadge = new Label(status);
        statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-text-fill: white; " +
            "-fx-padding: 5 15; -fx-background-radius: 15; -fx-font-size: 12px; -fx-font-weight: bold;");

        headerRow.getChildren().addAll(orderIcon, orderInfo, statusBadge);

        // Divider
        Separator separator = new Separator();

        // Order details
        HBox detailsRow = new HBox(30);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        VBox totalBox = new VBox(3);
        Label totalTitle = new Label("Order Total");
        totalTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        Label totalValue = new Label(String.format("$%.2f", order.getOrderTotal() != null ? order.getOrderTotal() : 0.0));
        totalValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        totalBox.getChildren().addAll(totalTitle, totalValue);

        // Payment notice
        VBox paymentBox = new VBox(3);
        Label paymentTitle = new Label("Payment");
        paymentTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        Label paymentValue = new Label("⚠️ Pending - Payment not yet available");
        paymentValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c;");
        paymentBox.getChildren().addAll(paymentTitle, paymentValue);

        detailsRow.getChildren().addAll(totalBox, paymentBox);

        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

        Button viewDetailsBtn = new Button("📋 View Details");
        viewDetailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 15; " +
            "-fx-background-radius: 5; -fx-cursor: hand;");
        viewDetailsBtn.setOnAction(e -> showCustomerOrderDetails(order));

        // Only show cancel button if order is not completed or already cancelled
        if (!status.equals("Completed") && !status.equals("Cancelled")) {
            Button cancelBtn = new Button("❌ Cancel Order");
            cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 15; " +
                "-fx-background-radius: 5; -fx-cursor: hand;");
            cancelBtn.setOnAction(e -> cancelOrder(order));
            actionButtons.getChildren().add(cancelBtn);
        }

        actionButtons.getChildren().add(viewDetailsBtn);

        card.getChildren().addAll(headerRow, separator, detailsRow, actionButtons);

        return card;
    }

    private String determineCustomerOrderStatus(Order order) {
        if (order.getOrderStatus() != null) {
            // Try to get status name from order status service
            OrderStatus orderStatus = orderStatusService.getOrderStatusById(order.getOrderStatus());
            if (orderStatus != null) {
                return orderStatus.getStatus();
            }
        }

        if (order.getShippingMethodId() != null) {
            return "Processing";
        }

        return "Pending";
    }

    private String getStatusColor(String status) {
        return switch (status.toLowerCase()) {
            case "completed" -> "#27ae60";
            case "processing" -> "#3498db";
            case "cancelled" -> "#e74c3c";
            case "shipped" -> "#9b59b6";
            default -> "#f39c12";
        };
    }

    private void showCustomerOrderDetails(Order order) {
        // Get order items with product names using JOIN query
        List<OrderItemDetail> orderItems = getOrderItemsForCustomer(order.getId());

        // Get shipping cost
        double shippingCost = getShippingCost(order.getShippingMethodId());

        StringBuilder details = new StringBuilder();
        details.append("Order ID: #").append(order.getId().toString().substring(0, 8).toUpperCase()).append("\n\n");
        details.append("📅 Order Date: ").append(order.getOrderDate()).append("\n");
        details.append("📋 Status: ").append(determineCustomerOrderStatus(order)).append("\n");

        // Get shipping method name and cost
        if (order.getShippingMethodName() != null && !order.getShippingMethodName().isEmpty()) {
            details.append("🚚 Shipping: ").append(order.getShippingMethodName());
            if (shippingCost > 0) {
                details.append(String.format(" ($%.2f)", shippingCost));
            } else {
                details.append(" (FREE)");
            }
            details.append("\n");
        }
        details.append("\n");

        // Display order items
        details.append("📦 ORDER ITEMS:\n");
        details.append("─".repeat(35)).append("\n");

        double itemsTotal = 0;
        if (orderItems.isEmpty()) {
            details.append("No items found for this order.\n");
        } else {
            for (OrderItemDetail item : orderItems) {
                double lineTotal = item.price * item.quantity;
                itemsTotal += lineTotal;
                details.append(String.format("• %s\n", item.productName));
                details.append(String.format("  Qty: %d × $%.2f = $%.2f\n", item.quantity, item.price, lineTotal));
            }
        }
        details.append("─".repeat(35)).append("\n");

        // Show breakdown
        details.append(String.format("Items Subtotal: $%.2f\n", itemsTotal));
        details.append(String.format("Shipping Cost:  $%.2f\n", shippingCost));
        details.append("─".repeat(35)).append("\n");
        details.append(String.format("💰 ORDER TOTAL: $%.2f", order.getOrderTotal() != null ? order.getOrderTotal() : 0.0));

        // Create dialog
        TextArea textArea = new TextArea(details.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(400);
        textArea.setPrefHeight(380);
        textArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Order #" + order.getId().toString().substring(0, 8).toUpperCase());
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefWidth(450);
        alert.showAndWait();
    }

    private static class OrderItemDetail {
        String productName;
        int quantity;
        double price;

        OrderItemDetail(String productName, int quantity, double price) {
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }
    }

    private List<OrderItemDetail> getOrderItemsForCustomer(UUID orderId) {
        List<OrderItemDetail> items = new ArrayList<>();

        String sql = """
            SELECT p.name AS product_name, ol.qty, ol.price
            FROM order_line ol
            JOIN product_item pi ON ol.product_item_id = pi.id
            JOIN product p ON pi.product_id = p.id
            WHERE ol.order_id = ?
            ORDER BY p.name
            """;

        try {
            java.sql.Connection conn = com.amalitech.smartecommerce.utils.DBConnection.getConnection();
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, orderId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String productName = rs.getString("product_name");
                        int qty = rs.getInt("qty");
                        double price = rs.getDouble("price");
                        items.add(new OrderItemDetail(
                            productName != null ? productName : "Unknown Product",
                            qty,
                            price
                        ));
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error fetching order items: " + e.getMessage());
        }

        return items;
    }

    private void cancelOrder(Order order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Order");
        confirm.setHeaderText("Are you sure you want to cancel this order?");
        confirm.setContentText("Order #" + order.getId().toString().substring(0, 8).toUpperCase() +
            "\nTotal: $" + String.format("%.2f", order.getOrderTotal()) +
            "\n\nThis action cannot be undone. The order will be permanently deleted.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete the order from database
            Order deleted = orderService.deleteOrder(order.getId());

            if (deleted != null) {
                // Remove from cache
                orderCache.remove(order.getId());
                showAlert(Alert.AlertType.INFORMATION, "Order Cancelled",
                    "Your order has been cancelled and removed successfully.");
                // Refresh orders view
                showMyOrders();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel order. Please try again.");
            }
        }
    }

    private UUID getCancelledStatusId() {
        try {
            java.sql.Connection conn = com.amalitech.smartecommerce.utils.DBConnection.getConnection();

            // First, try to find existing "Cancelled" status
            String selectSql = "SELECT id FROM order_status WHERE LOWER(status) = 'cancelled' LIMIT 1";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(selectSql);
                 java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (UUID) rs.getObject("id");
                }
            }

            // If not found, create it
            UUID newId = UUID.randomUUID();
            String insertSql = "INSERT INTO order_status (id, status) VALUES (?, 'Cancelled')";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setObject(1, newId);
                if (stmt.executeUpdate() > 0) {
                    System.out.println("Created 'Cancelled' order status with ID: " + newId);
                    return newId;
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error getting/creating cancelled status: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets the shipping cost for a shipping method.
     */
    private double getShippingCost(UUID shippingMethodId) {
        if (shippingMethodId == null) {
            return 0.0;
        }
        try {
            java.sql.Connection conn = com.amalitech.smartecommerce.utils.DBConnection.getConnection();
            String sql = "SELECT price FROM shipping_method WHERE id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, shippingMethodId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("price");
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error getting shipping cost: " + e.getMessage());
        }
        return 0.0;
    }

    @FXML
    public void showCart() {
        VBox cartContent = new VBox(20);
        cartContent.setPadding(new Insets(20));

        Label title = new Label("Shopping Cart");
        title.setFont(new Font("System Bold", 24));
        title.setStyle("-fx-text-fill: #2c3e50;");

        if (cartManager.isEmpty()) {
            // Empty cart view
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(Pos.CENTER);

            Label emptyIcon = new Label("🛒");
            emptyIcon.setFont(new Font(60));

            Label placeholder = new Label("Your cart is empty");
            placeholder.setStyle("-fx-font-size: 20px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

            Label subtext = new Label("Add some products to get started!");
            subtext.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

            Button shopBtn = new Button("Browse Products");
            shopBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; " +
                "-fx-padding: 15 35; -fx-background-radius: 25; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
            shopBtn.setOnAction(e -> showShop());

            emptyState.getChildren().addAll(emptyIcon, placeholder, subtext, shopBtn);
            VBox.setVgrow(emptyState, Priority.ALWAYS);

            cartContent.getChildren().addAll(title, emptyState);
        } else {
            // Cart with items
            VBox itemsContainer = new VBox(15);

            for (CartManager.CartItem item : cartManager.getCartItems()) {
                HBox itemRow = createCartItemRow(item);
                itemsContainer.getChildren().add(itemRow);
            }

            ScrollPane scrollPane = new ScrollPane(itemsContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            VBox.setVgrow(scrollPane, Priority.ALWAYS);

            // Cart summary
            HBox summaryBox = new HBox(20);
            summaryBox.setAlignment(Pos.CENTER_RIGHT);
            summaryBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 12;");

            // Calculate actual cart total
            double cartTotal = cartManager.getCartTotal();

            VBox totalInfo = new VBox(5);
            Label itemsLabel = new Label("Items: " + cartManager.getCartSize());
            itemsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            Label totalLabel = new Label(String.format("Total: $%.2f", cartTotal));
            totalLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
            totalInfo.getChildren().addAll(itemsLabel, totalLabel);

            Button clearBtn = new Button("Clear Cart");
            clearBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5;");
            clearBtn.setOnAction(e -> {
                cartManager.clearCart();
                updateCartButton();
                showCart(); // Refresh view
            });

            Button checkoutBtn = new Button("Proceed to Checkout");
            checkoutBtn.setStyle("-fx-background-color: linear-gradient(to right, #27ae60, #2ecc71); -fx-text-fill: white; " +
                "-fx-padding: 12 30; -fx-background-radius: 8; -fx-font-size: 14px; -fx-font-weight: bold;");
            checkoutBtn.setOnAction(e -> processCheckout());

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            summaryBox.getChildren().addAll(totalInfo, spacer, clearBtn, checkoutBtn);

            cartContent.getChildren().addAll(title, scrollPane, summaryBox);
        }

        showView(cartContent);
        setActiveButton(btnCart);
    }

    private HBox createCartItemRow(CartManager.CartItem item) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // Product image placeholder
        Label imageLabel = new Label("🖼️");
        imageLabel.setFont(new Font(30));
        imageLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 8;");

        // Get product price
        double price = getProductPrice(item.getProductId());
        double itemTotal = price * item.getQuantity();

        // Product info
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(item.getProductName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label priceLabel = new Label(String.format("$%.2f each", price));
        priceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        infoBox.getChildren().addAll(nameLabel, priceLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Quantity controls
        Button minusBtn = new Button("-");
        minusBtn.setStyle("-fx-background-color: #e9ecef; -fx-padding: 5 12; -fx-background-radius: 5; -fx-cursor: hand;");
        minusBtn.setOnAction(e -> {
            cartManager.updateQuantity(item.getProductId(), item.getQuantity() - 1);
            updateCartButton();
            showCart();
        });

        Label quantityLabel = new Label(String.valueOf(item.getQuantity()));
        quantityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0 10;");

        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-background-color: #e9ecef; -fx-padding: 5 12; -fx-background-radius: 5; -fx-cursor: hand;");
        plusBtn.setOnAction(e -> {
            cartManager.updateQuantity(item.getProductId(), item.getQuantity() + 1);
            updateCartButton();
            showCart();
        });

        HBox qtyControls = new HBox(5, minusBtn, quantityLabel, plusBtn);
        qtyControls.setAlignment(Pos.CENTER);

        // Item total price
        Label totalPriceLabel = new Label(String.format("$%.2f", itemTotal));
        totalPriceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        totalPriceLabel.setMinWidth(80);

        // Remove button
        Button removeBtn = new Button("🗑️");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            cartManager.removeFromCart(item.getProductId());
            updateCartButton();
            showCart();
        });

        row.getChildren().addAll(imageLabel, infoBox, qtyControls, totalPriceLabel, removeBtn);

        return row;
    }

    @FXML
    public void showProfile() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox profileContent = new VBox(25);
        profileContent.setPadding(new Insets(25));
        profileContent.setAlignment(Pos.TOP_CENTER);
        profileContent.setFillWidth(true);

        // Show loading state
        Label loadingLabel = new Label("Loading profile...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        profileContent.getChildren().add(loadingLabel);

        scrollPane.setContent(profileContent);
        showView(scrollPane);

        // Load profile asynchronously using cache
        Task<VBox> loadProfileTask = new Task<>() {
            @Override
            protected VBox call() throws Exception {
                var user = SessionManager.getInstance().getCurrentUser();

                // Profile Card Container
                VBox profileCard = new VBox(25);
                profileCard.getStyleClass().add("profile-container");
                profileCard.setMaxWidth(600);
                profileCard.setAlignment(Pos.TOP_CENTER);

                // Profile Header with Avatar
                VBox headerSection = new VBox(15);
                headerSection.getStyleClass().add("profile-header");
                headerSection.setAlignment(Pos.CENTER);

                // Avatar circle with initials
                StackPane avatarCircle = new StackPane();
                avatarCircle.getStyleClass().add("profile-avatar");
                String initials = (user.getFirstName() != null ? user.getFirstName().substring(0, 1) : "") +
                                 (user.getLastName() != null ? user.getLastName().substring(0, 1) : "");
                Label avatarLabel = new Label(initials.toUpperCase());
                avatarLabel.getStyleClass().add("profile-avatar-text");
                avatarCircle.getChildren().add(avatarLabel);

                Label nameLabel = new Label(user.getFirstName() + " " + user.getLastName());
                nameLabel.getStyleClass().add("profile-name");

                Label emailLabel = new Label(user.getEmailAddress());
                emailLabel.getStyleClass().add("profile-email");

                headerSection.getChildren().addAll(avatarCircle, nameLabel, emailLabel);

                // Stats Section - Using cache for fast order count
                FlowPane statsPane = new FlowPane(15, 15);
                statsPane.setAlignment(Pos.CENTER);

                // Use OrderCache to get user's orders efficiently
                int orderCount = orderCache.getOrderCountForUser(user.getId());

                // If cache is empty, load orders from database
                if (orderCache.getSize() == 0) {
                    List<Order> allOrders = orderService.getAllOrders();
                    orderCache.loadAll(allOrders);
                    orderCount = orderCache.getOrderCountForUser(user.getId());
                }

                VBox ordersStatCard = createStatCard(String.valueOf(orderCount), "Orders");
                VBox cartStatCard = createStatCard(String.valueOf(cartManager.getCartSize()), "Cart Items");

                statsPane.getChildren().addAll(ordersStatCard, cartStatCard);

                // Personal Information Section
                VBox personalSection = new VBox(15);
                personalSection.getStyleClass().add("profile-section");

                Label personalTitle = new Label("📝 Personal Information");
                personalTitle.getStyleClass().add("profile-section-title");

                GridPane form = new GridPane();
                form.setHgap(15);
                form.setVgap(15);
                form.setMaxWidth(Double.MAX_VALUE);

                // First Name
                VBox firstNameBox = createFormField("First Name", user.getFirstName());
                TextField firstNameField = (TextField) firstNameBox.getChildren().get(1);

                // Last Name
                VBox lastNameBox = createFormField("Last Name", user.getLastName());
                TextField lastNameField = (TextField) lastNameBox.getChildren().get(1);

                // Email
                VBox emailBox = createFormField("Email Address", user.getEmailAddress());
                TextField emailField = (TextField) emailBox.getChildren().get(1);

                // Phone
                VBox phoneBox = createFormField("Phone Number", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
                TextField phoneField = (TextField) phoneBox.getChildren().get(1);

                // Use FlowPane for responsive form layout
                FlowPane formPane = new FlowPane(20, 15);
                formPane.setPrefWrapLength(500);

                firstNameBox.setMinWidth(200);
                firstNameBox.setPrefWidth(220);
                lastNameBox.setMinWidth(200);
                lastNameBox.setPrefWidth(220);
                emailBox.setMinWidth(200);
                emailBox.setPrefWidth(220);
                phoneBox.setMinWidth(200);
                phoneBox.setPrefWidth(220);

                formPane.getChildren().addAll(firstNameBox, lastNameBox, emailBox, phoneBox);

                personalSection.getChildren().addAll(personalTitle, formPane);

                // Save Button
                Button saveBtn = new Button("💾 Save Changes");
                saveBtn.getStyleClass().add("profile-save-btn");
                saveBtn.setOnAction(e -> {
                    // Show loading
                    saveBtn.setText("Saving...");
                    saveBtn.setDisable(true);

                    // Simulate save with delay
                    Task<Void> saveTask = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            // Update user object
                            user.setFirstName(firstNameField.getText());
                            user.setLastName(lastNameField.getText());
                            user.setEmailAddress(emailField.getText());
                            user.setPhoneNumber(phoneField.getText());

                            // Save to database using UserService
                            User updatedUser = userService.updateUser(user);

                            if (updatedUser != null) {
                                // Update cache
                                userCache.update(updatedUser);
                                // Update session manager with new user info
                                SessionManager.getInstance().setCurrentUser(updatedUser);
                                return null;
                            } else {
                                throw new Exception("Failed to update user in database");
                            }
                        }

                        @Override
                        protected void succeeded() {
                            Platform.runLater(() -> {
                                saveBtn.setText("💾 Save Changes");
                                saveBtn.setDisable(false);
                                // Update header
                                lblUserName.setText(user.getFirstName());
                                showAlert(Alert.AlertType.INFORMATION, "Profile Updated",
                                    "Your profile has been updated successfully.");
                            });
                        }

                        @Override
                        protected void failed() {
                            Platform.runLater(() -> {
                                saveBtn.setText("💾 Save Changes");
                                saveBtn.setDisable(false);
                                showAlert(Alert.AlertType.ERROR, "Error",
                                    "Failed to update profile. Please try again.");
                            });
                        }
                    };
                    new Thread(saveTask).start();
                });

                // Account Actions Section
                VBox actionsSection = new VBox(15);
                actionsSection.getStyleClass().add("profile-section");

                Label actionsTitle = new Label("⚙️ Account Actions");
                actionsTitle.getStyleClass().add("profile-section-title");

                FlowPane actionsPane = new FlowPane(10, 10);

                Button viewOrdersBtn = new Button("📦 View Orders");
                viewOrdersBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 20; " +
                    "-fx-background-radius: 5; -fx-cursor: hand;");
                viewOrdersBtn.setOnAction(e -> showMyOrders());

                Button changePasswordBtn = new Button("🔐 Change Password");
                changePasswordBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 10 20; " +
                    "-fx-background-radius: 5; -fx-cursor: hand;");
                changePasswordBtn.setOnAction(e -> showAlert(Alert.AlertType.INFORMATION, "Coming Soon",
                    "Password change feature will be available soon."));

                Button logoutBtn = new Button("🚪 Logout");
                logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10 20; " +
                    "-fx-background-radius: 5; -fx-cursor: hand;");
                logoutBtn.setOnAction(e -> handleLogout());

                actionsPane.getChildren().addAll(viewOrdersBtn, changePasswordBtn, logoutBtn);
                actionsSection.getChildren().addAll(actionsTitle, actionsPane);

                profileCard.getChildren().addAll(headerSection, statsPane, personalSection, saveBtn, actionsSection);

                VBox finalProfileContent = new VBox(25);
                finalProfileContent.setPadding(new Insets(25));
                finalProfileContent.setAlignment(Pos.TOP_CENTER);
                finalProfileContent.setFillWidth(true);
                finalProfileContent.getChildren().add(profileCard);

                return finalProfileContent;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    profileContent.getChildren().clear();
                    profileContent.getChildren().add(getValue());
                    VBox.setVgrow(scrollPane, Priority.ALWAYS);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    profileContent.getChildren().clear();
                    Label errorLabel = new Label("Failed to load profile. Please try again.");
                    errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                    profileContent.getChildren().add(errorLabel);
                });
            }
        };

        new Thread(loadProfileTask).start();
        setActiveButton(btnProfile);
    }

    private VBox createStatCard(String value, String label) {
        VBox card = new VBox(5);
        card.getStyleClass().add("profile-stats-card");
        card.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("profile-stats-number");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("profile-stats-label");

        card.getChildren().addAll(valueLabel, textLabel);
        return card;
    }

    private VBox createFormField(String labelText, String value) {
        VBox fieldBox = new VBox(5);

        Label label = new Label(labelText);
        label.getStyleClass().add("profile-field-label");

        TextField field = new TextField(value);
        field.getStyleClass().add("profile-field-input");
        field.setMaxWidth(Double.MAX_VALUE);

        fieldBox.getChildren().addAll(label, field);
        return fieldBox;
    }

    private void processCheckout() {
        // Create checkout dialog with shipping options
        Dialog<ShippingMethod> checkoutDialog = new Dialog<>();
        checkoutDialog.setTitle("Checkout");
        checkoutDialog.setHeaderText("Complete Your Order");

        // Create content
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);

        // Order Summary Section
        Label summaryTitle = new Label("📋 Order Summary");
        summaryTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox itemsBox = new VBox(8);
        itemsBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

        for (CartManager.CartItem item : cartManager.getCartItems()) {
            HBox itemRow = new HBox(10);
            itemRow.setAlignment(Pos.CENTER_LEFT);
            Label itemLabel = new Label("• " + item.getProductName() + " x " + item.getQuantity());
            itemLabel.setStyle("-fx-font-size: 13px;");
            itemsBox.getChildren().add(itemLabel);
        }

        Label itemCountLabel = new Label("Total Items: " + cartManager.getCartSize());
        itemCountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        itemsBox.getChildren().add(itemCountLabel);

        // Shipping Section
        Label shippingTitle = new Label("🚚 Select Shipping Method");
        shippingTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10 0 0 0;");

        ToggleGroup shippingGroup = new ToggleGroup();
        VBox shippingOptions = new VBox(10);
        shippingOptions.setStyle("-fx-padding: 10 0;");

        // Load shipping methods from database
        List<ShippingMethod> shippingMethods = shippingMethodService.getAllShippingMethods();

        // If no shipping methods in DB, show error
        if (shippingMethods.isEmpty()) {
            Label noMethodsLabel = new Label("⚠️ No shipping methods available. Please contact support.");
            noMethodsLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
            shippingOptions.getChildren().add(noMethodsLabel);
        }

        for (ShippingMethod method : shippingMethods) {
            RadioButton rb = new RadioButton();
            rb.setToggleGroup(shippingGroup);
            rb.setUserData(method); // Store the actual ShippingMethod object

            HBox optionBox = new HBox(15);
            optionBox.setAlignment(Pos.CENTER_LEFT);
            optionBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-cursor: hand;");

            VBox optionInfo = new VBox(3);
            Label optionName = new Label(method.getName());
            optionName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            Label optionTime = new Label(getDeliveryTimeForMethod(method.getName()));
            optionTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
            optionInfo.getChildren().addAll(optionName, optionTime);
            HBox.setHgrow(optionInfo, Priority.ALWAYS);

            Label optionPrice = new Label(method.getPrice() == 0 ? "FREE" : String.format("$%.2f", method.getPrice()));
            optionPrice.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                (method.getPrice() == 0 ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #2c3e50;"));

            optionBox.getChildren().addAll(rb, optionInfo, optionPrice);

            // Make the whole box clickable
            optionBox.setOnMouseClicked(e -> rb.setSelected(true));

            // Highlight when selected
            rb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    optionBox.setStyle("-fx-background-color: #e8f4fd; -fx-padding: 15; -fx-background-radius: 8; " +
                        "-fx-border-color: #3498db; -fx-border-radius: 8; -fx-border-width: 2;");
                } else {
                    optionBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                        "-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
                }
            });

            shippingOptions.getChildren().add(optionBox);
        }

        // Select first option by default if available
        if (!shippingMethods.isEmpty()) {
            RadioButton firstRb = (RadioButton) shippingOptions.getChildren().get(0).lookup(".radio-button");
            if (firstRb != null) {
                firstRb.setSelected(true);
            }
        }

        // Total Section
        Label totalTitle = new Label("💰 Order Total");
        totalTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10 0 0 0;");

        HBox totalBox = new HBox(15);
        totalBox.setAlignment(Pos.CENTER_LEFT);
        totalBox.setStyle("-fx-background-color: #27ae60; -fx-padding: 15; -fx-background-radius: 8;");

        Label totalLabel = new Label("Total (including shipping):");
        totalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        HBox.setHgrow(totalLabel, Priority.ALWAYS);

        Label totalAmount = new Label("$5.99"); // Will be updated
        totalAmount.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        totalBox.getChildren().addAll(totalLabel, totalAmount);

        // Update total when shipping changes
        shippingGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ShippingMethod selected = (ShippingMethod) newVal.getUserData();
                double itemTotal = cartManager.getCartSize(); // Placeholder
                double total = itemTotal + selected.getPrice();
                totalAmount.setText(String.format("$%.2f", total));
            }
        });

        content.getChildren().addAll(summaryTitle, itemsBox, shippingTitle, shippingOptions, totalTitle, totalBox);

        // Wrap in scroll pane for smaller screens
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPrefHeight(500);

        checkoutDialog.getDialogPane().setContent(scrollPane);
        checkoutDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Customize OK button
        Button okButton = (Button) checkoutDialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Place Order");
        okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 25;");

        // Result converter
        checkoutDialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                Toggle selected = shippingGroup.getSelectedToggle();
                if (selected != null) {
                    return (ShippingMethod) selected.getUserData();
                }
            }
            return null;
        });

        checkoutDialog.showAndWait().ifPresent(selectedShipping -> {
            createOrderWithShippingMethod(selectedShipping);
        });
    }

    /**
     * Helper method to get delivery time based on shipping method name.
     */
    private String getDeliveryTimeForMethod(String methodName) {
        String nameLower = methodName.toLowerCase();
        if (nameLower.contains("overnight") || nameLower.contains("next day")) {
            return "Next Business Day";
        } else if (nameLower.contains("express") || nameLower.contains("fast")) {
            return "2-3 Business Days";
        } else if (nameLower.contains("pickup") || nameLower.contains("store")) {
            return "Ready in 2 Hours";
        } else {
            return "5-7 Business Days";
        }
    }

    /**
     * Creates an order using the shipping method from the database.
     */
    private void createOrderWithShippingMethod(ShippingMethod shipping) {
        try {
            // Get cart items
            var cartItems = cartManager.getCartItems();

            // Calculate item total from actual product prices
            double itemTotal = 0;
            for (var cartItem : cartItems) {
                double productPrice = getProductPrice(cartItem.getProduct().getId());
                itemTotal += productPrice * cartItem.getQuantity();
            }

            double shippingCost = shipping.getPrice();
            double orderTotal = itemTotal + shippingCost;

            // Create a new order
            Order order = new Order();
            order.setId(UUID.randomUUID()); // Generate order ID first so we can use it for order lines
            order.setUserId(SessionManager.getInstance().getCurrentUser().getId());
            order.setOrderDate(LocalDate.now());
            order.setOrderTotal(orderTotal);

            // Set shipping method ID from the actual database record
            order.setShippingMethodId(shipping.getId());
            order.setShippingMethodName(shipping.getName()); // Store the display name

            // Save the order
            Order createdOrder = orderService.createOrder(order);

            if (createdOrder != null) {
                // Create order lines for each cart item
                for (var cartItem : cartItems) {
                    createOrderLine(order.getId(), cartItem.getProduct(), cartItem.getQuantity());
                }

                // Clear the cart
                cartManager.clearCart();
                updateCartButton();

                // Show success message with shipping and payment info
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Order Placed Successfully!");
                successAlert.setHeaderText("🎉 Thank You For Your Order!");

                VBox content = new VBox(15);
                content.setPadding(new Insets(15));

                Label orderIdLabel = new Label("✅ Your order has been recorded!");
                orderIdLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

                Label dateLabel = new Label("📅 Order Date: " + LocalDate.now().toString());
                dateLabel.setStyle("-fx-font-size: 13px;");

                // Shipping info section
                VBox shippingBox = new VBox(5);
                shippingBox.setStyle("-fx-background-color: #e8f4fd; -fx-padding: 12; -fx-background-radius: 8;");

                Label shippingHeader = new Label("🚚 Shipping Details");
                shippingHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                Label shippingMethodLabel = new Label("Method: " + shipping.getName());
                shippingMethodLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");

                Label deliveryTime = new Label("Estimated Delivery: " + getDeliveryTimeForMethod(shipping.getName()));
                deliveryTime.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

                Label shippingPrice = new Label("Shipping Cost: " + (shipping.getPrice() == 0 ? "FREE" : String.format("$%.2f", shipping.getPrice())));
                shippingPrice.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (shipping.getPrice() == 0 ? "#27ae60" : "#2c3e50") + ";");

                shippingBox.getChildren().addAll(shippingHeader, shippingMethodLabel, deliveryTime, shippingPrice);

                // Total section
                HBox totalBox = new HBox(10);
                totalBox.setAlignment(Pos.CENTER_LEFT);
                totalBox.setStyle("-fx-background-color: #27ae60; -fx-padding: 12; -fx-background-radius: 8;");

                Label totalTitle = new Label("Order Total:");
                totalTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
                HBox.setHgrow(totalTitle, Priority.ALWAYS);

                Label totalValue = new Label(String.format("$%.2f", orderTotal));
                totalValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

                totalBox.getChildren().addAll(totalTitle, totalValue);

                Label statusLabel = new Label("📋 Status: Pending Payment");
                statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");

                Label divider = new Label("─".repeat(40));
                divider.setStyle("-fx-text-fill: #ddd;");

                Label paymentNotice = new Label("⚠️ Payment Integration Coming Soon!");
                paymentNotice.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

                Label paymentInfo = new Label(
                    "Your order has been placed and saved to our system.\n" +
                    "Payment processing is not yet available.\n\n" +
                    "Once payment integration is implemented, you will be\n" +
                    "able to complete your purchase using various payment\n" +
                    "methods including credit cards and mobile money."
                );
                paymentInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                paymentInfo.setWrapText(true);

                Label viewOrdersLabel = new Label("📦 You can view your orders in 'My Orders' section.");
                viewOrdersLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db;");

                content.getChildren().addAll(
                    orderIdLabel, dateLabel, shippingBox, totalBox, statusLabel, divider,
                    paymentNotice, paymentInfo, viewOrdersLabel
                );

                successAlert.getDialogPane().setContent(content);
                successAlert.getDialogPane().setPrefWidth(480);

                successAlert.showAndWait();

                // Navigate to My Orders
                showMyOrders();
            } else {
                showAlert(Alert.AlertType.ERROR, "Order Failed",
                    "Failed to create your order. Please try again.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                "An error occurred while processing your order: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        SessionManager.getInstance().logout();
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
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void setActiveButton(Button activeBtn) {
        btnHome.getStyleClass().remove("nav-button-active");
        btnShop.getStyleClass().remove("nav-button-active");
        btnOrders.getStyleClass().remove("nav-button-active");
        btnCart.getStyleClass().remove("nav-button-active");
        btnProfile.getStyleClass().remove("nav-button-active");

        activeBtn.getStyleClass().add("nav-button-active");
    }

    private void setStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gets the price of a product from the product_item table.
     */
    private double getProductPrice(UUID productId) {
        try {
            java.sql.Connection conn = com.amalitech.smartecommerce.utils.DBConnection.getConnection();
            String sql = "SELECT price FROM product_item WHERE product_id = ? LIMIT 1";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, productId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("price");
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Default price if not found
    }

    /**
     * Gets the product_item_id for a product.
     * If no product_item exists, creates one automatically.
     */
    private UUID getProductItemId(UUID productId) {
        try {
            java.sql.Connection conn = com.amalitech.smartecommerce.utils.DBConnection.getConnection();
            String sql = "SELECT id FROM product_item WHERE product_id = ? LIMIT 1";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, productId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return (UUID) rs.getObject("id");
                    }
                }
            }

            // No product_item found, create one automatically
            return createProductItemForProduct(productId);

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a product_item record for a product that doesn't have one.
     */
    private UUID createProductItemForProduct(UUID productId) {
        UUID productItemId = UUID.randomUUID();
        String sql = "INSERT INTO product_item (id, product_id, qty_in_stock, price, image) VALUES (?, ?, ?, ?, ?)";
        try {
            java.sql.Connection conn = com.amalitech.smartecommerce.utils.DBConnection.getConnection();
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, productItemId);
                stmt.setObject(2, productId);
                stmt.setInt(3, 100);  // Default stock
                stmt.setDouble(4, 10.0);  // Default price $10
                stmt.setString(5, null);
                if (stmt.executeUpdate() > 0) {
                    System.out.println("Created product_item for product: " + productId);
                    return productItemId;
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Failed to create product_item: " + e.getMessage());
        }
        return null;
    }

    /**
     * Creates an order line for a cart item.
     */
    private void createOrderLine(UUID orderId, Product product, int quantity) throws SQLException {
        UUID productItemId = getProductItemId(product.getId());
        if (productItemId == null) {
            System.err.println("Could not get or create product_item for product: " + product.getName());
            return;
        }

        double price = getProductPrice(product.getId());
        // If price is 0, set a default price
        if (price <= 0) {
            price = 10.0; // Default price
        }

        OrderLine orderLine = new OrderLine();
        orderLine.setId(UUID.randomUUID());
        orderLine.setOrderId(orderId);
        orderLine.setProductItemId(productItemId);
        orderLine.setQty(quantity);
        orderLine.setPrice(price);

        OrderLine created = orderLineDao.create(orderLine);
        if (created == null) {
            System.err.println("Failed to create order line for product: " + product.getName());
        }
    }
}

