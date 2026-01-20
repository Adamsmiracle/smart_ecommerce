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

    -- relationships
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


--     relationship
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

--  relationship
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

--     relationship
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


