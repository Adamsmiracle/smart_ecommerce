package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.cache.CategoryCache;
import com.amalitech.smartecommerce.cache.ProductCache;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductCategory;
import com.amalitech.smartecommerce.service.ProductCategoryService;
import com.amalitech.smartecommerce.service.ProductCategoryServiceImpl;
import com.amalitech.smartecommerce.service.ProductService;
import com.amalitech.smartecommerce.service.ProductServiceImpl;
import com.amalitech.smartecommerce.utils.InputValidator;
import com.amalitech.smartecommerce.utils.PerformanceMonitor;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.net.URL;
import java.util.*;

/**
 * Controller for product management view.
 * Implements CRUD operations with caching and performance monitoring.
 */
public class ProductController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<ProductCategory> cmbCategory;
    @FXML private ComboBox<String> cmbSortBy;
    @FXML private TableView<Product> tblProducts;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private ToggleButton btnUseCache;
    @FXML private Label lblQueryTime;
    @FXML private Label lblTotalProducts;
    @FXML private Label lblPageInfo;
    @FXML private Button btnPrevPage;
    @FXML private Button btnNextPage;

    private final ProductService productService = new ProductServiceImpl();
    private final ProductCategoryService categoryService = new ProductCategoryServiceImpl();
    private final ProductCache productCache = ProductCache.getInstance();
    private final CategoryCache categoryCache = CategoryCache.getInstance();
    private final PerformanceMonitor perfMonitor = PerformanceMonitor.getInstance();

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private List<Product> allFilteredProducts = new ArrayList<>();

    // Pagination
    private static final int PAGE_SIZE = 20;
    private int currentPage = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupCategoryComboBox();
        setupSortComboBox();
        loadProducts();
    }

    private void setupTable() {
        tblProducts.setItems(productList);

        // Setup category column to show category name instead of ID
        colCategory.setCellValueFactory(cellData -> {
            UUID categoryId = cellData.getValue().getCategoryId();
            ProductCategory category = categoryCache.getById(categoryId);
            String categoryName = category != null ? category.getCategoryName() : "Unknown";
            return new SimpleStringProperty(categoryName);
        });

        // Enable row selection
        tblProducts.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void setupCategoryComboBox() {
        List<ProductCategory> categories = categoryCache.getAll();
        cmbCategory.setItems(FXCollections.observableArrayList(categories));

        // Custom cell factory to display category name
        cmbCategory.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCategoryName());
            }
        });
        cmbCategory.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "All Categories" : item.getCategoryName());
            }
        });

        // Filter products when category changes
        cmbCategory.setOnAction(e -> filterByCategory());
    }

    private void setupSortComboBox() {
        // Populate sort options
        cmbSortBy.setItems(FXCollections.observableArrayList("Name (A-Z)", "Name (Z-A)"));
        cmbSortBy.setOnAction(e -> sortProducts());
    }

    private void loadProducts() {
        // Show loading state
        lblQueryTime.setText("Loading...");
        tblProducts.setPlaceholder(new ProgressIndicator());

        Task<List<Product>> loadTask = new Task<>() {
            @Override
            protected List<Product> call() throws Exception {
                if (btnUseCache != null && btnUseCache.isSelected()) {
                    return perfMonitor.measureCacheOperation("Load All Products",
                        () -> productCache.getAll());
                } else {
                    return perfMonitor.measureDbOperation("Load All Products",
                        () -> productService.getAllProducts());
                }
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<Product> products = getValue();
                    allFilteredProducts = new ArrayList<>(products);
                    currentPage = 0;
                    updateTableWithPagination();
                    lblTotalProducts.setText(String.format("Total: %d products", products.size()));
                    lblQueryTime.setText("Query Time: 0 ms");
                    tblProducts.setPlaceholder(new Label("No products found. Click 'Add Product' to create one."));
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    lblQueryTime.setText("Error loading");
                    tblProducts.setPlaceholder(new Label("Failed to load products."));
                });
            }
        };

        new Thread(loadTask).start();
    }

    @FXML
    public void searchProducts() {
        String query = txtSearch.getText();
        long startTime = System.currentTimeMillis();
        List<Product> results;


            results = productService.searchProductsByName(query);


        long queryTime = System.currentTimeMillis() - startTime;
        lblQueryTime.setText(String.format("Query Time: %d ms", queryTime));

        allFilteredProducts = new ArrayList<>(results);
        currentPage = 0;
        updateTableWithPagination();
        lblTotalProducts.setText(String.format("Found: %d products", results.size()));
    }

    @FXML
    public void clearSearch() {
        txtSearch.clear();
        cmbCategory.getSelectionModel().clearSelection();
        loadProducts();
    }

    private void filterByCategory() {
        ProductCategory selected = cmbCategory.getValue();
        if (selected == null) {
            loadProducts();
            return;
        }

        long startTime = System.currentTimeMillis();
        List<Product> results;

        if (btnUseCache != null && btnUseCache.isSelected()) {
            results = perfMonitor.measureCacheOperation("Filter by Category",
                () -> productCache.getByCategory(selected.getId()));
        } else {
            results = perfMonitor.measureDbOperation("Filter by Category",
                () -> productService.getProductsByCategoryId(selected.getId()));
        }

        long queryTime = System.currentTimeMillis() - startTime;
        lblQueryTime.setText(String.format("Query Time: %d ms", queryTime));

        allFilteredProducts = new ArrayList<>(results);
        currentPage = 0;
        updateTableWithPagination();
        lblTotalProducts.setText(String.format("Found: %d products", results.size()));
    }

    private void sortProducts() {
        String sortOption = cmbSortBy.getValue();
        if (sortOption == null) return;

        long startTime = System.currentTimeMillis();

        if (btnUseCache != null && btnUseCache.isSelected()) {
            // Use cache's built-in sorting (QuickSort)
            if (sortOption.contains("A-Z")) {
                allFilteredProducts = productCache.getAllSortedByName(true);
            } else {
                allFilteredProducts = productCache.getAllSortedByName(false);
            }
            perfMonitor.recordCacheOperation("Sort Products", System.currentTimeMillis() - startTime);
        } else {
            // Sort in memory
            allFilteredProducts.sort((p1, p2) -> {
                String n1 = p1.getName() != null ? p1.getName() : "";
                String n2 = p2.getName() != null ? p2.getName() : "";
                return sortOption.contains("A-Z") ? n1.compareToIgnoreCase(n2) : n2.compareToIgnoreCase(n1);
            });
        }

        long queryTime = System.currentTimeMillis() - startTime;
        lblQueryTime.setText(String.format("Sort Time: %d ms", queryTime));

        currentPage = 0;
        updateTableWithPagination();
    }

    private void updateTableWithPagination() {
        int totalPages = (int) Math.ceil((double) allFilteredProducts.size() / PAGE_SIZE);
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, allFilteredProducts.size());

        if (fromIndex < allFilteredProducts.size()) {
            productList.setAll(allFilteredProducts.subList(fromIndex, toIndex));
        } else {
            productList.clear();
        }

        lblPageInfo.setText(String.format("Page %d of %d", currentPage + 1, Math.max(1, totalPages)));
        btnPrevPage.setDisable(currentPage == 0);
        btnNextPage.setDisable(currentPage >= totalPages - 1);
    }

    @FXML
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateTableWithPagination();
        }
    }

    @FXML
    public void nextPage() {
        int totalPages = (int) Math.ceil((double) allFilteredProducts.size() / PAGE_SIZE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateTableWithPagination();
        }
    }

    @FXML
    public void showAddDialog() {
        Dialog<ProductWithPrice> dialog = createProductDialog(null);
        Optional<ProductWithPrice> result = dialog.showAndWait();

        result.ifPresent(productWithPrice -> {
            try {
                Product created = productService.createProductWithPrice(
                    productWithPrice.product,
                    productWithPrice.price,
                    productWithPrice.stock
                );
                if (created != null) {
                    productCache.put(created);
                    loadProducts();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product created successfully!");
                }
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create product: " + e.getMessage());
            }
        });
    }

    @FXML
    public void showEditDialog() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to edit.");
            return;
        }

        Dialog<ProductWithPrice> dialog = createProductDialog(selected);
        Optional<ProductWithPrice> result = dialog.showAndWait();

        result.ifPresent(productWithPrice -> {
            try {
                Product updated = productService.updateProduct(productWithPrice.product);
                if (updated != null) {
                    // Update price and stock in product_item table
                    updateProductPriceAndStock(updated.getId(), productWithPrice.price, productWithPrice.stock);
                    productCache.update(updated);
                    loadProducts();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
                }
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product: " + e.getMessage());
            }
        });
    }

    @FXML
    public void deleteProduct() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Product");
        confirm.setContentText("Are you sure you want to delete '" + selected.getName() + "'?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Store for potential rollback
            final Product deletedProduct = selected;
            final int deletedIndex = productList.indexOf(selected);

            // OPTIMISTIC UPDATE: Remove from UI immediately for fast feedback
            productList.remove(selected);
            allFilteredProducts.remove(selected);
            productCache.remove(selected.getId());
            lblTotalProducts.setText(String.format("Total: %d products", allFilteredProducts.size()));

            // Delete from database in background
            Task<Product> deleteTask = new Task<>() {
                @Override
                protected Product call() throws Exception {
                    return productService.deleteProduct(deletedProduct.getId());
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        if (getValue() != null) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
                        } else {
                            // Rollback UI if delete failed
                            rollbackDelete(deletedProduct, deletedIndex);
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product from database.");
                        }
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        // Rollback UI on error
                        rollbackDelete(deletedProduct, deletedIndex);
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product: " + getException().getMessage());
                    });
                }
            };

            new Thread(deleteTask).start();
        }
    }

    /**
     * Rollback a delete operation by re-adding the product to the UI.
     */
    private void rollbackDelete(Product product, int originalIndex) {
        // Re-add to cache
        productCache.put(product);

        // Re-add to lists
        if (originalIndex >= 0 && originalIndex <= allFilteredProducts.size()) {
            allFilteredProducts.add(originalIndex, product);
        } else {
            allFilteredProducts.add(product);
        }

        // Update pagination
        updateTableWithPagination();
        lblTotalProducts.setText(String.format("Total: %d products", allFilteredProducts.size()));
    }

    private Dialog<ProductWithPrice> createProductDialog(Product existingProduct) {
        Dialog<ProductWithPrice> dialog = new Dialog<>();
        dialog.setTitle(existingProduct == null ? "Add Product" : "Edit Product");
        dialog.setHeaderText(existingProduct == null ? "Create a new product" : "Edit product details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Error label for validation messages
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        lblError.setWrapText(true);
        lblError.setMaxWidth(300);

        TextField txtName = new TextField();
        txtName.setPromptText("Product Name (required)");

        TextField txtPrice = new TextField();
        txtPrice.setPromptText("Price (e.g., 99.99)");

        TextField txtStock = new TextField();
        txtStock.setPromptText("Stock Quantity (e.g., 100)");

        TextArea txtDescription = new TextArea();
        txtDescription.setPromptText("Description");
        txtDescription.setPrefRowCount(3);
        TextField txtImage = new TextField();
        txtImage.setPromptText("Image URL");
        ComboBox<ProductCategory> cmbDialogCategory = new ComboBox<>();
        cmbDialogCategory.setItems(FXCollections.observableArrayList(categoryCache.getAll()));
        cmbDialogCategory.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCategoryName());
            }
        });
        cmbDialogCategory.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCategoryName());
            }
        });

        // Pre-fill if editing
        if (existingProduct != null) {
            txtName.setText(existingProduct.getName());
            txtDescription.setText(existingProduct.getDescription());
            txtImage.setText(existingProduct.getProductImage());
            // Find and select the category
            for (ProductCategory cat : categoryCache.getAll()) {
                if (cat.getId().equals(existingProduct.getCategoryId())) {
                    cmbDialogCategory.setValue(cat);
                    break;
                }
            }
            // Load existing price and stock from product_item
            double[] priceAndStock = getProductPriceAndStock(existingProduct.getId());
            txtPrice.setText(String.format("%.2f", priceAndStock[0]));
            txtStock.setText(String.valueOf((int) priceAndStock[1]));
        } else {
            txtStock.setText("100"); // Default stock
        }

        grid.add(lblError, 0, 0, 2, 1);
        grid.add(new Label("Name:*"), 0, 1);
        grid.add(txtName, 1, 1);
        grid.add(new Label("Category:*"), 0, 2);
        grid.add(cmbDialogCategory, 1, 2);
        grid.add(new Label("Price:*"), 0, 3);
        grid.add(txtPrice, 1, 3);
        grid.add(new Label("Stock:*"), 0, 4);
        grid.add(txtStock, 1, 4);
        grid.add(new Label("Description:"), 0, 5);
        grid.add(txtDescription, 1, 5);
        grid.add(new Label("Image:"), 0, 6);
        grid.add(txtImage, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Get save button and add validation
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Validate product name
            String nameError = InputValidator.getProductNameError(txtName.getText());
            if (nameError != null) {
                lblError.setText(nameError);
                event.consume();
                return;
            }

            // Validate category selection
            if (cmbDialogCategory.getValue() == null) {
                lblError.setText("Please select a category.");
                event.consume();
                return;
            }

            // Validate price
            String priceError = InputValidator.getPriceError(txtPrice.getText());
            if (priceError != null) {
                lblError.setText(priceError);
                event.consume();
                return;
            }

            // Validate stock
            String stockError = InputValidator.getStockError(txtStock.getText());
            if (stockError != null) {
                lblError.setText(stockError);
                event.consume();
                return;
            }

            // Validate description length
            if (!InputValidator.isValidDescription(txtDescription.getText())) {
                lblError.setText("Description is too long (max 1000 characters).");
                event.consume();
                return;
            }

            lblError.setText("");
        });

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Product product = existingProduct != null ? existingProduct : new Product();
                product.setName(txtName.getText().trim());
                product.setDescription(txtDescription.getText() != null ? txtDescription.getText().trim() : "");
                product.setProductImage(txtImage.getText() != null ? txtImage.getText().trim() : "");
                if (cmbDialogCategory.getValue() != null) {
                    product.setCategoryId(cmbDialogCategory.getValue().getId());
                }

                double price = Double.parseDouble(txtPrice.getText().trim());
                int stock = Integer.parseInt(txtStock.getText().trim());

                return new ProductWithPrice(product, price, stock);
            }
            return null;
        });

        return dialog;
    }

    /**
     * Helper class to hold product with price and stock info.
     */
    private static class ProductWithPrice {
        Product product;
        double price;
        int stock;

        ProductWithPrice(Product product, double price, int stock) {
            this.product = product;
            this.price = price;
            this.stock = stock;
        }
    }

    /**
     * Gets the price and stock for a product from product_item table.
     */
    private double[] getProductPriceAndStock(UUID productId) {
        double[] result = {0.0, 0.0};
        String sql = "SELECT price, qty_in_stock FROM product_item WHERE product_id = ? LIMIT 1";
        try {
            java.sql.Connection conn = com.amalitech.smartecommerce.utils.DBConnection.getConnection();
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, productId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        result[0] = rs.getDouble("price");
                        result[1] = rs.getDouble("qty_in_stock");
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error getting product price/stock: " + e.getMessage());
        }
        return result;
    }

    /**
     * Updates the price and stock for a product in product_item table.
     */
    private void updateProductPriceAndStock(UUID productId, double price, int stock) {
        String sql = "UPDATE product_item SET price = ?, qty_in_stock = ? WHERE product_id = ?";
        try {
            java.sql.Connection conn = com.amalitech.smartecommerce.utils.DBConnection.getConnection();
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, price);
                stmt.setInt(2, stock);
                stmt.setObject(3, productId);
                stmt.executeUpdate();
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error updating product price/stock: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

