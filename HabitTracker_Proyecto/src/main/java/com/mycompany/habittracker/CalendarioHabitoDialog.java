package com.mycompany.habittracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/** Calendario mensual para consultar y actualizar el cumplimiento diario de un hábito. */
public class CalendarioHabitoDialog extends JDialog {
    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-CO");
    private static final Color COMPLETADO = new Color(190, 235, 202);
    private static final Color PENDIENTE = Color.WHITE;

    private final int habitId;
    private final String habitName;
    private final GestorHabitos gestor = new GestorHabitos();
    private final JComboBox<Month> monthBox = new JComboBox<>(Month.values());
    private final JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 1900, 9999, 1));
    private final JPanel daysPanel = new JPanel(new java.awt.GridLayout(7, 7, 5, 5));
    private final JLabel monthTitle = new JLabel("", SwingConstants.CENTER);
    private final JLabel summary = new JLabel("", SwingConstants.CENTER);

    public CalendarioHabitoDialog(JFrame owner, int habitId, String habitName) {
        super(owner, "Calendario de " + habitName, ModalityType.APPLICATION_MODAL);
        this.habitId = habitId;
        this.habitName = habitName;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JButton previous = new JButton("◀");
        JButton next = new JButton("▶");
        previous.setToolTipText("Mes anterior");
        next.setToolTipText("Mes siguiente");
        JPanel navigation = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        navigation.add(previous);
        navigation.add(monthBox);
        navigation.add(yearSpinner);
        navigation.add(next);
        JPanel heading = new JPanel(new BorderLayout(4, 4));
        heading.add(new JLabel("Hábito: " + habitName, SwingConstants.CENTER), BorderLayout.NORTH);
        monthTitle.setFont(monthTitle.getFont().deriveFont(Font.BOLD, 18f));
        heading.add(navigation, BorderLayout.CENTER);
        heading.add(monthTitle, BorderLayout.SOUTH);
        add(heading, BorderLayout.NORTH);

        String[] weekdays = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (String weekday : weekdays) {
            JLabel label = new JLabel(weekday, SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            daysPanel.add(label);
        }
        add(daysPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(4, 5));
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        legend.add(new JLabel("■ Cumplido", SwingConstants.CENTER));
        legend.add(new JLabel("□ Pendiente", SwingConstants.CENTER));
        footer.add(legend, BorderLayout.NORTH);
        footer.add(summary, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);

        monthBox.setRenderer(new DefaultListCellRenderer() {
            @Override public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Month month) setText(capitalizar(month.getDisplayName(TextStyle.FULL, LOCALE_ES)));
                return this;
            }
        });
        monthBox.setSelectedItem(LocalDate.now().getMonth());
        monthBox.addActionListener(e -> refreshCalendar());
        yearSpinner.addChangeListener(e -> refreshCalendar());
        previous.addActionListener(e -> moveMonth(-1));
        next.addActionListener(e -> moveMonth(1));

        setMinimumSize(new Dimension(500, 430));
        refreshCalendar();
        pack();
        setLocationRelativeTo(owner);
    }

    private void moveMonth(int delta) {
        Month month = (Month) monthBox.getSelectedItem();
        int year = (Integer) yearSpinner.getValue();
        if ((delta > 0 && year == 9999 && month == Month.DECEMBER)
                || (delta < 0 && year == 1900 && month == Month.JANUARY)) return;
        LocalDate target = LocalDate.of(year, month, 1).plusMonths(delta);
        if (target.getYear() >= 1900 && target.getYear() <= 9999) {
            monthBox.setSelectedItem(target.getMonth());
            yearSpinner.setValue(target.getYear());
        }
    }

    private void refreshCalendar() {
        Month month = (Month) monthBox.getSelectedItem();
        if (month == null) return;
        int year = (Integer) yearSpinner.getValue();
        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate last = first.withDayOfMonth(first.lengthOfMonth());
        monthTitle.setText(capitalizar(month.getDisplayName(TextStyle.FULL, LOCALE_ES)) + " de " + year);
        daysPanel.removeAll();
        String[] weekdays = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (String weekday : weekdays) {
            JLabel label = new JLabel(weekday, SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            daysPanel.add(label);
        }
        int offset = first.getDayOfWeek().getValue() - 1;
        for (int blank = 0; blank < offset; blank++) daysPanel.add(new JLabel(""));

        Map<LocalDate, Boolean> records;
        String loadError = null;
        try {
            records = gestor.registrosDelMes(habitId, first, last);
        } catch (SQLException ex) {
            records = Map.of();
            loadError = ex.getMessage();
        }
        int completed = 0;
        for (int day = 1; day <= last.getDayOfMonth(); day++) {
            LocalDate date = first.withDayOfMonth(day);
            boolean isCompleted = Boolean.TRUE.equals(records.get(date));
            if (isCompleted) completed++;
            JButton cell = new JButton(day + (isCompleted ? " ✓" : ""));
            cell.setPreferredSize(new Dimension(52, 38));
            cell.setOpaque(true);
            cell.setBackground(isCompleted ? COMPLETADO : PENDIENTE);
            cell.setToolTipText(isCompleted ? "Cumplido: " + date : "Pendiente: " + date);
            cell.setEnabled(!date.isAfter(LocalDate.now()));
            cell.addActionListener(e -> toggleDate(date));
            daysPanel.add(cell);
        }
        while (daysPanel.getComponentCount() < 49) daysPanel.add(new JLabel(""));
        summary.setText(loadError == null
            ? "Días cumplidos: " + completed + " de " + last.getDayOfMonth() + " · Haz clic en un día pasado o actual para cambiarlo."
            : "No se pudo leer el historial: " + loadError);
        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private void toggleDate(LocalDate date) {
        try {
            boolean completed = !gestor.registrosDelMes(habitId, date, date).getOrDefault(date, false);
            gestor.marcarFecha(habitId, date, completed);
            refreshCalendar();
        } catch (SQLException ex) {
            summary.setText("No se pudo guardar el cambio: " + ex.getMessage());
        }
    }

    private static String capitalizar(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase(LOCALE_ES) + text.substring(1);
    }
}
