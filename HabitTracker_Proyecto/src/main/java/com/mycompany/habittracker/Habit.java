package com.mycompany.habittracker;

import java.time.LocalDate;

public class Habit {
    private final int id;
    private final String name;
    private final String description;
    private final LocalDate createdAt;
    private final int completedDays;

    public Habit(int id, String name, String description, LocalDate createdAt, int completedDays) {
        this.id = id; this.name = name; this.description = description;
        this.createdAt = createdAt; this.completedDays = completedDays;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getCreatedAt() { return createdAt; }
    public int getCompletedDays() { return completedDays; }
    @Override public String toString() { return name; }
}
