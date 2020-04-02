package com.tokarenko.oleg.taskmanagerapp.custom_query;

public final class TaskMySqlQuery {

    private TaskMySqlQuery() { }

    public static final String GET_TASKS_BY_USER_ID =
            "SELECT * FROM tasks LEFT JOIN users_tasks u on tasks.id = u.tasks_id WHERE u.users_id = ?;";

    public static final String INSERT_NEW_TASK
            = "INSERT INTO tasks(content) VALUE(?);";

    public static final String GET_LAST_INSERTED_TASK
            = "SELECT * FROM tasks WHERE id=(SELECT LAST_INSERT_ID());";

    public static final String GET_TASK_BY_CONTENT
            = "SELECT * FROM tasks WHERE content = ?;";

    public static final String GET_TASK_BY_ID
            = "SELECT * FROM tasks WHERE id = ?;";

    public static final String INSERT_TASKS_USERS
            = "INSERT INTO users_tasks(users_id, tasks_id) VALUES(?, ?);";

    public static final String UPDATE_TASK_BY_ID
            = "UPDATE tasks JOIN users_tasks" +
            "  JOIN users u ON tasks.id = users_tasks.tasks_id AND u.id = users_tasks.users_id" +
            "  SET content = ? WHERE tasks.id = ? AND u.active = TRUE;";

    public static final String DELETE_TASK_USER
            = "DELETE users_tasks FROM users_tasks" +
            "  JOIN users u JOIN tasks t ON users_tasks.tasks_id = t.id AND users_tasks.users_id = u.id" +
            "  WHERE t.id = ? AND u.active = TRUE;";

    public static final String DELETE_TASK_BY_ID
            = "DELETE FROM tasks WHERE id = ?;";

    public static final String GET_USER_BY_NAME
            = "SELECT * FROM users WHERE name = ?;";
}
