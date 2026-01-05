package com.amalitech.smartecommerce.model;

import com.amalitech.smartecommerce.utils.DBConnection;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SchemaCreator {
    private static final String SCHEMA_PATH = "src/main/java/com/amalitech/smartecommerce/model/ecommerce_schema.sql";

    public static void createTablesFromSchema() {
        try (Connection conn = DBConnection.getConnection()) {
            if (!tableExists(conn, "app_user")) {
                Statement stmt = conn.createStatement();
                String sql = new String(Files.readAllBytes(Paths.get(SCHEMA_PATH)));
                stmt.execute(sql);
                System.out.println("Database tables created successfully from schema.");
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
