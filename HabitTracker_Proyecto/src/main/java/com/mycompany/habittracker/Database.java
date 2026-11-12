package com.mycompany.habittracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String URL = "jdbc:sqlite:habittracker.db";
    private Database() {}

    public static Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initialize() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS habit (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT NOT NULL DEFAULT '', created_at TEXT NOT NULL DEFAULT (date('now','localtime')))");
            statement.execute("CREATE TABLE IF NOT EXISTS habit_log (id INTEGER PRIMARY KEY AUTOINCREMENT, habit_id INTEGER NOT NULL, log_date TEXT NOT NULL, completed INTEGER NOT NULL DEFAULT 0 CHECK (completed IN (0,1)), UNIQUE(habit_id, log_date), FOREIGN KEY(habit_id) REFERENCES habit(id) ON DELETE CASCADE)");
            statement.execute("INSERT INTO habit(name,description) SELECT 'Entrenar','Ir al gimnasio' WHERE NOT EXISTS (SELECT 1 FROM habit)");
            statement.execute("INSERT OR IGNORE INTO habit(name,description) VALUES ('Tomar agua','Tomar 2 litros de agua'),('Estudiar','Estudiar Java')");
        }
    }
}
