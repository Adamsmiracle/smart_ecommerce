package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.cache.CategoryCache;
import com.amalitech.smartecommerce.cache.ProductCache;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.model.ProductCategory;
import com.amalitech.smartecommerce.service.ProductCategoryService;
import com.amalitech.smartecommerce.service.ProductCategoryServiceImpl;
import com.amalitech.smartecommerce.utils.InputValidator;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.*;

/**
 * Controller for category management view.
 */
public class CategoryController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TreeView<ProductCategory> treeCategories;
    @FXML private TableView<ProductCategory> tblCategories;
    @FXML private TableColumn<ProductCategory, String> colParent;
    @FXML private TableColumn<ProductCategory, Integer> colProductCount;

    private final ProductCategoryService categoryService = new ProductCategoryServiceImpl();
    private final CategoryCache categoryCache = CategoryCache.getInstance();
    private final ProductCache productCache = ProductCache.getInstance();

    private ObservableList<ProductCategory> categoryList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupTree();
        loadCategories();
    }

    private void setupTable() {
        tblCategories.setItems(categoryList);

        // Parent category column
        colParent.setCellValueFactory(cellData -> {
            UUID parentId = cellData.getValue().getParentCategoryId();
            if (parentId == null) {
                return new SimpleStringProperty("(Root)");
            }
            ProductCategory parent = categoryCache.getById(parentId);
            return new SimpleStringProperty(parent != null ? parent.getCategoryName() : "Unknown");
        });

        // Product count column
        colProductCount.setCellValueFactory(cellData -> {
            UUID categoryId = cellData.getValue().getId();
            List<Product> products = productCache.getByCategory(categoryId);
            return new SimpleIntegerProperty(products.size()).asObject();
        });

        // Sync tree selection with table
        tblCategories.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectInTree(newVal);
            }
        });
    }

    private void setupTree() {
        TreeItem<ProductCategory> root = new TreeItem<>(null);
        root.setExpanded(true);
        treeCategories.setRoot(root);
        treeCategories.setShowRoot(false);

        // Custom cell factory
        treeCategories.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCategoryName());
                }
            }
        });

        // Sync table selection with tree
        treeCategories.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null) {
                tblCategories.getSelectionModel().select(newVal.getValue());
            }
        });
    }

    private void loadCategories() {
        List<ProductCategory> categories = categoryCache.getAll();
        categoryList.setAll(categories);
        buildTree(categories);
    }

    private void buildTree(List<ProductCategory> categories) {
        TreeItem<ProductCategory> root = treeCategories.getRoot();
        root.getChildren().clear();

        // Map to store tree items by category ID
        Map<UUID, TreeItem<ProductCategory>> itemMap = new HashMap<>();

        // First pass: create all tree items
        for (ProductCategory category : categories) {
            TreeItem<ProductCategory> item = new TreeItem<>(category);
            item.setExpanded(true);
            itemMap.put(category.getId(), item);
        }

        // Second pass: build hierarchy
        for (ProductCategory category : categories) {
            TreeItem<ProductCategory> item = itemMap.get(category.getId());
            UUID parentId = category.getParentCategoryId();

            if (parentId == null) {
                // Root level category
                root.getChildren().add(item);
            } else {
                // Child category
                TreeItem<ProductCategory> parentItem = itemMap.get(parentId);
                if (parentItem != null) {
                    parentItem.getChildren().add(item);
                } else {
                    // Parent not found, add to root
                    root.getChildren().add(item);
                }
            }
        }
    }

    private void selectInTree(ProductCategory category) {
        TreeItem<ProductCategory> root = treeCategories.getRoot();
        TreeItem<ProductCategory> found = findTreeItem(root, category);
        if (found != null) {
            treeCategories.getSelectionModel().select(found);
        }
    }

    private TreeItem<ProductCategory> findTreeItem(TreeItem<ProductCategory> parent, ProductCategory category) {
        if (parent.getValue() != null && parent.getValue().getId().equals(category.getId())) {
            return parent;
        }
        for (TreeItem<ProductCategory> child : parent.getChildren()) {
            TreeItem<ProductCategory> found = findTreeItem(child, category);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @FXML
    public void searchCategories() {
        String query = txtSearch.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            loadCategories();
            return;
        }

        List<ProductCategory> filtered = categoryCache.getAll().stream()
            .filter(c -> c.getCategoryName() != null &&
                        c.getCategoryName().toLowerCase().contains(query))
            .toList();

        categoryList.setAll(filtered);
    }

    @FXML
    public void showAddDialog() {
        Dialog<ProductCategory> dialog = createCategoryDialog(null);
        Optional<ProductCategory> result = dialog.showAndWait();

        result.ifPresent(category -> {
            try {
                if (category.getId() == null) {
                    category.setId(UUID.randomUUID());
                }
                boolean created = categoryService.createCategory(category);
                if (created) {
                    categoryCache.put(category);
                    loadCategories();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Category created successfully!");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create category: " + e.getMessage());
            }
        });
    }

    @FXML
    public void showEditDialog() {
        ProductCategory selected = tblCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a category to edit.");
            return;
        }

        Dialog<ProductCategory> dialog = createCategoryDialog(selected);
        Optional<ProductCategory> result = dialog.showAndWait();

        result.ifPresent(category -> {
            try {
                boolean updated = categoryService.updateCategory(category);
                if (updated) {
                    categoryCache.update(category);
                    loadCategories();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully!");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update category: " + e.getMessage());
            }
        });
    }

    @FXML
    public void deleteCategory() {
        ProductCategory selected = tblCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a category to delete.");
            return;
        }

        // Check if category has products
        List<Product> products = productCache.getByCategory(selected.getId());
        if (!products.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cannot Delete",
                "This category has " + products.size() + " products. Remove or reassign them first.");
            return;
        }

        // Check if category has subcategories
        List<ProductCategory> subcategories = categoryCache.getSubcategories(selected.getId());
        if (!subcategories.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cannot Delete",
                "This category has subcategories. Remove or reassign them first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Category");
        confirm.setContentText("Are you sure you want to delete '" + selected.getCategoryName() + "'?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Store for potential rollback
            final ProductCategory deletedCategory = selected;

            // OPTIMISTIC UPDATE: Remove from UI immediately
            categoryList.remove(selected);
            categoryCache.remove(selected.getId());
            buildTree(new ArrayList<>(categoryList));

            // Delete from database in background
            Task<Boolean> deleteTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return categoryService.deleteCategory(deletedCategory.getId());
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        if (getValue()) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted successfully!");
                        } else {
                            // Rollback on failure
                            categoryCache.put(deletedCategory);
                            loadCategories();
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete category from database.");
                        }
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        categoryCache.put(deletedCategory);
                        loadCategories();
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete category: " + getException().getMessage());
                    });
                }
            };

            new Thread(deleteTask).start();
        }
    }

    private Dialog<ProductCategory> createCategoryDialog(ProductCategory existing) {
        Dialog<ProductCategory> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Category" : "Edit Category");
        dialog.setHeaderText(existing == null ? "Create a new category" : "Edit category details");

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
        txtName.setPromptText("Category Name (required)");

        ComboBox<ProductCategory> cmbParent = new ComboBox<>();
        List<ProductCategory> parentOptions = new ArrayList<>();
        parentOptions.add(null); // Option for no parent (root)
        parentOptions.addAll(categoryCache.getAll());
        cmbParent.setItems(FXCollections.observableArrayList(parentOptions));
        cmbParent.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "(None - Root Category)" : item.getCategoryName()));
            }
        });
        cmbParent.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ProductCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "(None - Root Category)" : item.getCategoryName()));
            }
        });

        // Pre-fill if editing
        if (existing != null) {
            txtName.setText(existing.getCategoryName());
            if (existing.getParentCategoryId() != null) {
                for (ProductCategory cat : categoryCache.getAll()) {
                    if (cat.getId().equals(existing.getParentCategoryId())) {
                        cmbParent.setValue(cat);
                        break;
                    }
                }
            }
        }

        grid.add(lblError, 0, 0, 2, 1);
        grid.add(new Label("Name:*"), 0, 1);
        grid.add(txtName, 1, 1);
        grid.add(new Label("Parent:"), 0, 2);
        grid.add(cmbParent, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Get save button and add validation
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Validate category name
            String nameError = InputValidator.getCategoryNameError(txtName.getText());
            if (nameError != null) {
                lblError.setText(nameError);
                event.consume();
                return;
            }

            // Check for duplicate name
            String newName = txtName.getText().trim();
            for (ProductCategory cat : categoryCache.getAll()) {
                if (cat.getCategoryName().equalsIgnoreCase(newName) &&
                    (existing == null || !cat.getId().equals(existing.getId()))) {
                    lblError.setText("A category with this name already exists.");
                    event.consume();
                    return;
                }
            }

            lblError.setText("");
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                ProductCategory category = existing != null ? existing : new ProductCategory();
                category.setCategoryName(txtName.getText().trim());
                ProductCategory parent = cmbParent.getValue();
                category.setParentCategoryId(parent != null ? parent.getId() : null);
                return category;
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

