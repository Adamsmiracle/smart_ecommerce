-- =============================================================================
-- FIX SCRIPT: Create product_item records for products that don't have them
-- Run this script to fix existing products without product_item records
-- =============================================================================

-- Show products without product_item records
SELECT p.id, p.name, 'Missing product_item' as issue
FROM product p
LEFT JOIN product_item pi ON p.id = pi.product_id
WHERE pi.id IS NULL;

-- Create product_item records for products that don't have them
-- This uses a default price of $10.00 and stock of 100
INSERT INTO product_item (id, product_id, qty_in_stock, price, image)
SELECT
    gen_random_uuid(),
    p.id,
    100,  -- Default stock quantity
    10.00,  -- Default price
    NULL  -- No image
FROM product p
LEFT JOIN product_item pi ON p.id = pi.product_id
WHERE pi.id IS NULL;

-- Verify all products now have product_item records
SELECT
    p.name AS product_name,
    pi.id AS product_item_id,
    pi.qty_in_stock,
    pi.price
FROM product p
LEFT JOIN product_item pi ON p.id = pi.product_id
ORDER BY p.name;

-- Show count
SELECT
    (SELECT COUNT(*) FROM product) AS total_products,
    (SELECT COUNT(*) FROM product_item) AS total_product_items;

