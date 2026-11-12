package com.mycompany.habittracker;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class HabitTracker {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Database.initialize();
                new FrmPrincipal().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "No se pudo iniciar la base de datos. Verifique que SQLite JDBC se haya descargado.\n" + ex.getMessage(),
                    "Error de inicio", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
