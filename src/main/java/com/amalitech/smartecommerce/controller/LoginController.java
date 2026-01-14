package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.constants.ValidationMessages;
import com.amalitech.smartecommerce.dto.UserCreateDto;
import com.amalitech.smartecommerce.exception.EmailAlreadyExistsException;
import com.amalitech.smartecommerce.model.User;
import com.amalitech.smartecommerce.service.UserService;
import com.amalitech.smartecommerce.service.UserServiceImpl;
import com.amalitech.smartecommerce.utils.InputValidator;
import com.amalitech.smartecommerce.utils.SessionManager;
import com.amalitech.smartecommerce.utils.UserUtils;
import com.amalitech.smartecommerce.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controller for login and registration.
 */
public class LoginController implements Initializable {

    // Login form fields
    @FXML private VBox loginForm;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    // Register form fields
    @FXML private ScrollPane registerFormScroll;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtRegEmail;
    @FXML private TextField txtPhone;
    @FXML private PasswordField txtRegPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblRegError;

    // Admin login form fields
    @FXML private VBox adminLoginForm;
    @FXML private TextField txtAdminEmail;
    @FXML private PasswordField txtAdminPassword;
    @FXML private Label lblAdminError;

    private final UserService userService = new UserServiceImpl();

    // Admin credentials (in production, this would be in database with role)
    private static final String ADMIN_EMAIL = "admin@smartecommerce.com";
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Hide error labels initially
        hideErrors();
    }

    @FXML
    public void handleLogin() {
        hideErrors();

        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        // Validate email
        String emailError = InputValidator.getEmailError(email);
        if (emailError != null) {
            showError(lblError, emailError);
            return;
        }

        // Validate password
        if (!InputValidator.isNotEmpty(password)) {
            showError(lblError, "Password is required.");
            return;
        }


        // Check for regular user login
        User user;
        try {
            user = userService.getUserByEmail(email);
        } catch (Exception e) {
            showError(lblError, "⚠️ Connection error. Please check your internet and try again.");
            return;
        }

        if (user == null) {
            showError(lblError, "No account found with this email.");
            return;
        }

        // Verify password
        if (!UserUtils.verifyPassword(password, user.getPassword())) {
            showError(lblError, "Incorrect password. Please try again.");
            return;
        }

//        // Migrate password to BCrypt if using legacy hash format
//        if (UserUtils.needsHashMigration(user.getPassword())) {
//            try {
//                String newHash = UserUtils.hashPassword(password);
//                user.setPassword(newHash);
//                userService.updateUser(user);
//                System.out.println("Password migrated to BCrypt for user: " + user.getEmailAddress());
//            } catch (Exception e) {
//                // Migration failed, but login can still proceed
//                System.err.println("Failed to migrate password: " + e.getMessage());
//            }
//        }

        // Login successful
        SessionManager.getInstance().setCurrentUser(user);
        SessionManager.getInstance().setAdmin(false);
        navigateToCustomerDashboard();
    }

    @FXML
    public void handleRegister() {
        hideErrors();

        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtRegEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String password = txtRegPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        // Create DTO for validation
        UserCreateDto createDto = new UserCreateDto(email, firstName, lastName, phone, password);

        // Validate using Jakarta Bean Validation
        Set<String> errors = ValidationUtil.validate(createDto);
        if (!errors.isEmpty()) {
            showError(lblRegError, errors.iterator().next());
            return;
        }

        // Validate confirm password (not in DTO since it's UI-only)
        if (!password.equals(confirmPassword)) {
            showError(lblRegError, ValidationMessages.PASSWORDS_NOT_MATCH);
            return;
        }

//        // Check for network/database connectivity first
//        if (!DBConnection.testConnection()) {
//            String errorMsg = DBConnection.getConnectionErrorMessage();
//            if (errorMsg != null) {
//                showError(lblRegError, errorMsg);
//            } else {
//                showError(lblRegError, "⚠️ No internet connection. Please check your network and try again.");
//            }
//            return;
//        }


        // Check if email already exists
        try {
            if (userService.getUserByEmail(email) != null) {
                showError(lblRegError, "An account with this email already exists. Please use a different email or sign in.");
                return;
            }
        } catch (Exception e) {
            showError(lblRegError, "⚠️ Connection error. Please check your internet and try again.");
            return;
        }

        try {
            // Convert DTO to User entity (inline mapping)
            User newUser = new User();
            newUser.setId(java.util.UUID.randomUUID());
            newUser.setEmailAddress(createDto.getEmailAddress());
            newUser.setFirstName(createDto.getFirstName());
            newUser.setLastName(createDto.getLastName());
            newUser.setPhoneNumber(createDto.getPhoneNumber());
            newUser.setPassword(createDto.getPassword()); // Plain text - service will hash

            User createdUser = userService.createUser(newUser);
            if (createdUser != null) {
                // Auto-login after registration
                SessionManager.getInstance().setCurrentUser(createdUser);
                SessionManager.getInstance().setAdmin(false);
                navigateToCustomerDashboard();
            } else {
                showError(lblRegError, "Failed to create account. Please try again.");
            }
        } catch (EmailAlreadyExistsException e) {
            showError(lblRegError, "An account with this email already exists. Please use a different email.");
        } catch (Exception e) {
            showError(lblRegError, "⚠️ Connection error. Please check your internet and try again.");
        }
    }

    @FXML
    public void showLoginForm() {
        loginForm.setVisible(true);
        loginForm.setManaged(true);
        registerFormScroll.setVisible(false);
        registerFormScroll.setManaged(false);
        adminLoginForm.setVisible(false);
        adminLoginForm.setManaged(false);
        hideErrors();
        clearFields();
    }

    @FXML
    public void showRegisterForm() {
        loginForm.setVisible(false);
        loginForm.setManaged(false);
        registerFormScroll.setVisible(true);
        registerFormScroll.setManaged(true);
        adminLoginForm.setVisible(false);
        adminLoginForm.setManaged(false);
        hideErrors();
        clearFields();
    }

    @FXML
    public void showAdminLoginForm() {
        loginForm.setVisible(false);
        loginForm.setManaged(false);
        registerFormScroll.setVisible(false);
        registerFormScroll.setManaged(false);
        adminLoginForm.setVisible(true);
        adminLoginForm.setManaged(true);
        hideErrors();
        clearFields();
    }

    @FXML
    public void handleAdminLogin() {
        hideErrors();

        String email = txtAdminEmail.getText().trim();
        String password = txtAdminPassword.getText();

        // Validate email
        String emailError = InputValidator.getEmailError(email);
        if (emailError != null) {
            showError(lblAdminError, emailError);
            return;
        }

        // Validate password
        if (!InputValidator.isNotEmpty(password)) {
            showError(lblAdminError, "Password is required.");
            return;
        }

        // Check for network/database connectivity first (admin needs DB access)
//        if (!DBConnection.testConnection()) {
//            String errorMsg = DBConnection.getConnectionErrorMessage();
//            if (errorMsg != null) {
//                showError(lblAdminError, errorMsg);
//            } else {
//                showError(lblAdminError, "⚠️ No internet connection. Please check your network and try again.");
//            }
//            return;
//        }

        // Verify admin credentials
        if (email.equalsIgnoreCase(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
            // Create admin user session
            User adminUser = new User();
            adminUser.setEmailAddress(ADMIN_EMAIL);
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            SessionManager.getInstance().setCurrentUser(adminUser);
            SessionManager.getInstance().setAdmin(true);
            navigateToAdminDashboard();
        } else {
            showError(lblAdminError, "Invalid admin credentials. Please try again.");
        }
    }

    private void navigateToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/admin-dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/com/amalitech/smartecommerce/styles.css").toExternalForm());

            // Get stage from whichever form is visible
            Stage stage;
            if (adminLoginForm.isVisible()) {
                stage = (Stage) txtAdminEmail.getScene().getWindow();
            } else {
                stage = (Stage) txtEmail.getScene().getWindow();
            }
            stage.setTitle("Smart E-Commerce - Admin Dashboard");
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
        } catch (IOException e) {
            if (adminLoginForm.isVisible()) {
                showError(lblAdminError, "Failed to load admin dashboard: " + e.getMessage());
            } else {
                showError(lblError, "Failed to load admin dashboard: " + e.getMessage());
            }
        }
    }

    private void navigateToCustomerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/customer-dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/com/amalitech/smartecommerce/styles.css").toExternalForm());

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setTitle("Smart E-Commerce - Welcome " + SessionManager.getInstance().getCurrentUser().getFirstName());
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
        } catch (IOException e) {
            showError(lblError, "Failed to load customer dashboard: " + e.getMessage());
        }
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideErrors() {
        lblError.setVisible(false);
        lblError.setManaged(false);
        lblRegError.setVisible(false);
        lblRegError.setManaged(false);
        lblAdminError.setVisible(false);
        lblAdminError.setManaged(false);
    }

    private void clearFields() {
        txtEmail.clear();
        txtPassword.clear();
        txtFirstName.clear();
        txtLastName.clear();
        txtRegEmail.clear();
        txtPhone.clear();
        txtRegPassword.clear();
        txtConfirmPassword.clear();
        txtAdminEmail.clear();
        txtAdminPassword.clear();
    }
}

