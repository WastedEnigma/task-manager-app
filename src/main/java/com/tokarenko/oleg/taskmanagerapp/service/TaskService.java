package com.tokarenko.oleg.taskmanagerapp.service;

import com.tokarenko.oleg.taskmanagerapp.database.Database;
import com.tokarenko.oleg.taskmanagerapp.model.Task;
import com.tokarenko.oleg.taskmanagerapp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static com.tokarenko.oleg.taskmanagerapp.custom_query.TaskMySqlQuery.*;

/**
 * Service class for managing tasks
*/

public final class TaskService {

    private TaskService() { }

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

    private static Connection connection = Database.getConnection();

    private static Task currentTask;

    public static void createAndAddTask(String content) {
        User user = AuthService.getCurrentUser();

        if (!content.trim().equals("")) {
            try (PreparedStatement createNewTask = connection.prepareStatement(INSERT_NEW_TASK)) {
                createNewTask.setString(1, content);
                createNewTask.execute();

                currentTask = new Task();
                currentTask.setContent(content);

                LOGGER.info("New task has been created");
            } catch (SQLException e) {
                LOGGER.error("Error: failed to create new task", e);
            }

            boolean isInserted = insertTask(user);

            if (isInserted) {
                LOGGER.info("New task successfully added to set");
            } else {
                LOGGER.error("Error: failed to add new task");
            }
        } else {
            LOGGER.error("Error: task content should not be empty");
        }
    }

    public static void editTask(long id, String content) {
        boolean isUpdated = updateTask(id, content);

        if (isUpdated) {
            LOGGER.info("Task with id " + id + " is successfully edited");
        } else {
            LOGGER.error("Error: failed to update task");
        }
    }

    public static void removeTask(long id) {
        boolean isDeleted = deleteTask(id);

        if (isDeleted) {
            LOGGER.info("Task with id " + id + " was successfully removed");
        } else {
            LOGGER.error("Error: failed to remove task");
        }
    }

    public static void shareTask(long id, String username) {
        boolean isSent = sendTask(id, username);

        if (isSent) {
            LOGGER.info("Task with id " + id + " has been shared with " + username);
        } else {
            LOGGER.error("Error: failed to share task");
        }
    }

    private static boolean sendTask(long id, String username) {
        PreparedStatement preparedStatement = null;
        ResultSet taskResultSet = null;
        ResultSet userTasksResultSet = null;
        ResultSet lastInsertedTaskResultSet = null;
        ResultSet userResultSet = null;

        User user = AuthService.getCurrentUser();

        try {
            // Set and retrieve task by specified id in method parameter
            preparedStatement = connection.prepareStatement(GET_TASK_BY_ID);
            preparedStatement.setLong(1, id);
            taskResultSet = preparedStatement.executeQuery();
            taskResultSet.next();

            // Set current user id to obtain tasks he manages
            preparedStatement = connection.prepareStatement(GET_TASKS_BY_USER_ID);
            preparedStatement.setLong(1, user.getId());
            userTasksResultSet = preparedStatement.executeQuery();

            // Check whether current user has any tasks
            if (!userTasksResultSet.next()) {
                return false;
            }

            userTasksResultSet = preparedStatement.executeQuery();
            userTasksResultSet.next();

            // Check whether current user manage any task with provided id
            if (!userHasAnyTasks(userTasksResultSet, id)) {
                return false;
            }

            // Set content and generate new task with old content
            preparedStatement = connection.prepareStatement(INSERT_NEW_TASK);
            preparedStatement.setString(1, taskResultSet.getString("content"));
            preparedStatement.execute();

            // Retrieve last inserted task
            preparedStatement = connection.prepareStatement(GET_LAST_INSERTED_TASK);
            lastInsertedTaskResultSet = preparedStatement.executeQuery();
            lastInsertedTaskResultSet.next();

            // Set and get user by name
            preparedStatement = connection.prepareStatement(GET_USER_BY_NAME);
            preparedStatement.setString(1, username);
            userResultSet = preparedStatement.executeQuery();
            userResultSet.next();

            // Bind task to user
            preparedStatement = connection.prepareStatement(INSERT_TASKS_USERS);
            preparedStatement.setLong(1, userResultSet.getLong("id"));
            preparedStatement.setLong(2, lastInsertedTaskResultSet.getLong("id"));
            preparedStatement.execute();
        } catch (SQLException e) {
            return false;
        } finally {
            if (userResultSet != null) {
                try {
                    userResultSet.close();
                } catch (SQLException ignored) {
                }
            }

            if (lastInsertedTaskResultSet != null) {
                try {
                    lastInsertedTaskResultSet.close();
                } catch (SQLException ignored) {
                }
            }

            if (userTasksResultSet != null) {
                try {
                    userTasksResultSet.close();
                } catch (SQLException ignored) {
                }
            }

            if (taskResultSet != null) {
                try {
                    taskResultSet.close();
                } catch (SQLException ignored) {
                }
            }

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException ignored) {
                }
            }
        }

        return true;
    }

    private static boolean updateTask(long id, String content) {
        if (content.trim().equals("")) {
            return false;
        }

        try (PreparedStatement updateTaskById = connection.prepareStatement(UPDATE_TASK_BY_ID)) {
            updateTaskById.setString(1, content);
            updateTaskById.setLong(2, id);

            if (updateTaskById.executeUpdate() == 0) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }

        return true;
    }

    private static boolean deleteTask(long id) {
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(DELETE_TASK_USER);
            preparedStatement.setLong(1, id);

            if (preparedStatement.executeUpdate() == 0) {
                return false;
            }

            preparedStatement = connection.prepareStatement(DELETE_TASK_BY_ID);
            preparedStatement.setLong(1, id);

            if (preparedStatement.executeUpdate() == 0) {
                return false;
            }
        } catch (SQLException e) {
            LOGGER.error("Error: failed to delete task", e);
            return false;
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException ignored) {
                }
            }
        }

        currentTask = null;

        return true;
    }

    private static boolean insertTask(User user) {
        Connection connection = Database.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet tasksResultSet = null;

        try {
            // Set and retrieve task
            preparedStatement = connection.prepareStatement(GET_TASK_BY_CONTENT);
            preparedStatement.setString(1, currentTask.getContent());
            tasksResultSet = preparedStatement.executeQuery();
            setValues(tasksResultSet);

            // Bind task to user
            preparedStatement = connection.prepareStatement(INSERT_TASKS_USERS);
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setLong(2, currentTask.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            return false;
        } finally {
            if (tasksResultSet != null) {
                try {
                    tasksResultSet.close();
                } catch (SQLException ignored) {
                }
            }

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException ignored) {
                }
            }
        }

        return true;
    }

    private static void setValues(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            currentTask = new Task();
            currentTask.setId(resultSet.getLong("id"));
            currentTask.setContent(resultSet.getString("content"));
        }
    }

    private static boolean userHasAnyTasks(ResultSet userTasksResultSet, long id) {
        return Stream.of(userTasksResultSet).anyMatch(ut -> {
            try {
                return ut.getLong("tasks_id") == id;
            } catch (SQLException e) {
                return false;
            }
        });
    }
}
