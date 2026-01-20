package com.amalitech.smartecommerce.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Always return a fresh connection. Managing a single static Connection leads to
    // reuse of closed connections and issues when callers close/reset it. If you
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DBConfig.getUrl(),
                DBConfig.getUser(),
                DBConfig.getPassword()
        );
    }
}
