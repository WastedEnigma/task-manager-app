package com.tokarenko.oleg.taskmanagerapp.ui;

import com.tokarenko.oleg.taskmanagerapp.service.AuthService;
import com.tokarenko.oleg.taskmanagerapp.model.User;
import com.tokarenko.oleg.taskmanagerapp.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import static com.tokarenko.oleg.taskmanagerapp.custom_query.TaskMySqlQuery.GET_TASKS_BY_USER_ID;

/**
 * Main Console User Interface class
*/

public class ConsoleUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleUI.class);

    public void run() {
        Database.connect();

        System.out.println("Console Auth Application");
        Scanner sc = new Scanner(System.in);

        String option;

        do {
            System.out.println("(1) Login");
            System.out.println("(2) Register");
            System.out.println("(3) Exit");
            System.out.print("Choose option: ");
            option = sc.nextLine();

            switch (option.toLowerCase()) {
                case "1":
                    initLogin();
                    break;
                case "2":
                    initRegistration();
                    break;
                default:
                    if (!option.equalsIgnoreCase("3")) {
                        LOGGER.error("Invalid input");
                    }
            }
        } while (!option.equalsIgnoreCase("3"));

        Database.disconnect();
    }

    private void initRegistration() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter email: ");
        String email = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        AuthService.register(username, email, password);
    }

    private void initLogin() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        boolean loggedIn = AuthService.login(username, password);

        if (loggedIn) {
            String option;

            do {
                System.out.println("(1) My account");
                System.out.println("(2) My tasks");
                System.out.println("(3) Log out");
                System.out.print("Choose option: ");
                option = sc.nextLine();

                switch (option) {
                    case "1":
                        displayAccountInfo();
                        break;
                    case "2":
                        displayAccountTasksInfo();
                        break;
                    case "3":
                        AuthService.logout();
                        break;
                    default:
                        if (!option.equalsIgnoreCase("3")) {
                            LOGGER.error("Invalid input");
                        }
                }
            } while (!option.equalsIgnoreCase("3"));
        } else {
            LOGGER.error("Error: user is not registered");
        }
    }

    private void displayAccountInfo() {
        User user = AuthService.getCurrentUser();

        System.out.println("\n~Account Info~");
        System.out.println("-------------");
        System.out.println("Username: " + user.getName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("-------------\n");
    }

    private void displayAccountTasksInfo() {
        Connection connection = Database.getConnection();
        PreparedStatement getTasksByUserId = null;
        ResultSet tasksResultSet = null;

        User user = AuthService.getCurrentUser();

        try {
            getTasksByUserId = connection.prepareStatement(GET_TASKS_BY_USER_ID);
            getTasksByUserId.setLong(1, user.getId());
            tasksResultSet = getTasksByUserId.executeQuery();
            viewTaskList(tasksResultSet);

            new TaskConsoleUI().run();
        } catch (SQLException e) {
            LOGGER.error("Error: failed to retrieve tasks info from DB", e);
        } finally {
            if (tasksResultSet != null) {
                try {
                    tasksResultSet.close();
                } catch (SQLException ignored) {
                }
            }

            if (getTasksByUserId != null) {
                try {
                    getTasksByUserId.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private void viewTaskList(ResultSet taskResultSet) throws SQLException {
        boolean isEmpty = true;

        System.out.println("\n~Tasks~");
        System.out.println("-------------");

        while (taskResultSet.next()) {
            String content = taskResultSet.getString("content");
            long id = taskResultSet.getLong("id");
            System.out.println("- " + content + " (" + id + ")");
            isEmpty = false;
        }

        if (isEmpty) {
            System.out.println("Task set is empty.");
        }

        System.out.println("-------------\n");
    }
}
