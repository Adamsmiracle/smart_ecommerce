package com.amalitech.smartecommerce.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    DBConfig.getUrl(),
                    DBConfig.getUser(),
                    DBConfig.getPassword()
            );
        }
        return connection;
    }

    /**
     * Tests if the database connection can be established.
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Gets a detailed error message for connection failures.
     * @return error message describing the connection issue
     */
    public static String getConnectionErrorMessage() {
        try {
            Connection conn = DriverManager.getConnection(
                    DBConfig.getUrl(),
                    DBConfig.getUser(),
                    DBConfig.getPassword()
            );
            if (conn != null) {
                conn.close();
            }
            return null; // No error
        } catch (SQLException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("connection") || message.contains("network") ||
                message.contains("timeout") || message.contains("unreachable")) {
                return "Unable to connect to database. Please check your internet connection.";
            } else if (message.contains("authentication") || message.contains("password") ||
                       message.contains("denied")) {
                return "Database authentication failed. Please contact support.";
            } else {
                return "Connection error: " + e.getMessage();
            }
        }
    }
}
