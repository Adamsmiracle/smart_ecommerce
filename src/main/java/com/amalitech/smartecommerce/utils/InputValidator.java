package com.amalitech.smartecommerce.utils;

import java.util.regex.Pattern;

/**
 * Utility class for validating user inputs across the application.
 */
public class InputValidator {

    // Email pattern - standard email format
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Phone pattern - allows various formats: +1234567890, 123-456-7890, (123) 456-7890, etc.
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]{6,15}$"
    );

    // Name pattern - letters, spaces, hyphens, apostrophes (for names like O'Brien, Mary-Jane)
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[A-Za-z][A-Za-z\\s'-]{0,49}$"
    );

    // Price pattern - positive numbers with up to 2 decimal places
    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "^\\d+(\\.\\d{1,2})?$"
    );

    // Quantity pattern - positive integers
    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
        "^[1-9]\\d*$"
    );

    // ================== Validation Methods ==================

    /**
     * Validates if a string is not null and not empty after trimming.
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates email format.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates phone number format.
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is often optional
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates name format (first name, last name).
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validates password strength.
     * Requirements: at least 6 characters
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validates password strength with more requirements.
     * Requirements: at least 8 characters, 1 uppercase, 1 lowercase, 1 digit
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }

    /**
     * Validates if passwords match.
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Validates price format.
     */
    public static boolean isValidPrice(String price) {
        if (price == null || price.trim().isEmpty()) {
            return false;
        }
        if (!PRICE_PATTERN.matcher(price.trim()).matches()) {
            return false;
        }
        try {
            double value = Double.parseDouble(price.trim());
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates quantity format.
     */
    public static boolean isValidQuantity(String quantity) {
        if (quantity == null || quantity.trim().isEmpty()) {
            return false;
        }
        if (!QUANTITY_PATTERN.matcher(quantity.trim()).matches()) {
            return false;
        }
        try {
            int value = Integer.parseInt(quantity.trim());
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates stock quantity (can be 0).
     */
    public static boolean isValidStockQuantity(String quantity) {
        if (quantity == null || quantity.trim().isEmpty()) {
            return false;
        }
        try {
            int value = Integer.parseInt(quantity.trim());
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates text length.
     */
    public static boolean isValidLength(String text, int minLength, int maxLength) {
        if (text == null) {
            return minLength == 0;
        }
        int length = text.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validates product name.
     */
    public static boolean isValidProductName(String name) {
        return isValidLength(name, 2, 100);
    }

    /**
     * Validates description.
     */
    public static boolean isValidDescription(String description) {
        return description == null || description.trim().length() <= 1000;
    }

    /**
     * Validates category name.
     */
    public static boolean isValidCategoryName(String name) {
        return isValidLength(name, 2, 50);
    }

    // ================== Error Message Methods ==================

    /**
     * Gets validation error message for email.
     */
    public static String getEmailError(String email) {
        if (!isNotEmpty(email)) {
            return "Email is required.";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address (e.g., user@example.com).";
        }
        return null;
    }

    /**
     * Gets validation error message for phone.
     */
    public static String getPhoneError(String phone) {
        if (isNotEmpty(phone) && !isValidPhone(phone)) {
            return "Please enter a valid phone number.";
        }
        return null;
    }

    /**
     * Gets validation error message for name.
     */
    public static String getNameError(String name, String fieldName) {
        if (!isNotEmpty(name)) {
            return fieldName + " is required.";
        }
        if (!isValidName(name)) {
            return fieldName + " must contain only letters, spaces, hyphens, or apostrophes.";
        }
        return null;
    }

    /**
     * Gets validation error message for password.
     */
    public static String getPasswordError(String password) {
        if (!isNotEmpty(password)) {
            return "Password is required.";
        }
        if (!isValidPassword(password)) {
            return "Password must be at least 6 characters.";
        }
        return null;
    }

    /**
     * Gets validation error message for password confirmation.
     */
    public static String getConfirmPasswordError(String password, String confirmPassword) {
        if (!isNotEmpty(confirmPassword)) {
            return "Please confirm your password.";
        }
        if (!passwordsMatch(password, confirmPassword)) {
            return "Passwords do not match.";
        }
        return null;
    }

    /**
     * Gets validation error message for price.
     */
    public static String getPriceError(String price) {
        if (!isNotEmpty(price)) {
            return "Price is required.";
        }
        if (!isValidPrice(price)) {
            return "Please enter a valid price (e.g., 99.99).";
        }
        return null;
    }

    /**
     * Gets validation error message for quantity.
     */
    public static String getQuantityError(String quantity) {
        if (!isNotEmpty(quantity)) {
            return "Quantity is required.";
        }
        if (!isValidQuantity(quantity)) {
            return "Please enter a valid quantity (positive number).";
        }
        return null;
    }

    /**
     * Gets validation error message for stock quantity.
     */
    public static String getStockError(String stock) {
        if (!isNotEmpty(stock)) {
            return "Stock quantity is required.";
        }
        if (!isValidStockQuantity(stock)) {
            return "Please enter a valid stock quantity (0 or more).";
        }
        return null;
    }

    /**
     * Gets validation error message for product name.
     */
    public static String getProductNameError(String name) {
        if (!isNotEmpty(name)) {
            return "Product name is required.";
        }
        if (!isValidProductName(name)) {
            return "Product name must be between 2 and 100 characters.";
        }
        return null;
    }

    /**
     * Gets validation error message for category name.
     */
    public static String getCategoryNameError(String name) {
        if (!isNotEmpty(name)) {
            return "Category name is required.";
        }
        if (!isValidCategoryName(name)) {
            return "Category name must be between 2 and 50 characters.";
        }
        return null;
    }

    /**
     * Gets validation error message for required field.
     */
    public static String getRequiredFieldError(String value, String fieldName) {
        if (!isNotEmpty(value)) {
            return fieldName + " is required.";
        }
        return null;
    }
}

