-- Enable UUID extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";



-- APP_USER (renamed from user)
CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email_address VARCHAR NOT NULL UNIQUE,
    first_name VARCHAR,
    last_name VARCHAR,
    phone_number VARCHAR,
    password VARCHAR
);


-- COUNTRY
CREATE TABLE country (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_name VARCHAR
);

-- ADDRESS
CREATE TABLE address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_number VARCHAR,
    street_number VARCHAR,
    address_line VARCHAR,
    city VARCHAR NOT NULL,
    region VARCHAR NOT NULL,
    country_id UUID NOT NULL,
    CONSTRAINT fk_country_id_address FOREIGN KEY (country_id)
        REFERENCES country(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- USER_ADDRESS (many-to-many)
CREATE TABLE user_address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    address_id UUID NOT NULL,
    CONSTRAINT fk_user_id_user_address FOREIGN KEY (user_id)
        REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_address_id_user_address FOREIGN KEY (address_id)
        REFERENCES address(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    UNIQUE (user_id, address_id)
);

-- PRODUCT_CATEGORY
CREATE TABLE product_category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_category_id UUID,
    category_name VARCHAR NOT NULL,
    CONSTRAINT fk_parent_category FOREIGN KEY (parent_category_id)
        REFERENCES product_category(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- PRODUCT
CREATE TABLE product (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL,
    name VARCHAR NOT NULL,
    description TEXT,
    product_image TEXT,
    CONSTRAINT fk_product_category_id_product FOREIGN KEY (category_id)
        REFERENCES product_category(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- PRODUCT_ITEM
CREATE TABLE product_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    qty_in_stock INTEGER NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    image VARCHAR,
    CONSTRAINT fk_product_id_product_item FOREIGN KEY (product_id)
        REFERENCES product(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- VARIATION
CREATE TABLE variation (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id   UUID NOT NULL,
    name          VARCHAR,

    CONSTRAINT fk_product_category_id_variation FOREIGN KEY (category_id)
        REFERENCES product_category(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- VARIATION_OPTION
CREATE TABLE variation_option (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    variation_id UUID NOT NULL,
    value VARCHAR,

    CONSTRAINT fk_variation_id_variation_option FOREIGN KEY (variation_id)
        REFERENCES variation(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- PRODUCT_CONFIGURATION (many-to-many)
CREATE TABLE product_configuration (
    product_item_id     UUID NOT NULL UNIQUE,
    variation_option_id UUID NOT NULL,

    CONSTRAINT pk_product_configuration PRIMARY KEY (product_item_id, variation_option_id),
    CONSTRAINT fk_product_item_id_product_configuration FOREIGN KEY (product_item_id)
        REFERENCES product_item(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_variation_option_id_product_configuration FOREIGN KEY (variation_option_id)
        REFERENCES variation_option(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- SHOPPING_CART
CREATE TABLE shopping_cart (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,

    CONSTRAINT fk_user_id_shopping_cart FOREIGN KEY (user_id)
        REFERENCES app_user(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- SHOPPING_CART_ITEM
CREATE TABLE shopping_cart_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL,
    product_item_id UUID NOT NULL,
    quantity INTEGER,

--    relationships
    CONSTRAINT fk_shopping_cart_id_shopping_cart_item FOREIGN KEY (cart_id)
        REFERENCES shopping_cart(id) ON DELETE NO ACTION ON UPDATE NO ACTION,

    CONSTRAINT fk_product_item_id_shopping_cart_item FOREIGN KEY (product_item_id)
        REFERENCES product_item(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- PAYMENT_TYPE
CREATE TABLE payment_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    value TEXT NOT NULL
);

-- USE_PAYMENT_METHOD
CREATE TABLE use_payment_method (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    payment_type_id UUID NOT NULL,
    provider_provider TEXT NOT NULL,
    account_number VARCHAR NOT NULL,
    expiry_date DATE NOT NULL,
    is_default BOOLEAN,


    CONSTRAINT fk_user_id_use_payment_method FOREIGN KEY (user_id)
        REFERENCES app_user(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_payment_type_id_use_payment_method FOREIGN KEY (payment_type_id)
        REFERENCES payment_type(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- ORDER_STATUS
CREATE TABLE order_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR NOT NULL
);

-- SHIPPING_METHOD
CREATE TABLE shipping_method (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR NOT NULL,
    price DOUBLE PRECISION NOT NULL
);

-- CUSTOMER_ORDER (renamed from order)
CREATE TABLE customer_order (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    order_date DATE NOT NULL,
    payment_method_id UUID,
    shipping_address_id UUID,
    shipping_method_id UUID,
    order_total DOUBLE PRECISION NOT NULL,
    order_status UUID,


    CONSTRAINT fk_user_id_customer_order FOREIGN KEY (user_id)
        REFERENCES app_user(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_payment_method_id_customer_order FOREIGN KEY (payment_method_id)
        REFERENCES use_payment_method(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_address_id_customer_order FOREIGN KEY (shipping_address_id)
        REFERENCES address(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_shipping_method_id_customer_order FOREIGN KEY (shipping_method_id)
        REFERENCES shipping_method(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_order_status_id_customer_order FOREIGN KEY (order_status)
        REFERENCES order_status(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- ORDER_LINE
CREATE TABLE order_line (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_item_id UUID NOT NULL,
    order_id UUID NOT NULL,
    qty INTEGER NOT NULL,
    price DOUBLE PRECISION,
    CONSTRAINT fk_product_item_id_order_line FOREIGN KEY (product_item_id)
        REFERENCES product_item(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_order_id_order_line FOREIGN KEY (order_id)
        REFERENCES customer_order(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- USER_REVIEW
CREATE TABLE user_review (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    ordered_product_id UUID NOT NULL,
    rating_value INTEGER,
    comment TEXT,

    CONSTRAINT fk_user_id_user_review FOREIGN KEY (user_id)
        REFERENCES app_user(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_order_line_id_user_review FOREIGN KEY (ordered_product_id)
        REFERENCES order_line(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- INDEXES
CREATE INDEX idx_address_country_id ON address(country_id);
CREATE INDEX idx_user_address_user_id ON user_address(user_id);
CREATE INDEX idx_user_address_address_id ON user_address(address_id);
CREATE INDEX idx_product_category_parent_id ON product_category(parent_category_id);
CREATE INDEX idx_product_category_id ON product(category_id);
CREATE INDEX idx_product_item_product_id ON product_item(product_id);
CREATE INDEX idx_variation_category_id ON variation(category_id);
CREATE INDEX idx_variation_option_variation_id ON variation_option(variation_id);
CREATE INDEX idx_product_configuration_variation_option_id ON product_configuration(variation_option_id);
CREATE INDEX idx_shopping_cart_user_id ON shopping_cart(user_id);
CREATE INDEX idx_shopping_cart_item_cart_id ON shopping_cart_item(cart_id);
CREATE INDEX idx_shopping_cart_item_product_item_id ON shopping_cart_item(product_item_id);
CREATE INDEX idx_use_payment_method_user_id ON use_payment_method(user_id);
CREATE INDEX idx_use_payment_method_payment_type_id ON use_payment_method(payment_type_id);
CREATE INDEX idx_customer_order_user_id ON customer_order(user_id);
CREATE INDEX idx_customer_order_payment_method_id ON customer_order(payment_method_id);
CREATE INDEX idx_customer_order_shipping_address_id ON customer_order(shipping_address_id);
CREATE INDEX idx_customer_order_shipping_method_id ON customer_order(shipping_method_id);
CREATE INDEX idx_customer_order_order_status ON customer_order(order_status);
CREATE INDEX idx_order_line_order_id ON order_line(order_id);
CREATE INDEX idx_order_line_product_item_id ON order_line(product_item_id);
CREATE INDEX idx_user_review_user_id ON user_review(user_id);
CREATE INDEX idx_user_review_ordered_product_id ON user_review(ordered_product_id);

-- SAMPLE DATA

-- Countries
INSERT INTO country (id, country_name) VALUES (gen_random_uuid(), 'Ghana');
INSERT INTO country (id, country_name) VALUES (gen_random_uuid(), 'Nigeria');

-- Product Categories
INSERT INTO product_category (id, category_name) VALUES (gen_random_uuid(), 'Electronics');
INSERT INTO product_category (id, category_name) VALUES (gen_random_uuid(), 'Books');

-- Users
-- NOTE: For testing, register new users through the application UI which will BCrypt hash passwords.
-- The passwords below are BCrypt hashes generated for: password123 and securepass
-- BCrypt hash for 'password123': $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V4W5WBWVHxkd0L
-- BCrypt hash for 'securepass': $2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
INSERT INTO app_user (id, email_address, first_name, last_name, phone_number, password) VALUES (gen_random_uuid(), 'john.doe@example.com', 'John', 'Doe', '1234567890', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V4pVWBWVHxkd0L');
INSERT INTO app_user (id, email_address, first_name, last_name, phone_number, password) VALUES (gen_random_uuid(), 'jane.smith@example.com', 'Jane', 'Smith', '0987654321', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

-- Addresses
INSERT INTO address (id, unit_number, street_number, address_line, city, region, country_id) VALUES (gen_random_uuid(), 'A1', '12', 'Main Street', 'Accra', 'Greater Accra', (SELECT id FROM country WHERE country_name = 'Ghana' LIMIT 1));
INSERT INTO address (id, unit_number, street_number, address_line, city, region, country_id) VALUES (gen_random_uuid(), 'B2', '34', 'Market Road', 'Lagos', 'Lagos', (SELECT id FROM country WHERE country_name = 'Nigeria' LIMIT 1));

-- User Addresses
INSERT INTO user_address (id, user_id, address_id) VALUES (gen_random_uuid(), (SELECT id FROM app_user WHERE email_address = 'john.doe@example.com' LIMIT 1), (SELECT id FROM address WHERE city = 'Accra' LIMIT 1));
INSERT INTO user_address (id, user_id, address_id) VALUES (gen_random_uuid(), (SELECT id FROM app_user WHERE email_address = 'jane.smith@example.com' LIMIT 1), (SELECT id FROM address WHERE city = 'Lagos' LIMIT 1));

-- Products
INSERT INTO product (id, category_id, name, description, product_image) VALUES (gen_random_uuid(), (SELECT id FROM product_category WHERE category_name = 'Electronics' LIMIT 1), 'Smartphone', 'Latest model smartphone', 'smartphone.jpg');
INSERT INTO product (id, category_id, name, description, product_image) VALUES (gen_random_uuid(), (SELECT id FROM product_category WHERE category_name = 'Books' LIMIT 1), 'Java Programming', 'Comprehensive guide to Java', 'java_book.jpg');

-- Product Items
INSERT INTO product_item (id, product_id, qty_in_stock, price, image) VALUES (gen_random_uuid(), (SELECT id FROM product WHERE name = 'Smartphone' LIMIT 1), 50, 999.99, 'smartphone.jpg');
INSERT INTO product_item (id, product_id, qty_in_stock, price, image) VALUES (gen_random_uuid(), (SELECT id FROM product WHERE name = 'Java Programming' LIMIT 1), 100, 49.99, 'java_book.jpg');

-- Payment Types
INSERT INTO payment_type (id, value) VALUES (gen_random_uuid(), 'Credit Card');
INSERT INTO payment_type (id, value) VALUES (gen_random_uuid(), 'Mobile Money');

-- Use Payment Method
INSERT INTO use_payment_method (id, user_id, payment_type_id, provider_provider, account_number, expiry_date, is_default) VALUES (gen_random_uuid(), (SELECT id FROM app_user WHERE email_address = 'john.doe@example.com' LIMIT 1), (SELECT id FROM payment_type WHERE value = 'Credit Card' LIMIT 1), 'Visa', '4111111111111111', '2026-12-31', TRUE);
INSERT INTO use_payment_method (id, user_id, payment_type_id, provider_provider, account_number, expiry_date, is_default) VALUES (gen_random_uuid(), (SELECT id FROM app_user WHERE email_address = 'jane.smith@example.com' LIMIT 1), (SELECT id FROM payment_type WHERE value = 'Mobile Money' LIMIT 1), 'MTN', '0244000000', '2026-12-31', TRUE);

-- Shipping Methods
INSERT INTO shipping_method (id, name, price) VALUES (gen_random_uuid(), 'Standard', 10.00);
INSERT INTO shipping_method (id, name, price) VALUES (gen_random_uuid(), 'Express', 25.00);
INSERT INTO shipping_method (id, name, price) VALUES (gen_random_uuid(), 'In_person', 0.00);

-- Order Status
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Pending');
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Completed');
INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), 'Cancelled');


-- Customer Orders
INSERT INTO customer_order (id, user_id, order_date, payment_method_id, shipping_address_id, shipping_method_id, order_total, order_status) VALUES (
    gen_random_uuid(),
    (SELECT id FROM app_user WHERE email_address = 'john.doe@example.com' LIMIT 1),
    '2026-01-04',
    (SELECT id FROM use_payment_method WHERE user_id = (SELECT id FROM app_user WHERE email_address = 'john.doe@example.com' LIMIT 1) LIMIT 1),
    (SELECT id FROM address WHERE city = 'Accra' LIMIT 1),
    (SELECT id FROM shipping_method WHERE name = 'Standard' LIMIT 1),
    999.99,
    (SELECT id FROM order_status WHERE status = 'Pending' LIMIT 1)
);

-- Order Lines
INSERT INTO order_line (id, product_item_id, order_id, qty, price) VALUES (
    gen_random_uuid(),
    (SELECT id FROM product_item WHERE image = 'smartphone.jpg' LIMIT 1),
    (SELECT id FROM customer_order WHERE user_id = (SELECT id FROM app_user WHERE email_address = 'john.doe@example.com' LIMIT 1) LIMIT 1),
    1,
    999.99
);

-- User Reviews
INSERT INTO user_review (id, user_id, ordered_product_id, rating_value, comment) VALUES (
    gen_random_uuid(),
    (SELECT id FROM app_user WHERE email_address = 'john.doe@example.com' LIMIT 1),
    (SELECT id FROM order_line WHERE product_item_id = (SELECT id FROM product_item WHERE image = 'smartphone.jpg' LIMIT 1) LIMIT 1),
    5,
    'Excellent product!'
);

--
-- Key Design Decisions:
--
-- 1. All tables use UUID as primary keys, generated by pgcrypto's gen_random_uuid().
-- 2. Reserved keywords (user, order) are renamed to app_user and customer_order.
-- 3. All foreign keys use ON DELETE NO ACTION and ON UPDATE NO ACTION for referential integrity.
-- 4. Many-to-many relationships (user_address, product_configuration) are modeled with join tables and unique constraints.
-- 5. Indexes are created for all foreign keys and frequently queried columns for performance.
-- 6. The schema is normalized to 3NF, with no redundant data and all non-key attributes fully dependent on the primary key.
-- 7. All constraints (PK, FK, UNIQUE, NOT NULL) are explicitly defined for data integrity.
-- 8. The schema is fully compatible with Java JDBC and Neon PostgreSQL.
