# Input Validation Guide

## Overview

The application uses a centralized `InputValidator` utility class for validating all user inputs. This ensures consistent validation across all forms.

## Location

`com.amalitech.smartecommerce.utils.InputValidator`

## Validation Types

### 1. Email Validation
```java
// Check if email is valid
boolean isValid = InputValidator.isValidEmail("user@example.com");

// Get error message (returns null if valid)
String error = InputValidator.getEmailError(email);
if (error != null) {
    showError(error);
}
```

**Rules:**
- Required field
- Must match pattern: `user@domain.ext`
- Domain must have valid TLD (2+ characters)

### 2. Name Validation (First/Last Name)
```java
String error = InputValidator.getNameError(firstName, "First name");
if (error != null) {
    showError(error);
}
```

**Rules:**
- Required field
- Only letters, spaces, hyphens, apostrophes
- Max 50 characters
- Examples: "John", "Mary-Jane", "O'Brien"

### 3. Phone Validation
```java
String error = InputValidator.getPhoneError(phone);
if (error != null) {
    showError(error);
}
```

**Rules:**
- Optional field
- If provided, must match valid phone patterns
- Supports: +1234567890, 123-456-7890, (123) 456-7890

### 4. Password Validation
```java
String error = InputValidator.getPasswordError(password);
if (error != null) {
    showError(error);
}

// For password confirmation
String confirmError = InputValidator.getConfirmPasswordError(password, confirmPassword);
```

**Rules:**
- Required field
- Minimum 6 characters
- Confirmation must match

### 5. Product Name Validation
```java
String error = InputValidator.getProductNameError(name);
```

**Rules:**
- Required field
- 2-100 characters

### 6. Category Name Validation
```java
String error = InputValidator.getCategoryNameError(name);
```

**Rules:**
- Required field
- 2-50 characters

### 7. Price Validation
```java
String error = InputValidator.getPriceError(price);
```

**Rules:**
- Required field
- Positive number
- Up to 2 decimal places
- Examples: "99", "99.99", "0.50"

### 8. Quantity Validation
```java
String error = InputValidator.getQuantityError(quantity);
String stockError = InputValidator.getStockError(stock);
```

**Rules:**
- Required field
- Quantity: Positive integer (1 or more)
- Stock: Non-negative integer (0 or more)

## Usage in Controllers

### Login Form
```java
@FXML
public void handleLogin() {
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

    // Continue with login...
}
```

### Registration Form
```java
@FXML
public void handleRegister() {
    // Validate first name
    String firstNameError = InputValidator.getNameError(firstName, "First name");
    if (firstNameError != null) {
        showError(lblRegError, firstNameError);
        return;
    }

    // Validate last name
    String lastNameError = InputValidator.getNameError(lastName, "Last name");
    if (lastNameError != null) {
        showError(lblRegError, lastNameError);
        return;
    }

    // Validate email
    String emailError = InputValidator.getEmailError(email);
    if (emailError != null) {
        showError(lblRegError, emailError);
        return;
    }

    // Validate phone (optional)
    String phoneError = InputValidator.getPhoneError(phone);
    if (phoneError != null) {
        showError(lblRegError, phoneError);
        return;
    }

    // Validate password
    String passwordError = InputValidator.getPasswordError(password);
    if (passwordError != null) {
        showError(lblRegError, passwordError);
        return;
    }

    // Validate confirm password
    String confirmError = InputValidator.getConfirmPasswordError(password, confirmPassword);
    if (confirmError != null) {
        showError(lblRegError, confirmError);
        return;
    }

    // Continue with registration...
}
```

### Dialog Forms (Product, Category, User)
```java
// Get save button and add validation
Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
saveButton.addEventFilter(ActionEvent.ACTION, event -> {
    // Validate product name
    String nameError = InputValidator.getProductNameError(txtName.getText());
    if (nameError != null) {
        lblError.setText(nameError);
        event.consume(); // Prevent dialog from closing
        return;
    }

    // Validate category selection
    if (cmbCategory.getValue() == null) {
        lblError.setText("Please select a category.");
        event.consume();
        return;
    }

    lblError.setText(""); // Clear error if validation passes
});
```

## Error Messages

| Validation | Error Message |
|------------|---------------|
| Empty Email | "Email is required." |
| Invalid Email | "Please enter a valid email address (e.g., user@example.com)." |
| Empty Name | "[Field name] is required." |
| Invalid Name | "[Field name] must contain only letters, spaces, hyphens, or apostrophes." |
| Invalid Phone | "Please enter a valid phone number." |
| Empty Password | "Password is required." |
| Short Password | "Password must be at least 6 characters." |
| Password Mismatch | "Passwords do not match." |
| Empty Product Name | "Product name is required." |
| Invalid Product Name Length | "Product name must be between 2 and 100 characters." |
| Empty Category Name | "Category name is required." |
| Invalid Category Name Length | "Category name must be between 2 and 50 characters." |
| Empty Price | "Price is required." |
| Invalid Price | "Please enter a valid price (e.g., 99.99)." |
| Empty Quantity | "Quantity is required." |
| Invalid Quantity | "Please enter a valid quantity (positive number)." |
| Invalid Stock | "Please enter a valid stock quantity (0 or more)." |

## Forms Updated with Validation

✅ **LoginController**
- handleLogin() - Email, Password
- handleRegister() - First Name, Last Name, Email, Phone, Password, Confirm Password
- handleAdminLogin() - Email, Password

✅ **ProductController**
- createProductDialog() - Product Name, Category, Description

✅ **CategoryController**
- createCategoryDialog() - Category Name, Duplicate check

✅ **UserController**
- createUserDialog() - Email, First Name, Last Name, Phone, Password

## Best Practices

1. **Always show error label** in dialog forms for validation messages
2. **Use `event.consume()`** to prevent dialog from closing on validation failure
3. **Trim input values** before validation and saving
4. **Clear error messages** when validation passes
5. **Mark required fields** with asterisk (*) in labels
6. **Use placeholder text** with helpful hints like "(required)" or "(min 6 chars)"

