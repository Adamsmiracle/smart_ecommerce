package com.amalitech.smartecommerce.constants;

/**
 * Centralized validation messages for Jakarta Bean Validation.
 * Use these constants in DTO validation annotations to avoid magic strings.
 */
public final class ValidationMessages {

    private ValidationMessages() {
    }

    // ==================== User Validation Messages ====================
    // ID
    public static final String USER_ID_REQUIRED = "User ID is required for update";

    // Email
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID = "Email must be a valid email address";

    // First Name
    public static final String FIRST_NAME_REQUIRED = "First name is required";
    public static final String FIRST_NAME_SIZE = "First name must be between 1 and 100 characters";

    // Last Name
    public static final String LAST_NAME_REQUIRED = "Last name is required";
    public static final String LAST_NAME_SIZE = "Last name must be between 1 and 100 characters";

    // Phone
    public static final String PHONE_SIZE = "Phone number must be at most 20 characters";

    // Password
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String PASSWORD_SIZE = "Password must be at least 8 characters";
    public static final String PASSWORDS_NOT_MATCH = "Passwords do not match";



    //  Product Validation Messages
    public static final String PRODUCT_ID_REQUIRED = "Product ID is required";
    public static final String PRODUCT_NAME_REQUIRED = "Product name is required";
    public static final String PRODUCT_NAME_SIZE = "Product name must be between 1 and 255 characters";
    public static final String PRODUCT_DESCRIPTION_SIZE = "Product description must be at most 2000 characters";
    public static final String PRODUCT_PRICE_POSITIVE = "Price must be a positive number";
    public static final String PRODUCT_STOCK_POSITIVE = "Stock quantity must be zero or positive";
    public static final String CATEGORY_ID_REQUIRED = "Category is required";


    //  Order Validation Messages

    public static final String ORDER_ID_REQUIRED = "Order ID is required";
    public static final String ORDER_USER_REQUIRED = "User is required for order";
    public static final String ORDER_TOTAL_POSITIVE = "Order total must be a positive number";
    public static final String SHIPPING_METHOD_REQUIRED = "Shipping method is required";


    //  Category Validation Messages

    public static final String CATEGORY_NAME_REQUIRED = "Category name is required";
    public static final String CATEGORY_NAME_SIZE = "Category name must be between 1 and 100 characters";


    //  General Validation Messages

    public static final String FIELD_REQUIRED = "This field is required";
    public static final String INVALID_FORMAT = "Invalid format";
}

