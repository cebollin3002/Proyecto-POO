package com.mycompany.habittracker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FrmPrincipal extends JFrame {
    private final GestorHabitos gestor = new GestorHabitos();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Hábito", "Descripción", "Creado", "Días cumplidos"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel status = new JLabel("  Gestione sus hábitos y registre su progreso diario.");

    public FrmPrincipal() {
        super("Habit Tracker | Panel administrativo");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(780, 500));
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setJMenuBar(createMenu());
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(18,22,14,22));
        JLabel title = new JLabel("HABIT TRACKER"); title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        header.add(title, BorderLayout.NORTH);
        header.add(new JLabel("Panel administrativo · Seguimiento de hábitos"), BorderLayout.SOUTH);
        table.setRowHeight(28); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMinWidth(0); table.getColumnModel().getColumn(0).setMaxWidth(0);
        JPanel actions = new JPanel();
        JButton add = new JButton("Nuevo hábito"), edit = new JButton("Editar"), delete = new JButton("Eliminar"), mark = new JButton("Marcar hoy cumplido"), calendar = new JButton("Ver calendario");
        add.addActionListener(e -> nuevo()); edit.addActionListener(e -> editar()); delete.addActionListener(e -> eliminar()); mark.addActionListener(e -> marcarHoy()); calendar.addActionListener(e -> abrirCalendario());
        actions.add(add); actions.add(edit); actions.add(delete); actions.add(mark); actions.add(calendar);
        add(header, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER);
        // La barra de estado se coloca arriba del pie sin ocultar las acciones.
        JPanel south = new JPanel(new BorderLayout()); south.add(actions, BorderLayout.CENTER); south.add(status, BorderLayout.SOUTH); add(south, BorderLayout.SOUTH);
        cargar();
    }
    private JMenuBar createMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu principal = new JMenu("Principal"), habitos = new JMenu("Hábitos"), registros = new JMenu("Registros");
        JMenuItem inicio = new JMenuItem("Inicio"); inicio.addActionListener(e -> cargar());
        JMenuItem salir = new JMenuItem("Salir"); salir.addActionListener(e -> { if (JOptionPane.showConfirmDialog(this,"¿Desea cerrar Habit Tracker?","Salir",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) { JOptionPane.showMessageDialog(this,"¡Hasta pronto!"); dispose(); } });
        JMenuItem crear = new JMenuItem("Nuevo hábito"); crear.addActionListener(e -> nuevo());
        JMenuItem actualizar = new JMenuItem("Editar hábito"); actualizar.addActionListener(e -> editar());
        JMenuItem borrar = new JMenuItem("Eliminar hábito"); borrar.addActionListener(e -> eliminar());
        JMenuItem hoy = new JMenuItem("Marcar hábito de hoy"); hoy.addActionListener(e -> marcarHoy());
        JMenuItem calendario = new JMenuItem("Calendario mensual"); calendario.addActionListener(e -> abrirCalendario());
        principal.add(inicio); principal.addSeparator(); principal.add(salir);
        habitos.add(crear); habitos.add(actualizar); habitos.add(borrar); registros.add(hoy); registros.add(calendario);
        bar.add(principal); bar.add(habitos); bar.add(registros); return bar;
    }
    private void cargar() {
        try { model.setRowCount(0); List<Habit> habits=gestor.listar(); for(Habit h:habits) model.addRow(new Object[]{h.getId(),h.getName(),h.getDescription(),h.getCreatedAt(),h.getCompletedDays()}); status.setText("  Hábitos registrados: " + habits.size()); }
        catch(SQLException ex) { error("No se pudieron cargar los hábitos",ex); }
    }
    private int selectedId() { int row=table.getSelectedRow(); if(row<0) { JOptionPane.showMessageDialog(this,"Seleccione un hábito de la tabla."); return -1; } return (int)table.getValueAt(table.convertRowIndexToModel(row),0); }
    private void nuevo() {
        JTextField name=new JTextField(22); JTextArea description=new JTextArea(4,22); description.setLineWrap(true); description.setWrapStyleWord(true);
        JPanel form=new JPanel(new BorderLayout(5,8)); JPanel fields=new JPanel(new java.awt.GridLayout(0,1,4,4)); fields.add(new JLabel("Nombre")); fields.add(name); fields.add(new JLabel("Descripción")); form.add(fields,BorderLayout.NORTH); form.add(new JScrollPane(description),BorderLayout.CENTER);
        if(JOptionPane.showConfirmDialog(this,form,"Crear hábito",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION) { if(name.getText().isBlank()) { JOptionPane.showMessageDialog(this,"El nombre es obligatorio."); return; } try { gestor.agregar(name.getText(),description.getText()); cargar(); } catch(SQLException ex) { error("No se pudo guardar. El nombre debe ser único.",ex); } }
    }
    private void editar() {
        int id=selectedId(); if(id<0)return; int row=table.convertRowIndexToModel(table.getSelectedRow());
        JTextField name=new JTextField(String.valueOf(model.getValueAt(row,1)),22); JTextArea description=new JTextArea(String.valueOf(model.getValueAt(row,2)),4,22); description.setLineWrap(true); description.setWrapStyleWord(true);
        JPanel p=new JPanel(new BorderLayout(5,8)); JPanel fields=new JPanel(new java.awt.GridLayout(0,1,4,4)); fields.add(new JLabel("Nombre")); fields.add(name); fields.add(new JLabel("Descripción")); p.add(fields,BorderLayout.NORTH); p.add(new JScrollPane(description),BorderLayout.CENTER);
        if(JOptionPane.showConfirmDialog(this,p,"Editar hábito",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION) { if(name.getText().isBlank()) { JOptionPane.showMessageDialog(this,"El nombre es obligatorio."); return; } try { gestor.actualizar(id,name.getText(),description.getText()); cargar(); } catch(SQLException ex) { error("No se pudo actualizar. El nombre debe ser único.",ex); } }
    }
    private void eliminar() { int id=selectedId(); if(id<0)return; if(JOptionPane.showConfirmDialog(this,"¿Eliminar el hábito y su historial diario?","Confirmar eliminación",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) try { gestor.eliminar(id); cargar(); } catch(SQLException ex) { error("No se pudo eliminar el hábito",ex); } }
    private void marcarHoy() { int id=selectedId(); if(id<0)return; try { boolean complete=!gestor.estaCompletadoHoy(id); gestor.marcarHoy(id,complete); cargar(); status.setText("  Hábito " + (complete ? "marcado como cumplido" : "desmarcado") + " para hoy."); } catch(SQLException ex) { error("No se pudo guardar el registro diario",ex); } }
    private void abrirCalendario() {
        int id = selectedId();
        if (id < 0) return;
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        String name = String.valueOf(model.getValueAt(row, 1));
        new CalendarioHabitoDialog(this, id, name).setVisible(true);
        cargar();
    }
    private void error(String message, Exception ex) { JOptionPane.showMessageDialog(this,message+"\n"+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); }
}





