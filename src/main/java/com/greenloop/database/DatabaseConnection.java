package com.greenloop.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Properties props = new Properties();
            try (InputStream input = DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties")) {

                if (input == null) {
                    throw new SQLException("config.properties not found in resources.");
                }
                props.load(input);

            } catch (IOException e) {
                throw new SQLException("Failed to load config.properties: " + e.getMessage());
            }

            String url      = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }
}