package com.amalitech.smartecommerce.model;

import com.amalitech.smartecommerce.utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Runs the sample data for the packaged schema.
 * This implementation seeds 10 rows per key table and is idempotent: it checks whether data exists before inserting.
 */
public class DBSeeder {

    private static final int N = 10;

    public static void seedSampleData() {
        try (Connection conn = DBConnection.getConnection()) {
            // Check whether sample data likely already present (simple heuristic: any user exists)
//            try (Statement check = conn.createStatement(); ResultSet rs = check.executeQuery("SELECT 1 FROM app_user LIMIT 1")) {
////                if (rs.next()) {
////                    System.out.println("Seeder: app_user already populated; skipping sample data.");
////                    return;
////                }
//            } catch (SQLException e) {
//                System.err.println("Seeder: could not check existing app_user: " + e.getMessage());
//                throw e;
//            }

            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                // Countries
                for (int i = 1; i <= N; i++) {
                    stmt.executeUpdate(String.format("INSERT INTO country (id, country_name) VALUES (gen_random_uuid(), 'Country %d')", i));
                }

                // Product Categories
                for (int i = 1; i <= N; i++) {
                    stmt.executeUpdate(String.format("INSERT INTO product_category (id, category_name) VALUES (gen_random_uuid(), 'Category %d')", i));
                }

                // Users (bcrypt password)
                String plainPassword = "password123";
                for (int i = 1; i <= N; i++) {
                    String email = String.format("user%d@example.com", i);
                    String first = "User" + i;
                    String last = "Seed" + i;
                    String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
                    stmt.executeUpdate(String.format("INSERT INTO app_user (id, email_address, first_name, last_name, phone_number, password) VALUES (gen_random_uuid(), '%s', '%s', '%s', '00000000%d', '%s')", email, first, last, i, hash));
                }

                // Addresses (assign country by offset)
                for (int i = 0; i < N; i++) {
                    int countryOffset = i % N;
                    stmt.executeUpdate(String.format("INSERT INTO address (id, unit_number, street_number, address_line, city, region, country_id) VALUES (gen_random_uuid(), 'U%d', '%d', 'Street %d', 'City%d', 'Region%d', (SELECT id FROM country ORDER BY id LIMIT 1 OFFSET %d))",
                            i + 1, i + 10, i + 1, i + 1, i + 1, countryOffset));
                }

                // user_address map user i -> address i
//                for (int i = 0; i < N; i++) {
//                    stmt.executeUpdate(String.format("INSERT INTO user_address (id, user_id, address_id) VALUES (gen_random_uuid(), (SELECT id FROM app_user ORDER BY id LIMIT 1 OFFSET %d), (SELECT id FROM address ORDER BY id LIMIT 1 OFFSET %d))", i, i));
//                }

                // Products and Product Items
                for (int i = 0; i < N; i++) {
                    stmt.executeUpdate(String.format("INSERT INTO product (id, category_id, name, description, product_image) VALUES (gen_random_uuid(), (SELECT id FROM product_category ORDER BY id LIMIT 1 OFFSET %d), 'Product %d', 'Description for product %d', 'product_%d.jpg')", i, i + 1, i + 1, i + 1));
                }

                for (int i = 0; i < N; i++) {
                    int qty = 10 + i * 5;
                    double price = 10.0 + i * 2.5;
                    stmt.executeUpdate(String.format("INSERT INTO product_item (id, product_id, qty_in_stock, price, image) VALUES (gen_random_uuid(), (SELECT id FROM product ORDER BY id LIMIT 1 OFFSET %d), %d, %.2f, 'product_%d.jpg')", i, qty, price, i + 1));
                }

                // Payment types
                for (int i = 1; i <= N; i++) {
                    stmt.executeUpdate(String.format("INSERT INTO payment_type (id, value) VALUES (gen_random_uuid(), 'PaymentType %d')", i));
                }

                // Use payment methods: assign one per user
                for (int i = 0; i < N; i++) {
                    stmt.executeUpdate(String.format("INSERT INTO use_payment_method (id, user_id, payment_type_id, provider_provider, account_number, expiry_date, is_default) VALUES (gen_random_uuid(), (SELECT id FROM app_user ORDER BY id LIMIT 1 OFFSET %d), (SELECT id FROM payment_type ORDER BY id LIMIT 1 OFFSET %d), 'Provider%d', 'ACC%04d', '2026-12-31', TRUE)", i, i, i + 1, i + 1000));
                }

                // Shipping methods
                for (int i = 1; i <= N; i++) {
                    stmt.executeUpdate(String.format("INSERT INTO shipping_method (id, name, price) VALUES (gen_random_uuid(), 'Shipping %d', %.2f)", i, i * 5.0));
                }

                // Order statuses
                String[] baseStatuses = new String[]{"Pending", "Completed", "Cancelled"};
                for (int i = 0; i < N; i++) {
                    String s = (i < baseStatuses.length) ? baseStatuses[i] : "Status " + (i + 1);
                    stmt.executeUpdate(String.format("INSERT INTO order_status (id, status) VALUES (gen_random_uuid(), '%s')", s));
                }

                // Customer orders - one per user
                for (int i = 0; i < N; i++) {
                    // pick user i, payment method for user, address for user, shipping method i, order status pending
                    stmt.executeUpdate(String.format("INSERT INTO customer_order (id, user_id, order_date, payment_method_id, shipping_address_id, shipping_method_id, order_total, order_status) VALUES (gen_random_uuid(), (SELECT id FROM app_user ORDER BY id LIMIT 1 OFFSET %d), CURRENT_DATE, (SELECT id FROM use_payment_method WHERE user_id = (SELECT id FROM app_user ORDER BY id LIMIT 1 OFFSET %d) LIMIT 1), (SELECT id FROM address ORDER BY id LIMIT 1 OFFSET %d), (SELECT id FROM shipping_method ORDER BY id LIMIT 1 OFFSET %d), 0.0, (SELECT id FROM order_status WHERE status = 'Pending' LIMIT 1))", i, i, i, i));
                }

                // Order lines - add one line per order referencing product_item i
                for (int i = 0; i < N; i++) {
                    int qty = (i % 3) + 1;
                    stmt.executeUpdate(String.format("INSERT INTO order_line (id, product_item_id, order_id, qty, price) VALUES (gen_random_uuid(), (SELECT id FROM product_item ORDER BY id LIMIT 1 OFFSET %d), (SELECT id FROM customer_order WHERE user_id = (SELECT id FROM app_user ORDER BY id LIMIT 1 OFFSET %d) LIMIT 1), %d, (SELECT price FROM product_item ORDER BY id LIMIT 1 OFFSET %d))", i, i, qty, i));

                    // Update the order_total for the order to reflect the line price * qty
                    stmt.executeUpdate(String.format("UPDATE customer_order SET order_total = (SELECT COALESCE(SUM(ol.qty * ol.price),0) FROM order_line ol WHERE ol.order_id = (SELECT id FROM customer_order WHERE user_id = (SELECT id FROM app_user ORDER BY id LIMIT 1 OFFSET %d) LIMIT 1)) WHERE user_id = (SELECT id FROM app_user ORDER BY id LIMIT 1 OFFSET %d)", i, i));

                    // Optionally decrement stock so inventory reflects orders
                    stmt.executeUpdate(String.format("UPDATE product_item SET qty_in_stock = GREATEST(0, qty_in_stock - %d) WHERE id = (SELECT id FROM product_item ORDER BY id LIMIT 1 OFFSET %d)", qty, i));
                }

                // User reviews - attach a review for the first few orders
                for (int i = 0; i < Math.min(N, 5); i++) {
                    stmt.executeUpdate(String.format("INSERT INTO user_review (id, user_id, ordered_product_id, rating_value, comment) VALUES (gen_random_uuid(), (SELECT id FROM app_user ORDER BY id LIMIT 1 OFFSET %d), (SELECT ol.id FROM order_line ol JOIN customer_order o ON ol.order_id = o.id WHERE o.user_id = (SELECT id FROM app_user ORDER BY id LIMIT 1 OFFSET %d) LIMIT 1), %d, 'Seed review %d')", i, i, (i % 5) + 4, i));
                }

                conn.commit();
                System.out.println("Seeder: sample data seeded successfully (" + N + " items per main table).");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Seeder: failed - rolled back: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (SQLException e) {
            System.err.println("Seeder: failed to seed sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

