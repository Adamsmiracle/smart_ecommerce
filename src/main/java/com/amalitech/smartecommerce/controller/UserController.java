package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.cache.UserCache;
import com.amalitech.smartecommerce.dto.UserCreateDto;
import com.amalitech.smartecommerce.dto.UserUpdateDto;
import com.amalitech.smartecommerce.model.Order;
import com.amalitech.smartecommerce.model.User;
import com.amalitech.smartecommerce.service.OrderService;
import com.amalitech.smartecommerce.service.OrderServiceImpl;
import com.amalitech.smartecommerce.service.UserService;
import com.amalitech.smartecommerce.service.UserServiceImpl;
import com.amalitech.smartecommerce.exception.EmailAlreadyExistsException;
import com.amalitech.smartecommerce.utils.ValidationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for user management view.
 */
public class UserController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TableView<User> tblUsers;
    @FXML private Label lblTotalUsers;

    private final UserService userService = new UserServiceImpl();
    private final UserCache userCache = UserCache.getInstance();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private List<User> allUsers;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadUsers();
    }

    private void setupTable() {
        tblUsers.setItems(userList);
        tblUsers.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void loadUsers() {
        // Show loading state
        tblUsers.setPlaceholder(new ProgressIndicator());
        lblTotalUsers.setText("Loading...");

        Task<List<User>> loadTask = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                // Check cache first
                if (userCache.getSize() > 0) {
                    return userCache.getAll();
                }
                // Load from database and populate cache
                List<User> users = userService.getAllUsers();
                userCache.loadAll(users);
                return users;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    allUsers = getValue();
                    userList.setAll(allUsers);
                    lblTotalUsers.setText("Total Users: " + allUsers.size());
                    tblUsers.setPlaceholder(new Label("No users found."));
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    tblUsers.setPlaceholder(new Label("Failed to load users"));
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + getException().getMessage());
                });
            }
        };

        new Thread(loadTask).start();
    }

    @FXML
    public void searchUsers() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            userList.setAll(allUsers);
            lblTotalUsers.setText("Total Users: " + allUsers.size());
            return;
        }

        // Use cache for fast search
        List<User> filtered = userCache.search(query);
        userList.setAll(filtered);
        lblTotalUsers.setText("Found: " + filtered.size() + " users");
    }

    @FXML
    public void clearSearch() {
        txtSearch.clear();
        userList.setAll(allUsers);
        lblTotalUsers.setText("Total Users: " + allUsers.size());
    }

    @FXML
    public void refreshUsers() {
        // Clear cache and reload from database
        userCache.clear();
        loadUsers();
    }

    @FXML
    public void showAddDialog() {
        Dialog<User> dialog = createUserDialog(null);
        Optional<User> result = dialog.showAndWait();

        result.ifPresent(user -> {
            // Check cache first for email uniqueness
            if (userCache.emailExists(user.getEmailAddress())) {
                showAlert(Alert.AlertType.ERROR, "Email Exists",
                    "A user with this email already exists. Please use a different email.");
                return;
            }

            // Show saving indicator
            lblTotalUsers.setText("Saving...");

            Task<User> createTask = new Task<>() {
                @Override
                protected User call() throws Exception {
                    return userService.createUser(user);
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        User created = getValue();
                        if (created != null) {
                            // Add to cache and lists
                            userCache.put(created);
                            allUsers.add(created);
                            userList.add(created);
                            lblTotalUsers.setText("Total Users: " + allUsers.size());
                            tblUsers.refresh();
                            showAlert(Alert.AlertType.INFORMATION, "Success", "User created successfully!");
                        } else {
                            lblTotalUsers.setText("Total Users: " + allUsers.size());
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create user.");
                        }
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        lblTotalUsers.setText("Total Users: " + allUsers.size());
                        Throwable ex = getException();
                        if (ex instanceof EmailAlreadyExistsException) {
                            showAlert(Alert.AlertType.ERROR, "Email Exists",
                                "A user with this email already exists. Please use a different email.");
                        } else if (ex instanceof IllegalArgumentException) {
                            showAlert(Alert.AlertType.ERROR, "Validation Error", ex.getMessage());
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create user: " + ex.getMessage());
                        }
                    });
                }
            };

            new Thread(createTask).start();
        });
    }


    @FXML
    public void showEditDialog() {
        User selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to edit.");
            return;
        }

        Dialog<User> dialog = createUserDialog(selected);
        Optional<User> result = dialog.showAndWait();

        result.ifPresent(user -> {
            // Check if email changed and if new email exists (using cache)
            if (!user.getEmailAddress().equalsIgnoreCase(selected.getEmailAddress())) {
                if (userCache.emailExistsForOtherUser(user.getEmailAddress(), user.getId())) {
                    showAlert(Alert.AlertType.ERROR, "Email Exists",
                        "A user with this email already exists. Please use a different email.");
                    return;
                }
            }

            // Store original for rollback
            final User originalUser = new User();
            originalUser.setId(selected.getId());
            originalUser.setEmailAddress(selected.getEmailAddress());
            originalUser.setFirstName(selected.getFirstName());
            originalUser.setLastName(selected.getLastName());
            originalUser.setPhoneNumber(selected.getPhoneNumber());

            // OPTIMISTIC UPDATE: Update cache and UI immediately
            userCache.update(user);
            int index = allUsers.indexOf(selected);
            if (index >= 0) {
                allUsers.set(index, user);
            }
            int listIndex = userList.indexOf(selected);
            if (listIndex >= 0) {
                userList.set(listIndex, user);
            }
            tblUsers.refresh();
            lblTotalUsers.setText("Saving...");

            Task<User> updateTask = new Task<>() {
                @Override
                protected User call() throws Exception {
                    return userService.updateUser(user);
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        lblTotalUsers.setText("Total Users: " + allUsers.size());
                        if (getValue() != null) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");
                        } else {
                            // Rollback on failure
                            rollbackUserUpdate(originalUser, user);
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user.");
                        }
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        lblTotalUsers.setText("Total Users: " + allUsers.size());
                        // Rollback on failure
                        rollbackUserUpdate(originalUser, user);
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user: " + getException().getMessage());
                    });
                }
            };

            new Thread(updateTask).start();
        });
    }

    private void rollbackUserUpdate(User original, User updated) {
        userCache.update(original);
        int index = allUsers.indexOf(updated);
        if (index >= 0) {
            allUsers.set(index, original);
        }
        int listIndex = userList.indexOf(updated);
        if (listIndex >= 0) {
            userList.set(listIndex, original);
        }
        tblUsers.refresh();
    }

    @FXML
    public void deleteUser() {
        User selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
            return;
        }

        // Check if user has orders
        OrderService orderService = new OrderServiceImpl();
        List<Order> allOrders = orderService.getAllOrders();
        long userOrderCount = allOrders.stream()
            .filter(o -> o.getUserId() != null && o.getUserId().equals(selected.getId()))
            .count();

        String warningMessage = "Are you sure you want to delete user '" + selected.getEmailAddress() + "'?";
        if (userOrderCount > 0) {
            warningMessage += "\n\n⚠️ WARNING: This user has " + userOrderCount + " order(s).\n" +
                "Deleting will also remove all associated orders.";
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete User");
        confirm.setContentText(warningMessage);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Store for potential rollback
            final User deletedUser = selected;
            final int deletedIndex = userList.indexOf(selected);

            // OPTIMISTIC UPDATE: Remove from cache and UI immediately
            userCache.remove(selected.getId());
            userList.remove(selected);
            allUsers.remove(selected);
            lblTotalUsers.setText("Deleting...");

            // Delete from database in background
            Task<User> deleteTask = new Task<>() {
                @Override
                protected User call() throws Exception {
                    // First delete user's orders if any
                    if (userOrderCount > 0) {
                        List<Order> userOrders = allOrders.stream()
                            .filter(o -> o.getUserId() != null && o.getUserId().equals(deletedUser.getId()))
                            .collect(Collectors.toList());
                        for (Order order : userOrders) {
                            orderService.deleteOrder(order.getId());
                        }
                    }
                    return userService.deleteUser(deletedUser.getId());
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        lblTotalUsers.setText("Total Users: " + allUsers.size());
                        if (getValue() != null) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully!");
                        } else {
                            // Rollback on failure
                            rollbackUserDelete(deletedUser, deletedIndex);
                            showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to delete user from database. The user may have related data that cannot be deleted.");
                        }
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        rollbackUserDelete(deletedUser, deletedIndex);
                        lblTotalUsers.setText("Total Users: " + allUsers.size());
                        String errorMsg = getException().getMessage();
                        if (errorMsg != null && (errorMsg.contains("foreign key") || errorMsg.contains("constraint"))) {
                            showAlert(Alert.AlertType.ERROR, "Cannot Delete",
                                "Cannot delete this user because they have related data (orders, reviews, etc.).\n\n" +
                                "Please delete the related data first, or contact an administrator.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user: " + errorMsg);
                        }
                    });
                }
            };

            new Thread(deleteTask).start();
        }
    }

    private void rollbackUserDelete(User user, int originalIndex) {
        // Restore to cache
        userCache.put(user);

        // Restore to lists
        if (originalIndex >= 0 && originalIndex <= allUsers.size()) {
            allUsers.add(originalIndex, user);
        } else {
            allUsers.add(user);
        }
        userList.setAll(allUsers);
        lblTotalUsers.setText("Total Users: " + allUsers.size());
    }

    @FXML
    public void viewUserOrders() {
        User selected = tblUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to view orders.");
            return;
        }

        // Load orders for selected user
        OrderService orderService = new OrderServiceImpl();
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> userOrders = allOrders.stream()
            .filter(o -> o.getUserId() != null && o.getUserId().equals(selected.getId()))
            .collect(Collectors.toList());

        // Create dialog to show orders
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Orders for " + selected.getFirstName() + " " + selected.getLastName());
        dialog.setHeaderText("📦 Order History for: " + selected.getEmailAddress());


        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        content.setPrefHeight(400);

        if (userOrders.isEmpty()) {
            Label emptyLabel = new Label("No orders found for this user.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            content.getChildren().add(emptyLabel);
        } else {
            Label countLabel = new Label("Total Orders: " + userOrders.size());
            countLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            content.getChildren().add(countLabel);

            // Create a table for orders
            TableView<Order> ordersTable = new TableView<>();
            ordersTable.setPrefHeight(300);

            TableColumn<Order, String> colOrderId = new TableColumn<>("Order #");
            colOrderId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                    "#" + cellData.getValue().getId().toString().substring(0, 8).toUpperCase()));
            colOrderId.setPrefWidth(100);

            TableColumn<Order, String> colDate = new TableColumn<>("Date");
            colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getOrderDate() != null ?
                        cellData.getValue().getOrderDate().toString() : "N/A"));
            colDate.setPrefWidth(100);

            TableColumn<Order, String> colTotal = new TableColumn<>("Total");
            colTotal.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                    String.format("$%.2f", cellData.getValue().getOrderTotal() != null ?
                        cellData.getValue().getOrderTotal() : 0.0)));
            colTotal.setPrefWidth(80);

            TableColumn<Order, String> colStatus = new TableColumn<>("Status");
            colStatus.setCellValueFactory(cellData -> {
                Order order = cellData.getValue();
                String status = "Pending";
                if (order.getShippingMethodId() != null) {
                    status = "Processing";
                }
                if (order.getOrderStatus() != null) {
                    status = "Completed";
                }
                return new javafx.beans.property.SimpleStringProperty(status);
            });
            colStatus.setPrefWidth(100);

            ordersTable.getColumns().addAll(colOrderId, colDate, colTotal, colStatus);
            ordersTable.setItems(FXCollections.observableArrayList(userOrders));

            content.getChildren().add(ordersTable);

            // Calculate total spent
            double totalSpent = userOrders.stream()
                .mapToDouble(o -> o.getOrderTotal() != null ? o.getOrderTotal() : 0.0)
                .sum();
            Label totalLabel = new Label(String.format("💰 Total Spent: $%.2f", totalSpent));
            totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
            content.getChildren().add(totalLabel);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private Dialog<User> createUserDialog(User existing) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add User" : "Edit User");
        dialog.setHeaderText(existing == null ? "Create a new user" : "Edit user details");

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

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email Address (required)");
        TextField txtFirstName = new TextField();
        txtFirstName.setPromptText("First Name (required)");
        TextField txtLastName = new TextField();
        txtLastName.setPromptText("Last Name (required)");
        TextField txtPhone = new TextField();
        txtPhone.setPromptText("Phone Number (optional)");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText(existing == null ? "Password (required, min 8 chars)" : "New Password (leave blank to keep)");

        // Pre-fill if editing
        if (existing != null) {
            txtEmail.setText(existing.getEmailAddress());
            txtFirstName.setText(existing.getFirstName());
            txtLastName.setText(existing.getLastName());
            txtPhone.setText(existing.getPhoneNumber());
            // Don't show existing password
        }

        grid.add(lblError, 0, 0, 2, 1);
        grid.add(new Label("Email:*"), 0, 1);
        grid.add(txtEmail, 1, 1);
        grid.add(new Label("First Name:*"), 0, 2);
        grid.add(txtFirstName, 1, 2);
        grid.add(new Label("Last Name:*"), 0, 3);
        grid.add(txtLastName, 1, 3);
        grid.add(new Label("Phone:"), 0, 4);
        grid.add(txtPhone, 1, 4);
        grid.add(new Label("Password:" + (existing == null ? "*" : "")), 0, 5);
        grid.add(txtPassword, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Get save button and add validation using Jakarta Bean Validation
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Create appropriate DTO based on create or update
            Set<String> errors;

            if (existing == null) {
                // Creating new user - use UserCreateDto
                UserCreateDto createDto = new UserCreateDto(
                    txtEmail.getText().trim(),
                    txtFirstName.getText().trim(),
                    txtLastName.getText().trim(),
                    txtPhone.getText().trim(),
                    txtPassword.getText()
                );
                errors = ValidationUtil.validate(createDto);
            } else {
                // Updating existing user - use UserUpdateDto
                UserUpdateDto updateDto = new UserUpdateDto(
                    existing.getId(),
                    txtEmail.getText().trim(),
                    txtFirstName.getText().trim(),
                    txtLastName.getText().trim(),
                    txtPhone.getText().trim(),
                    txtPassword.getText().isEmpty() ? null : txtPassword.getText()
                );
                errors = ValidationUtil.validate(updateDto);
            }

            if (!errors.isEmpty()) {
                // Show first validation error
                lblError.setText(errors.iterator().next());
                event.consume();
                return;
            }

            lblError.setText("");
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (existing == null) {
                    // Create new user from DTO (inline mapping)
                    UserCreateDto createDto = new UserCreateDto(
                        txtEmail.getText().trim(),
                        txtFirstName.getText().trim(),
                        txtLastName.getText().trim(),
                        txtPhone.getText().trim(),
                        txtPassword.getText()
                    );
                    User newUser = new User();
                    newUser.setId(java.util.UUID.randomUUID());
                    newUser.setEmailAddress(createDto.getEmailAddress());
                    newUser.setFirstName(createDto.getFirstName());
                    newUser.setLastName(createDto.getLastName());
                    newUser.setPhoneNumber(createDto.getPhoneNumber());
                    newUser.setPassword(createDto.getPassword());
                    return newUser;
                } else {
                    // Update existing user from DTO (inline mapping)
                    UserUpdateDto updateDto = new UserUpdateDto(
                        existing.getId(),
                        txtEmail.getText().trim(),
                        txtFirstName.getText().trim(),
                        txtLastName.getText().trim(),
                        txtPhone.getText().trim(),
                        txtPassword.getText().isEmpty() ? null : txtPassword.getText()
                    );
                    existing.setEmailAddress(updateDto.getEmailAddress());
                    existing.setFirstName(updateDto.getFirstName());
                    existing.setLastName(updateDto.getLastName());
                    existing.setPhoneNumber(updateDto.getPhoneNumber());
                    if (updateDto.hasPassword()) {
                        existing.setPassword(updateDto.getPassword());
                    }
                    return existing;
                }
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

