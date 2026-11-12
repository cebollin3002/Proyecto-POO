package com.mycompany.habittracker;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestorHabitos {
    public List<Habit> listar() throws SQLException {
        String sql = "SELECT h.id,h.name,h.description,h.created_at,COUNT(CASE WHEN l.completed=1 THEN 1 END) AS completed_days FROM habit h LEFT JOIN habit_log l ON l.habit_id=h.id GROUP BY h.id ORDER BY h.name COLLATE NOCASE";
        List<Habit> result = new ArrayList<>();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new Habit(rs.getInt("id"), rs.getString("name"), rs.getString("description"), LocalDate.parse(rs.getString("created_at")), rs.getInt("completed_days")));
        }
        return result;
    }
    public void agregar(String name, String description) throws SQLException {
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement("INSERT INTO habit(name,description) VALUES(?,?)")) { ps.setString(1,name.trim()); ps.setString(2,description.trim()); ps.executeUpdate(); }
    }
    public void actualizar(int id, String name, String description) throws SQLException {
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement("UPDATE habit SET name=?,description=? WHERE id=?")) { ps.setString(1,name.trim()); ps.setString(2,description.trim()); ps.setInt(3,id); ps.executeUpdate(); }
    }
    public void eliminar(int id) throws SQLException {
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM habit WHERE id=?")) { ps.setInt(1,id); ps.executeUpdate(); }
    }
    public void marcarHoy(int id, boolean completed) throws SQLException {
        marcarFecha(id, LocalDate.now(), completed);
    }
    public void marcarFecha(int id, LocalDate date, boolean completed) throws SQLException {
        String sql = "INSERT INTO habit_log(habit_id,log_date,completed) VALUES(?,?,?) ON CONFLICT(habit_id,log_date) DO UPDATE SET completed=excluded.completed";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ps.setString(2, date.toString()); ps.setInt(3, completed ? 1 : 0); ps.executeUpdate();
        }
    }
    public Map<LocalDate, Boolean> registrosDelMes(int id, LocalDate primerDia, LocalDate ultimoDia) throws SQLException {
        Map<LocalDate, Boolean> registros = new HashMap<>();
        String sql = "SELECT log_date,completed FROM habit_log WHERE habit_id=? AND log_date BETWEEN ? AND ?";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ps.setString(2, primerDia.toString()); ps.setString(3, ultimoDia.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) registros.put(LocalDate.parse(rs.getString("log_date")), rs.getInt("completed") == 1);
            }
        }
        return registros;
    }
    public boolean estaCompletadoHoy(int id) throws SQLException {
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement("SELECT completed FROM habit_log WHERE habit_id=? AND log_date=?")) { ps.setInt(1,id); ps.setString(2,LocalDate.now().toString()); try (ResultSet rs=ps.executeQuery()) { return rs.next() && rs.getInt(1)==1; } }
    }
}
