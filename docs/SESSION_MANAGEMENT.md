# Session Management Guide

## Overview

Your application uses a **Singleton SessionManager** to track the logged-in user's state across the application.

## SessionManager Class

Location: `com.amalitech.smartecommerce.utils.SessionManager`

```java
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private boolean isAdmin;
    
    // Singleton pattern - only one instance exists
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
}
```

## How to Use

### 1. Get the SessionManager Instance

```java
SessionManager session = SessionManager.getInstance();
```

### 2. Login a User

```java
// After successful authentication
User user = userService.getUserByEmail(email);
if (UserUtils.verifyPassword(password, user.getPassword())) {
    // Set the current user in session
    SessionManager.getInstance().setCurrentUser(user);
    SessionManager.getInstance().setAdmin(false); // or true for admin
}
```

### 3. Check if User is Logged In

```java
if (SessionManager.getInstance().isLoggedIn()) {
    // User is logged in
    User user = SessionManager.getInstance().getCurrentUser();
    System.out.println("Welcome, " + user.getFirstName());
} else {
    // User is not logged in - redirect to login page
}
```

### 4. Get Current User Information

```java
User currentUser = SessionManager.getInstance().getCurrentUser();

// Get user details
String firstName = currentUser.getFirstName();
String email = currentUser.getEmailAddress();
UUID userId = currentUser.getId();

// Get display name
String displayName = SessionManager.getInstance().getUserDisplayName();
```

### 5. Check if User is Admin

```java
if (SessionManager.getInstance().isAdmin()) {
    // Show admin dashboard
    navigateToAdminDashboard();
} else {
    // Show customer dashboard
    navigateToCustomerDashboard();
}
```

### 6. Logout User

```java
SessionManager.getInstance().logout();
// This clears currentUser and sets isAdmin to false
```

## Usage Examples in Your Application

### LoginController.java - Customer Login

```java
@FXML
public void handleLogin() {
    String email = txtEmail.getText().trim();
    String password = txtPassword.getText();
    
    User user = userService.getUserByEmail(email);
    if (user != null && UserUtils.verifyPassword(password, user.getPassword())) {
        // Set session
        SessionManager.getInstance().setCurrentUser(user);
        SessionManager.getInstance().setAdmin(false);
        navigateToCustomerDashboard();
    }
}
```

### LoginController.java - Admin Login

```java
@FXML
public void handleAdminLogin() {
    String email = txtAdminEmail.getText().trim();
    String password = txtAdminPassword.getText();
    
    if (email.equalsIgnoreCase(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
        User adminUser = new User();
        adminUser.setEmailAddress(ADMIN_EMAIL);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        
        SessionManager.getInstance().setCurrentUser(adminUser);
        SessionManager.getInstance().setAdmin(true);
        navigateToAdminDashboard();
    }
}
```

### CustomerDashboardController.java - Check Session on Load

```java
@Override
public void initialize(URL url, ResourceBundle resourceBundle) {
    if (SessionManager.getInstance().isLoggedIn()) {
        String firstName = SessionManager.getInstance().getCurrentUser().getFirstName();
        lblUserName.setText(firstName);
        lblWelcome.setText("Welcome back, " + firstName + "!");
    }
}
```

### Logout Handler

```java
@FXML
public void handleLogout() {
    SessionManager.getInstance().logout();
    
    // Navigate back to login page
    Stage stage = (Stage) someButton.getScene().getWindow();
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amalitech/smartecommerce/login-view.fxml"));
    Scene scene = new Scene(loader.load());
    stage.setScene(scene);
}
```

## Session State Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     SESSION LIFECYCLE                           │
└─────────────────────────────────────────────────────────────────┘

App Start
    │
    ▼
SessionManager.getInstance()
    │ currentUser = null
    │ isAdmin = false
    │
    ▼
User Login Attempt
    │
    ├─► Success ──► setCurrentUser(user)
    │               setAdmin(true/false)
    │               │
    │               ▼
    │           Session Active
    │               │ isLoggedIn() = true
    │               │ getCurrentUser() != null
    │               │
    │               ├─► Access Dashboard
    │               ├─► Access Profile
    │               ├─► Place Orders
    │               │
    │               ▼
    │           User Clicks Logout
    │               │
    │               ▼
    │           logout()
    │               │ currentUser = null
    │               │ isAdmin = false
    │               │
    │               ▼
    │           Back to Login Page
    │
    └─► Failure ──► Show Error Message
                    Stay on Login Page
```

## Available Methods

| Method | Description | Return Type |
|--------|-------------|-------------|
| `getInstance()` | Get the singleton instance | `SessionManager` |
| `getCurrentUser()` | Get the logged-in user | `User` |
| `setCurrentUser(User)` | Set the current user | `void` |
| `isAdmin()` | Check if user is admin | `boolean` |
| `setAdmin(boolean)` | Set admin status | `void` |
| `isLoggedIn()` | Check if user is logged in | `boolean` |
| `logout()` | Clear session (logout user) | `void` |
| `getUserDisplayName()` | Get "FirstName LastName" | `String` |

## Best Practices

### 1. Always Check Session Before Accessing Protected Views

```java
@Override
public void initialize(URL url, ResourceBundle resourceBundle) {
    if (!SessionManager.getInstance().isLoggedIn()) {
        // Redirect to login
        redirectToLogin();
        return;
    }
    // Continue with initialization
}
```

### 2. Update Session After Profile Changes

```java
// After user updates their profile
User updatedUser = userService.updateUser(user);
if (updatedUser != null) {
    // Update session with new user data
    SessionManager.getInstance().setCurrentUser(updatedUser);
}
```

### 3. Clear Caches on Logout

```java
public void handleLogout() {
    // Clear session
    SessionManager.getInstance().logout();
    
    // Clear caches (optional - prevents data leakage)
    ProductCache.getInstance().clear();
    CategoryCache.getInstance().clear();
    OrderCache.getInstance().clear();
    UserCache.getInstance().clear();
    
    // Navigate to login
    navigateToLogin();
}
```

### 4. Use Session for Access Control

```java
// In a view that requires admin access
if (!SessionManager.getInstance().isAdmin()) {
    showAlert(Alert.AlertType.ERROR, "Access Denied", "Admin access required.");
    navigateToCustomerDashboard();
    return;
}
```

## Thread Safety

The `getInstance()` method is `synchronized`, making it thread-safe for the singleton pattern. However, if you access session from multiple threads, consider additional synchronization for reading/writing user data.

## Session vs Caches

| Component | Purpose | Persistence |
|-----------|---------|-------------|
| **SessionManager** | Track logged-in user state | Memory only (lost on app close) |
| **ProductCache** | Cache products for fast access | Memory only |
| **UserCache** | Cache users for fast lookup | Memory only |
| **OrderCache** | Cache orders for fast access | Memory only |

The session is **not persisted** - when the application closes, the user will need to log in again.

