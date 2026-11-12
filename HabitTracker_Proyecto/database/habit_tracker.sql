-- Habit Tracker | SQLite
-- Ejecutar una sola vez; la aplicación también crea estas tablas automáticamente.
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS habit (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL DEFAULT '',
    created_at  TEXT NOT NULL DEFAULT (date('now','localtime'))
);

CREATE TABLE IF NOT EXISTS habit_log (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    habit_id  INTEGER NOT NULL,
    log_date  TEXT NOT NULL,
    completed INTEGER NOT NULL DEFAULT 0 CHECK (completed IN (0, 1)),
    CONSTRAINT uq_habit_log_date UNIQUE (habit_id, log_date),
    CONSTRAINT fk_habit_log_habit FOREIGN KEY (habit_id)
        REFERENCES habit(id) ON DELETE CASCADE
);
