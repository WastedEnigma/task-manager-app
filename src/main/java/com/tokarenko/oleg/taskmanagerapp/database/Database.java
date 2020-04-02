package com.tokarenko.oleg.taskmanagerapp.database;

import com.tokarenko.oleg.taskmanagerapp.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database class for working with DB using Java JDBC API
*/

public final class Database {

    private Database() { }

    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    private static final String URL = "jdbc:mysql://localhost:3306/task_manager";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    private static Connection connection;

    public static void connect() {
        try {
            Class.forName("java.sql.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            LOGGER.info("Connection established");
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Error: Connection failed", e);
        }
    }

    public static void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Connection is closed");
            } catch (SQLException ignored) {
            }
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}
