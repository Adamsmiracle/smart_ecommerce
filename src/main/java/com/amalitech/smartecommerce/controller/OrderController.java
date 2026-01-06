package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.cache.OrderCache;
import com.amalitech.smartecommerce.cache.ProductCache;
import com.amalitech.smartecommerce.cache.UserCache;
import com.amalitech.smartecommerce.dao.OrderLineDao;
import com.amalitech.smartecommerce.dao.OrderLineDaoImpl;
import com.amalitech.smartecommerce.model.*;
import com.amalitech.smartecommerce.service.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for order management view.
 */
public class OrderController implements Initializable {

    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private TableView<Order> tblOrders;
    @FXML private TableColumn<Order, String> colOrderNum;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, String> colShipping;

    @FXML private Label lblTotalOrders;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblPendingOrders;
    @FXML private Label lblCompletedOrders;

    private final OrderService orderService = new OrderServiceImpl();
    private final UserService userService = new UserServiceImpl();
    private final ShippingMethodService shippingMethodService = new ShippingMethodServiceImpl();
    private final OrderStatusService orderStatusService = new OrderStatusServiceImpl();
    private final OrderLineDao orderLineDao = new OrderLineDaoImpl();
    private final ProductService productService = new ProductServiceImpl();
    private final UserCache userCache = UserCache.getInstance();
    private final OrderCache orderCache = OrderCache.getInstance();
    private final ProductCache productCache = ProductCache.getInstance();
    private ObservableList<Order> orderList = FXCollections.observableArrayList();
    private List<Order> allOrders;
    private Map<UUID, String> shippingMethodCache = new HashMap<>();
    private Map<UUID, String> orderStatusCache = new HashMap<>(); // Cache for order status names

    // Track order statuses for filtering
    private Map<UUID, String> orderStatusMap = new HashMap<>();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserCache();
        loadShippingMethodCache();
        loadOrderStatusCache();
        setupTable();
        setupFilters();
        loadOrders();
    }

    private void loadUserCache() {
        try {
            // Load users into shared cache if not already loaded
            if (userCache.getSize() == 0) {
                List<User> users = userService.getAllUsers();
                userCache.loadAll(users);
            }
        } catch (Exception e) {
            // Continue without cache
        }
    }

    private void loadShippingMethodCache() {
        try {
            List<ShippingMethod> methods = shippingMethodService.getAllShippingMethods();
            for (ShippingMethod method : methods) {
                if (method.getId() != null) {
                    shippingMethodCache.put(method.getId(), method.getName());
                }
            }
        } catch (Exception e) {
            // Continue without cache
        }
    }

    private void loadOrderStatusCache() {
        try {
            List<OrderStatus> statuses = orderStatusService.getAllOrderStatuses();
            for (OrderStatus status : statuses) {
                if (status.getId() != null) {
                    orderStatusCache.put(status.getId(), status.getStatus());
                }
            }
        } catch (Exception e) {
            // Continue without cache
        }
    }

    private void setupTable() {
        tblOrders.setItems(orderList);

        // Order number column (display-friendly number)
        colOrderNum.setCellValueFactory(cellData -> {
            int index = orderList.indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty("#" + String.format("%04d", index));
        });

        // Customer column - show actual customer name from UserCache
        colCustomer.setCellValueFactory(cellData -> {
            UUID userId = cellData.getValue().getUserId();
            if (userId != null) {
                User user = userCache.getById(userId);
                if (user != null) {
                    String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                                      (user.getLastName() != null ? user.getLastName() : "")).trim();
                    return new SimpleStringProperty(fullName.isEmpty() ? user.getEmailAddress() : fullName);
                }
            }
            return new SimpleStringProperty("Unknown Customer");
        });

        // Status column - get status from database or derive from order data
        colStatus.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            // First check if order has a status ID
            if (order.getOrderStatus() != null && orderStatusCache.containsKey(order.getOrderStatus())) {
                String status = orderStatusCache.get(order.getOrderStatus());
                orderStatusMap.put(order.getId(), status);
                return new SimpleStringProperty(status);
            }
            // Fallback to deriving status
            String status = determineOrderStatus(order);
            orderStatusMap.put(order.getId(), status);
            return new SimpleStringProperty(status);
        });

        // Shipping column - show actual shipping method name from database
        colShipping.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            // First check if we have a shipping method name stored in the order object
            if (order.getShippingMethodName() != null && !order.getShippingMethodName().isEmpty()) {
                return new SimpleStringProperty(order.getShippingMethodName());
            }
            // Try to look up the shipping method by ID from cache
            if (order.getShippingMethodId() != null) {
                String methodName = shippingMethodCache.get(order.getShippingMethodId());
                if (methodName != null) {
                    return new SimpleStringProperty(methodName);
                }
                return new SimpleStringProperty("Shipping Selected");
            }
            return new SimpleStringProperty("Not Selected");
        });
    }

    private String determineOrderStatus(Order order) {
        // First check if order has a status ID in the database
        if (order.getOrderStatus() != null) {
            String statusName = orderStatusCache.get(order.getOrderStatus());
            if (statusName != null) {
                return statusName;
            }
        }

        // Fallback: determine status based on order properties
        if (order.getOrderTotal() != null && order.getOrderTotal() > 0) {
            if (order.getShippingMethodId() != null) {
                return "Processing";
            }
            return "Pending";
        }
        return "Pending";
    }

    private void setupFilters() {
        cmbStatus.setItems(FXCollections.observableArrayList(
            "All", "Pending", "Processing", "Completed", "Cancelled"
        ));
        cmbStatus.setValue("All");

        // Add listener to filter when status changes
        cmbStatus.setOnAction(e -> filterOrders());
    }

    private void loadOrders() {
        // Show loading state
        tblOrders.setPlaceholder(new ProgressIndicator());
        lblTotalOrders.setText("Loading...");
        lblTotalRevenue.setText("Loading...");
        lblPendingOrders.setText("...");
        lblCompletedOrders.setText("...");

        Task<List<Order>> loadTask = new Task<>() {
            @Override
            protected List<Order> call() throws Exception {
                // Check cache first
                if (orderCache.getSize() > 0) {
                    return orderCache.getAll();
                }
                // Load from database and populate cache
                List<Order> orders = orderService.getAllOrders();
                orderCache.loadAll(orders);
                return orders;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    allOrders = getValue();
                    orderList.setAll(allOrders);
                    updateSummary();
                    tblOrders.setPlaceholder(new Label("No orders found."));
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    tblOrders.setPlaceholder(new Label("Failed to load orders"));
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + getException().getMessage());
                });
            }
        };

        new Thread(loadTask).start();
    }

    private void updateSummary() {
        lblTotalOrders.setText("Total Orders: " + orderList.size());

        double totalRevenue = orderList.stream()
            .mapToDouble(o -> o.getOrderTotal() != null ? o.getOrderTotal() : 0)
            .sum();
        lblTotalRevenue.setText(String.format("Total Revenue: $%.2f", totalRevenue));

        // Count by status
        long pendingCount = allOrders.stream()
            .filter(o -> "Pending".equals(determineOrderStatus(o)))
            .count();
        long completedCount = allOrders.stream()
            .filter(o -> "Completed".equals(determineOrderStatus(o)))
            .count();

        lblPendingOrders.setText("Pending: " + pendingCount);
        lblCompletedOrders.setText("Completed: " + completedCount);
    }

    @FXML
    public void filterOrders() {
        LocalDate fromDate = dpFromDate.getValue();
        LocalDate toDate = dpToDate.getValue();
        String status = cmbStatus.getValue();

        List<Order> filtered = allOrders.stream()
            .filter(order -> {
                // Date filter
                if (fromDate != null && order.getOrderDate() != null &&
                    order.getOrderDate().isBefore(fromDate)) {
                    return false;
                }
                if (toDate != null && order.getOrderDate() != null &&
                    order.getOrderDate().isAfter(toDate)) {
                    return false;
                }
                // Status filter
                if (status != null && !"All".equals(status)) {
                    String orderStatus = determineOrderStatus(order);
                    if (!status.equals(orderStatus)) {
                        return false;
                    }
                }
                return true;
            })
            .collect(Collectors.toList());

        orderList.setAll(filtered);
        updateSummary();
    }

    @FXML
    public void clearFilters() {
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        cmbStatus.setValue("All");
        orderList.setAll(allOrders);
        updateSummary();
    }

    @FXML
    public void showOrderDetails() {
        Order selected = tblOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to view.");
            return;
        }

        // Get customer name from UserCache
        String customerName = "Unknown Customer";
        if (selected.getUserId() != null) {
            User user = userCache.getById(selected.getUserId());
            if (user != null) {
                String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                                  (user.getLastName() != null ? user.getLastName() : "")).trim();
                customerName = fullName.isEmpty() ? user.getEmailAddress() : fullName;
            }
        }

        // Get shipping method name
        String shippingMethod = "Not Selected";
        if (selected.getShippingMethodName() != null && !selected.getShippingMethodName().isEmpty()) {
            shippingMethod = selected.getShippingMethodName();
        } else if (selected.getShippingMethodId() != null) {
            shippingMethod = shippingMethodCache.getOrDefault(selected.getShippingMethodId(), "Shipping Selected");
        }

        // Get order items with product names using a single JOIN query
        List<OrderItemDetails> orderItems = getOrderItemsWithProductNames(selected.getId());

        StringBuilder details = new StringBuilder();
        details.append("Order ID: ").append(selected.getId().toString().substring(0, 8).toUpperCase()).append("\n\n");
        details.append("👤 Customer: ").append(customerName).append("\n");
        details.append("📅 Order Date: ").append(selected.getOrderDate()).append("\n");
        details.append("📋 Status: ").append(determineOrderStatus(selected)).append("\n");
        details.append("🚚 Shipping: ").append(shippingMethod).append("\n\n");

        // Display order items/products
        details.append("📦 ORDER ITEMS:\n");
        details.append("─".repeat(30)).append("\n");

        if (orderItems.isEmpty()) {
            details.append("No items found for this order.\n");
        } else {
            double itemsTotal = 0;
            for (OrderItemDetails item : orderItems) {
                double lineTotal = item.price * item.quantity;
                itemsTotal += lineTotal;

                details.append(String.format("• %s\n", item.productName));
                details.append(String.format("  Qty: %d × $%.2f = $%.2f\n",
                    item.quantity,
                    item.price,
                    lineTotal));
            }
            details.append("─".repeat(30)).append("\n");
            details.append(String.format("Items Subtotal: $%.2f\n", itemsTotal));
        }

        details.append("\n💰 ORDER TOTAL: $").append(String.format("%.2f", selected.getOrderTotal() != null ? selected.getOrderTotal() : 0.0));

        // Create a scrollable text area for the details
        TextArea textArea = new TextArea(details.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(400);
        textArea.setPrefHeight(350);
        textArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Order #" + selected.getId().toString().substring(0, 8).toUpperCase());
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefWidth(450);
        alert.showAndWait();
    }

    /**
     * Inner class to hold order item details with product name.
     */
    private static class OrderItemDetails {
        String productName;
        int quantity;
        double price;

        OrderItemDetails(String productName, int quantity, double price) {
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }
    }

    /**
     * Gets all order items with product names using a single JOIN query.
     * This is more efficient than querying each product individually.
     */
    private List<OrderItemDetails> getOrderItemsWithProductNames(UUID orderId) {
        List<OrderItemDetails> items = new ArrayList<>();

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
                        items.add(new OrderItemDetails(
                            productName != null ? productName : "Unknown Product",
                            qty,
                            price
                        ));
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error fetching order items: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    @FXML
    public void updateOrderStatus() {
        Order selected = tblOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to update.");
            return;
        }

        // Load available statuses from database
        List<OrderStatus> statuses = orderStatusService.getAllOrderStatuses();

        if (statuses.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Statuses",
                "No order statuses found in the database. Please add order statuses first.");
            return;
        }

        // Create a dialog with status options
        Dialog<OrderStatus> dialog = new Dialog<>();
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Update status for Order #" + selected.getId().toString().substring(0, 8).toUpperCase());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label instructionLabel = new Label("Select new status:");
        instructionLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<OrderStatus> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(statuses));
        statusCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(OrderStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getStatus());
            }
        });
        statusCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(OrderStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Status" : item.getStatus());
            }
        });
        statusCombo.setPrefWidth(200);

        // Pre-select current status if exists
        if (selected.getOrderStatus() != null) {
            for (OrderStatus status : statuses) {
                if (status.getId().equals(selected.getOrderStatus())) {
                    statusCombo.setValue(status);
                    break;
                }
            }
        }

        content.getChildren().addAll(instructionLabel, statusCombo);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style OK button
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Update");
        okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return statusCombo.getValue();
            }
            return null;
        });

        Optional<OrderStatus> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            // Store original for rollback
            UUID originalStatusId = selected.getOrderStatus();

            // OPTIMISTIC UPDATE: Update cache and UI immediately
            selected.setOrderStatus(newStatus.getId());
            orderCache.update(selected);
            orderStatusCache.put(newStatus.getId(), newStatus.getStatus());
            tblOrders.refresh();

            // Update in database in background
            Task<Boolean> updateTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return orderService.updateOrder(selected);
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        if (getValue()) {
                            showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Order status updated to: " + newStatus.getStatus());
                        } else {
                            // Rollback
                            selected.setOrderStatus(originalStatusId);
                            orderCache.update(selected);
                            tblOrders.refresh();
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update order status.");
                        }
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        // Rollback
                        selected.setOrderStatus(originalStatusId);
                        orderCache.update(selected);
                        tblOrders.refresh();
                        showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to update order status: " + getException().getMessage());
                    });
                }
            };

            new Thread(updateTask).start();
        });
    }

    @FXML
    public void deleteOrder() {
        Order selected = tblOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Order");
        confirm.setContentText("Are you sure you want to delete Order #" +
            selected.getId().toString().substring(0, 8) + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Store for potential rollback
            final Order deletedOrder = selected;
            final int deletedIndex = orderList.indexOf(selected);

            // OPTIMISTIC UPDATE: Remove from cache and UI immediately
            orderCache.remove(selected.getId());
            orderList.remove(selected);
            allOrders.remove(selected);
            updateSummary();

            // Delete from database in background
            Task<Boolean> deleteTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return orderService.deleteOrder(deletedOrder.getId());
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        if (getValue()) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Order deleted successfully!");
                        } else {
                            // Rollback on failure
                            rollbackOrderDelete(deletedOrder, deletedIndex);
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete order from database.");
                        }
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        rollbackOrderDelete(deletedOrder, deletedIndex);
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete order: " + getException().getMessage());
                    });
                }
            };

            new Thread(deleteTask).start();
        }
    }

    private void rollbackOrderDelete(Order order, int originalIndex) {
        // Restore to cache
        orderCache.put(order);

        // Restore to lists
        if (originalIndex >= 0 && originalIndex <= allOrders.size()) {
            allOrders.add(originalIndex, order);
        } else {
            allOrders.add(order);
        }
        orderList.setAll(allOrders);
        updateSummary();
    }

    @FXML
    public void exportReport() {
        StringBuilder report = new StringBuilder();
        report.append("ORDER REPORT\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");
        report.append("Total Orders: ").append(orderList.size()).append("\n");

        double totalRevenue = orderList.stream()
            .mapToDouble(o -> o.getOrderTotal() != null ? o.getOrderTotal() : 0)
            .sum();
        report.append("Total Revenue: $").append(String.format("%.2f", totalRevenue)).append("\n\n");

        report.append("ORDER DETAILS:\n");
        report.append("-".repeat(50)).append("\n");

        for (Order order : orderList) {
            report.append(String.format("Order #%s | Date: %s | Total: $%.2f\n",
                order.getId().toString().substring(0, 8),
                order.getOrderDate(),
                order.getOrderTotal() != null ? order.getOrderTotal() : 0));
        }

        // Show report in dialog
        TextArea textArea = new TextArea(report.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(500);
        textArea.setPrefHeight(400);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Report");
        alert.setHeaderText("Generated Report");
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

