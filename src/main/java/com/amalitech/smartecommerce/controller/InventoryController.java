package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.cache.CategoryCache;
import com.amalitech.smartecommerce.cache.InventoryCache;
import com.amalitech.smartecommerce.cache.ProductCache;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductCategory;
import com.amalitech.smartecommerce.model.ProductItem;
import com.amalitech.smartecommerce.service.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for inventory management view.
 */
public class InventoryController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(InventoryController.class.getName());

    @FXML private TableView<InventoryItem> tblInventory;
    @FXML private TableColumn<InventoryItem, String> colProduct;
    @FXML private TableColumn<InventoryItem, String> colCategory;
    @FXML private TableColumn<InventoryItem, String> colQuantity;
    @FXML private TableColumn<InventoryItem, String> colStatus;
    @FXML private TableColumn<InventoryItem, String> colLastUpdated;
    @FXML private TableColumn<InventoryItem, Void> colActions;

    @FXML private ComboBox<String> cmbStockFilter;
    @FXML private ComboBox<ProductCategory> cmbCategoryFilter;
    @FXML private TextField txtSearch;

    @FXML private Label lblTotalProducts;
    @FXML private Label lblInStock;
    @FXML private Label lblLowStock;
    @FXML private Label lblOutOfStock;
    @FXML private Label lblInventorySummary;

    private final ProductService productService = new ProductServiceImpl();
    private final ProductCategoryService categoryService = new ProductCategoryServiceImpl();
    private final ProductCache productCache = ProductCache.getInstance();
    private final CategoryCache categoryCache = CategoryCache.getInstance();
    private final InventoryCache inventoryCache = InventoryCache.getInstance();

    private ObservableList<InventoryItem> inventoryList = FXCollections.observableArrayList();
    private List<InventoryItem> allInventoryItems = new ArrayList<>();

    // Map to track ProductItem objects for database persistence
    private Map<UUID, ProductItem> productItemMap = new HashMap<>();
    // Cached inventory quantities (loaded from database via ProductService)
    private Map<UUID, Integer> inventoryQuantities = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        try {
            setupFilters();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        loadInventory();
    }

    private void setupTable() {
        tblInventory.setItems(inventoryList);

        // Make columns resize to fill available space responsively
        tblInventory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Ensure column pref widths (will be used as relative weights with constrained resize)
        colProduct.setPrefWidth(300);
        colCategory.setPrefWidth(180);
        colQuantity.setPrefWidth(120);
        colStatus.setPrefWidth(140);
        colLastUpdated.setPrefWidth(180);
        colActions.setPrefWidth(220);

        colProduct.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getProductName()));

        colCategory.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCategoryName()));

        colQuantity.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantity())));

        colStatus.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStatus()));

        // Style the status column
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "In Stock" -> setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        case "Low Stock" -> setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        case "Out of Stock" -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        colLastUpdated.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getLastUpdated()));

        // Actions column
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Edit qty");
            private final HBox actionBox = new HBox(5, editBtn);

            {
                actionBox.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 3 10; -fx-cursor: hand;");

                editBtn.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    showEditQuantityDialog(item);
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private void setupFilters() throws SQLException {
        // ... stock status filter code remains same ...

        // 1. Fetch categories
        List<ProductCategory> categoriesFromDb = categoryCache.getAll();
        if (categoriesFromDb.isEmpty()) {
            categoriesFromDb = categoryService.getAllCategories();
        }

        // 2. Create the "All Categories" option
        ProductCategory allCategory = new ProductCategory();
        allCategory.setCategoryName("All Categories");
        // We leave ID null or set a specific value to identify it as the "All" flag

        // 3. Create the list and add "All" at the very top
        ObservableList<ProductCategory> categoryList = FXCollections.observableArrayList();
        categoryList.add(allCategory);
        categoryList.addAll(categoriesFromDb);

        cmbCategoryFilter.setItems(categoryList);

        // 4. Set "All Categories" as the default starting selection
        cmbCategoryFilter.setValue(allCategory);

        // 5. Standardize display logic
        cmbCategoryFilter.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCategoryName());
            }
        });

        cmbCategoryFilter.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "All Categories" : item.getCategoryName());
            }
        });
    }

    private void loadInventory() {
        // Show loading state
        lblTotalProducts.setText("...");
        lblInStock.setText("...");
        lblLowStock.setText("...");
        lblOutOfStock.setText("...");
        tblInventory.setPlaceholder(new ProgressIndicator());

        Task<List<InventoryItem>> loadTask = new Task<>() {
            @Override
            protected List<InventoryItem> call() throws Exception {
                List<Product> products = productCache.getAll();
                if (products.isEmpty()) {
                    products = productService.getAllProducts();
                }

                // OPTIMIZATION: Batch load all inventory data in ONE query
                // Instead of: 1 query per product (N+1 problem)
                List<ProductItem> allProductItems = productService.getAllProductItems();

                // Create map for fast lookup: product_id -> ProductItem
                Map<UUID, ProductItem> productItemByProductId = new HashMap<>();
                for (ProductItem item : allProductItems) {
                    productItemByProductId.put(item.getProductId(), item);
                }

                List<InventoryItem> items = new ArrayList<>();

                for (Product product : products) {
                    // Get quantity from map (O(1) lookup, no database query)
                    int quantity = 0;
                    ProductItem productItem = productItemByProductId.get(product.getId());

                    if (productItem != null) {
                        quantity = productItem.getQtyInStock();
                        // Cache the quantity for future use
                        inventoryQuantities.put(product.getId(), quantity);
                    }

                    String categoryName = getCategoryName(product.getCategoryId());

                    // Track ProductItem for persistence
                    if (productItem == null) {
                        productItem = new ProductItem();
                        productItem.setProductId(product.getId());
                        productItem.setQtyInStock(quantity);
                    }
                    productItemMap.put(product.getId(), productItem);

                    InventoryItem item = new InventoryItem(
                        product.getId(),
                        product.getName(),
                        categoryName,
                        quantity,
                        LocalDateTime.now()
                    );
                    items.add(item);
                }
                return items;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    allInventoryItems.clear();
                    allInventoryItems.addAll(getValue());
                    inventoryList.setAll(allInventoryItems);

                    // Populate InventoryCache with ProductItem data
                    List<ProductItem> productItems = new ArrayList<>(productItemMap.values());
                    inventoryCache.loadAll(productItems);

                    updateSummary();
                    tblInventory.setPlaceholder(new Label("No inventory data found"));
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    tblInventory.setPlaceholder(new Label("Failed to load inventory data"));
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load inventory: " + getException().getMessage());
                });
            }
        };

        new Thread(loadTask).start();
    }

    private String getCategoryName(UUID categoryId) {
        if (categoryId == null) return "Uncategorized";
        ProductCategory category = categoryCache.getById(categoryId);
        return category != null ? category.getCategoryName() : "Unknown";
    }

    private void updateSummary() {
        int total = allInventoryItems.size();
        long inStock = allInventoryItems.stream().filter(i -> i.getQuantity() > 10).count();
        long lowStock = allInventoryItems.stream().filter(i -> i.getQuantity() > 0 && i.getQuantity() <= 10).count();
        long outOfStock = allInventoryItems.stream().filter(i -> i.getQuantity() == 0).count();

        lblTotalProducts.setText(String.valueOf(total));
        lblInStock.setText(String.valueOf(inStock));
        lblLowStock.setText(String.valueOf(lowStock));
        lblOutOfStock.setText(String.valueOf(outOfStock));
        lblInventorySummary.setText("Showing " + inventoryList.size() + " of " + total + " items");
    }

    @FXML
    public void filterInventory() {
        String stockFilter = cmbStockFilter.getValue();
        ProductCategory categoryFilter = cmbCategoryFilter.getValue();
        String searchText = txtSearch.getText().toLowerCase().trim();

        List<InventoryItem> filtered = allInventoryItems.stream()
                .filter(item -> {
                    // Stock filter
                    if (stockFilter != null && !"All".equals(stockFilter)) {
                        if (!item.getStatus().equals(stockFilter)) return false;
                    }

                    // FIXED Category logic:
                    // Only filter if a category is selected AND it isn't the "All Categories" option
                    if (categoryFilter != null && !"All Categories".equals(categoryFilter.getCategoryName())) {
                        if (!item.getCategoryName().equals(categoryFilter.getCategoryName())) {
                            return false;
                        }
                    }

                    // Search filter
                    if (!searchText.isEmpty()) {
                        if (!item.getProductName().toLowerCase().contains(searchText)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        inventoryList.setAll(filtered);
        lblInventorySummary.setText("Showing " + filtered.size() + " of " + allInventoryItems.size() + " items");
    }
    @FXML
    public void clearFilters() {
        cmbStockFilter.setValue("All");
        cmbCategoryFilter.setValue(null);
        txtSearch.clear();
        inventoryList.setAll(allInventoryItems);
        lblInventorySummary.setText("Showing " + allInventoryItems.size() + " items");
    }

    @FXML
    public void refreshInventory() {
        loadInventory();
        showAlert(Alert.AlertType.ERROR, "Refreshed", "Inventory data has been refreshed.");
    }

    @FXML
    public void exportInventoryReport() {
        StringBuilder report = new StringBuilder();
        report.append("INVENTORY REPORT\n");
        report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        report.append(String.format("%-30s | %-15s | %-10s | %-12s\n", "Product", "Category", "Quantity", "Status"));
        report.append("-".repeat(75)).append("\n");

        for (InventoryItem item : allInventoryItems) {
            report.append(String.format("%-30s | %-15s | %-10d | %-12s\n",
                truncate(item.getProductName(), 30),
                truncate(item.getCategoryName(), 15),
                item.getQuantity(),
                item.getStatus()));
        }

        report.append("\n\nSUMMARY:\n");
        report.append("Total Products: ").append(lblTotalProducts.getText()).append("\n");
        report.append("In Stock: ").append(lblInStock.getText()).append("\n");
        report.append("Low Stock: ").append(lblLowStock.getText()).append("\n");
        report.append("Out of Stock: ").append(lblOutOfStock.getText()).append("\n");

        // Show report in a dialog
        TextArea textArea = new TextArea(report.toString());
        textArea.setEditable(false);
        textArea.setFont(javafx.scene.text.Font.font("Monospaced", 12));
        textArea.setPrefSize(600, 400);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inventory Report");
        alert.setHeaderText("Inventory Report Generated");
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }

    @FXML
    public void showAddStockDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Stock");
        dialog.setHeaderText("Add stock to a product");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(350);

        ComboBox<InventoryItem> productCombo = new ComboBox<>();
        productCombo.setItems(inventoryList);
        productCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getProductName() + " (Current: " + item.getQuantity() + ")");
            }
        });
        productCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select product" : item.getProductName());
            }
        });
        productCombo.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> quantitySpinner = new Spinner<>(1, 1000, 10);
        quantitySpinner.setEditable(true);
        quantitySpinner.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().addAll(
            new Label("Product:"), productCombo,
            new Label("Quantity to Add:"), quantitySpinner
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && productCombo.getValue() != null) {
                InventoryItem item = productCombo.getValue();
                adjustQuantity(item, quantitySpinner.getValue());
                showAlert(Alert.AlertType.INFORMATION, "Stock Added",
                    "Added " + quantitySpinner.getValue() + " units to " + item.getProductName());
            }
        });
    }


    private void showEditQuantityDialog(InventoryItem item) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getQuantity()));
        dialog.setTitle("Edit Quantity");
        dialog.setHeaderText("Edit stock quantity for " + item.getProductName());
        dialog.setContentText("New quantity:");

        dialog.showAndWait().ifPresent(result -> {
            try {
                int newQuantity = Integer.parseInt(result);
                if (newQuantity >= 0) {
                    // Update UI immediately for responsiveness
                    inventoryQuantities.put(item.getProductId(), newQuantity);
                    item.setQuantity(newQuantity);
                    tblInventory.refresh();
                    updateSummary();

                    // Persist to database asynchronously
                    updateProductQuantity(item.getProductId(), newQuantity);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Quantity cannot be negative.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number.");
            }
        });
    }

    private void adjustQuantity(InventoryItem item, int adjustment) {
        int newQuantity = Math.max(0, item.getQuantity() + adjustment);
        // Update UI immediately for responsiveness
        inventoryQuantities.put(item.getProductId(), newQuantity);
        item.setQuantity(newQuantity);
        tblInventory.refresh();
        updateSummary();

        // Persist to database asynchronously
        updateProductQuantity(item.getProductId(), newQuantity);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Inner class representing an inventory item.
     */
    public static class InventoryItem {
        private final UUID productId;
        private final String productName;
        private final String categoryName;
        private int quantity;
        private final LocalDateTime lastUpdated;

        public InventoryItem(UUID productId, String productName, String categoryName, int quantity, LocalDateTime lastUpdated) {
            this.productId = productId;
            this.productName = productName;
            this.categoryName = categoryName;
            this.quantity = quantity;
            this.lastUpdated = lastUpdated;
        }

        public UUID getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getCategoryName() { return categoryName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public String getStatus() {
            if (quantity == 0) return "Out of Stock";
            if (quantity <= 10) return "Low Stock";
            return "In Stock";
        }

        public String getLastUpdated() {
            return lastUpdated.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        }
    }

    /**
     * Update product quantity in the database asynchronously.
     * Updates the ProductItem stock and persists it via ProductService.
     * Also updates InventoryCache to keep it in sync.
     * @param productId The product ID to update
     * @param newQuantity The new stock quantity
     */
    private void updateProductQuantity(UUID productId, int newQuantity) {
         Task<Boolean> updateTask = new Task<>() {
             @Override
             protected Boolean call() throws Exception {
                 try {
                    ProductItem productItem = productItemMap.get(productId);
                    if (productItem != null) {
                        productItem.setQtyInStock(newQuantity);
                        // Use the ProductService to update via DAO; ProductService updates the InventoryCache internally
                        ProductItem updated = productService.updateProductStock(productItem);
                        return updated != null;
                    }
                     return false;
                 } catch (Exception e) {
                     LOGGER.log(Level.SEVERE, "Error updating product quantity: {0}", e.getMessage());
                     LOGGER.log(Level.SEVERE, "Exception details", e);
                     return false;
                 }
             }

             @Override
             protected void succeeded() {
                 if (!getValue()) {
                     Platform.runLater(() ->
                         showAlert(Alert.AlertType.WARNING, "Database Update",
                             "Could not persist quantity to database. UI has been updated.")
                     );
                 }
             }

             @Override
             protected void failed() {
                 Platform.runLater(() ->
                     showAlert(Alert.AlertType.ERROR, "Database Error",
                         "Failed to update quantity in database: " + getException().getMessage())
                 );
             }
         };

         // Run update task in background thread
         new Thread(updateTask).start();
     }
 }

