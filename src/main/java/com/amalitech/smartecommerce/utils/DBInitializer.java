package com.amalitech.smartecommerce.utils;

import com.amalitech.smartecommerce.model.SchemaCreator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ensures the database schema is created once.
 * This helper will check for an existing table (app_user)
 * present, invoke the packaged SchemaCreator to create the schema.
 * It is safe to call multiple times; the work runs only once.
 */
public class DBInitializer {

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Ensure the DB schema exists. This method is idempotent and thread-safe.
     */
    public static void ensureSchemaCreated() {
        if (initialized.get()) return; // fast-path

        synchronized (DBInitializer.class) {
            if (initialized.get()) return;

            try (Connection conn = DBConnection.getConnection()) {
                boolean exists = false;
                try (ResultSet rs = conn.getMetaData().getTables(null, null, "app_user", null)) {
                    exists = rs.next();
                }

                if (!exists) {
                    System.out.println("DBInitializer: schema not found, creating tables from packaged schema...");
                    // Ensure SchemaCreator runs even if CREATE_SCHEMA isn't set externally
                    String prev = System.getProperty("CREATE_SCHEMA");
                    try {
                        System.setProperty("CREATE_SCHEMA", "true");
                        SchemaCreator.createTablesFromSchema();
                    } finally {
                        if (prev == null) {
                            System.clearProperty("CREATE_SCHEMA");
                        } else {
                            System.setProperty("CREATE_SCHEMA", prev);
                        }
                    }
                } else {
                    System.out.println("DBInitializer: schema already exists. Skipping creation.");
                }
            } catch (Exception e) {
                // Log and continue; calling code can decide how to handle connectivity issues
                System.err.println("DBInitializer: failed to ensure schema: " + e.getMessage());
                e.printStackTrace();
            } finally {
                initialized.set(true);
            }
        }
    }
}

