package com.tokarenko.oleg.taskmanagerapp.service;

import com.tokarenko.oleg.taskmanagerapp.database.Database;
import com.tokarenko.oleg.taskmanagerapp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.tokarenko.oleg.taskmanagerapp.custom_query.UserMySqlQuery.*;

/**
 * Service class for user login and registration
*/

public final class AuthService {

    private AuthService() { }

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private static Connection connection = Database.getConnection();

    private static User currentUser;

    public static void register(String name, String email, String password) {
        try (PreparedStatement addNewUser = connection.prepareStatement(INSERT_NEW_USER)) {
            addNewUser.setBoolean(1, false);
            addNewUser.setString(2, email);
            addNewUser.setString(3, name);
            addNewUser.setString(4, password);
            addNewUser.execute();

            currentUser = new User();
            currentUser.setName(name);
            currentUser.setEmail(email);
            currentUser.setPassword(password);

            LOGGER.info("User " + currentUser.getEmail() + " is registered successfully");
        } catch (SQLException e) {
            LOGGER.error("Error: failed to add new user to DB", e);
        }
    }

    public static boolean login(String username, String password) {
        PreparedStatement userLogin = null;
        ResultSet userResultSet = null;

        try {
            userLogin = connection.prepareStatement(LOGIN);
            userLogin.setString(1, username);
            userLogin.setString(2, password);

            userResultSet = userLogin.executeQuery();
            setValues(userResultSet);

            if (currentUser != null) {
                userLogin = connection.prepareStatement(OPEN_SESSION);
                userLogin.setLong(1, currentUser.getId());
                userLogin.execute();
                LOGGER.info("User " + currentUser.getEmail() + " logged in successfully");
            } else {
                return false;
            }
        } catch (SQLException e) {
            LOGGER.error("Error: failed to login", e);
            return false;
        } finally {
            if (userResultSet != null) {
                try {
                    userResultSet.close();
                } catch (SQLException ignored) {
                }
            }

            if (userLogin != null) {
                try {
                    userLogin.close();
                } catch (SQLException ignored) {
                }
            }
        }

        return true;
    }

    public static void logout() {
        try (PreparedStatement preparedStatement = connection.prepareStatement(CLOSE_SESSION)) {
            preparedStatement.setLong(1, currentUser.getId());
            preparedStatement.execute();

            LOGGER.info(currentUser.getEmail() + " logged out");
            currentUser = null;
        } catch (SQLException e) {
            LOGGER.error("Error: failed to logout", e);
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    private static void setValues(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            currentUser = new User();
            currentUser.setId(resultSet.getInt("id"));
            currentUser.setName(resultSet.getString("name"));
            currentUser.setEmail(resultSet.getString("email"));
            currentUser.setActive(resultSet.getBoolean("active"));
        }
    }


}
