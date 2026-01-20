package com.amalitech.smartecommerce.model;

import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SchemaCreator {
    // Classpath resource path for the schema SQL
    private static final String CLASSPATH_SCHEMA = "/com/amalitech/smartecommerce/model/ecommerce_schema.sql";
    private static final String SAMPLE_DATA_MARKER = "-- SAMPLE DATA";

    /**
     * Create tables from a packaged schema SQL file. This operation is guarded by
     * the CREATE_SCHEMA system property or CREATE_SCHEMA environment variable. Only
     * set these in development or controlled environments. Example:
     */
    public static void createTablesFromSchema() {
        boolean allow = Boolean.parseBoolean(System.getProperty("CREATE_SCHEMA", System.getenv().getOrDefault("CREATE_SCHEMA", "false")));
        if (!allow) {
            System.out.println("Schema creation skipped");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (!tableExists(conn, "app_user")) {
                // Read the schema SQL from the classpath resource (packaged with the app)
                try (InputStream is = SchemaCreator.class.getResourceAsStream(CLASSPATH_SCHEMA)) {
                    if (is == null) {
                        System.err.println("Schema resource not found on classpath: " + CLASSPATH_SCHEMA);
                        return;
                    }

                    String sql = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A").next();

                    // If the file contains a SAMPLE DATA marker, only execute the portion before it (schema/DDL)
                    int markerIndex = sql.indexOf(SAMPLE_DATA_MARKER);
                    String schemaSql = markerIndex >= 0 ? sql.substring(0, markerIndex) : sql;

                    // Split statements on semicolon. Statements that are empty after trim are ignored.
                    String[] statements = schemaSql.split(";\\s*(?=\\r?\\n|$)");
                    for (String stmtSql : statements) {
                        String trimmed = stmtSql.trim();
                        if (trimmed.isEmpty()) continue;
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(trimmed);
                        }
                    }

                    System.out.println("Database tables created successfully from packaged schema (DDL only).");
                }
            } else {
                System.out.println("Database tables already exist. Skipping schema creation.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean tableExists(Connection conn, String tableName) {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
