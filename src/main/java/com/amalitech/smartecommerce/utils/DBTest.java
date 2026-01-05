package com.amalitech.smartecommerce.utils;

import com.amalitech.smartecommerce.model.SchemaCreator;

import java.sql.Connection;
import java.sql.SQLException;

public class DBTest {

    public static void main(String[] args) {
        try(Connection connection = DBConnection.getConnection();) {

            if (connection != null && !connection.isClosed()) {
                System.out.println("Database connection successful!");
                SchemaCreator.createTablesFromSchema();

            } else {
                System.out.println("Database connection failed.");
            }

        } catch (SQLException e) {
            System.out.println("Error connecting to database:");
            e.printStackTrace();
        }
    }
}
