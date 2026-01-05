package com.amalitech.smartecommerce.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class DBConfig {

    private static final Dotenv dotenv = Dotenv.load();

    public static String getUrl() {
        return dotenv.get("JDBC_URL");
    }

    public static String getUser() {
        return dotenv.get("DB_USER");
    }

    public static String getPassword() {
        return dotenv.get("DB_PASSWORD");
    }
}

