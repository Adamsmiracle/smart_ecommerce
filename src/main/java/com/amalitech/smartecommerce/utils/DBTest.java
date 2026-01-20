package com.amalitech.smartecommerce.utils;

import com.amalitech.smartecommerce.model.SchemaCreator;
import com.amalitech.smartecommerce.model.DBSeeder;

import java.sql.Connection;
import java.sql.SQLException;

public class DBTest {

    public static void main(String[] args) {
        try(Connection connection = DBConnection.getConnection();) {

            if (connection != null && !connection.isClosed()) {
                System.out.println("Database connection successful!");
                // Use the new initializer which checks and runs DDL only once
                DBInitializer.ensureSchemaCreated();

                // Optionally run seeder controlled by RUN_SEEDER env var (dev only)
                boolean runSeeder = Boolean.parseBoolean(System.getProperty("RUN_SEEDER", System.getenv().getOrDefault("RUN_SEEDER", "false")));
                if (runSeeder) {
                    DBSeeder.seedSampleData();
                }

            } else {
                System.out.println("Database connection failed.");
            }

        } catch (SQLException e) {
            System.out.println("Error connecting to database:");
            e.printStackTrace();
        }
    }
}
