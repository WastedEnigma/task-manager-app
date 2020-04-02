package com.tokarenko.oleg.taskmanagerapp.custom_query;

public final class UserMySqlQuery {

    private UserMySqlQuery() { }

    public static final String INSERT_NEW_USER =
            "INSERT INTO users(active, email, name, password) VALUES(?, ?, ?, ?);";

    public static final String LOGIN = "SELECT * FROM users WHERE name = ? AND password = ?;";

    public static final String OPEN_SESSION = "UPDATE users set active = true WHERE id = ?;";

    public static final String CLOSE_SESSION = "UPDATE users set active = false WHERE id = ?;";
}
