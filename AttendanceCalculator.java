import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class AttendanceCalculator extends JFrame {
    private static final String APP_VERSION = "2.1.0";
    private JTextField subjectField;
    private JTextField totalClassesField;
    private JTextField attendedClassesField;
    private JTextField requiredPercentageField;
    private DefaultTableModel tableModel;
    private JTable subjectTable;
    private JLabel overallAttendanceLabel;
    private JCheckBoxMenuItem autoSaveMenuItem;
    private boolean autoSave = true;
    private boolean darkMode = false;
    private String databaseFile = "attendance_database.csv";
    private Stack<Object[][]> undoStack = new Stack<>();
    private java.util.Map<String, java.util.List<Double>> attendanceHistory = new java.util.HashMap<>();

    public AttendanceCalculator() {
        setTitle("Attendance Calculator Pro v" + APP_VERSION);
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 450));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (tableModel.getRowCount() > 0) {
                    int confirm = JOptionPane.showConfirmDialog(null, "Do you want to exit? Unsaved changes may be lost.", "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (autoSave) saveDataQuiet();
                        System.exit(0);
                    }
                } else {
                    System.exit(0);
                }
            }
        });

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 152, 219));
        JLabel titleLabel = new JLabel("Attendance Calculator Pro");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setToolTipText("Shortcuts: Ctrl+S Save | Ctrl+L Load | Ctrl+E Export | Ctrl+P Print | F1 Help");
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        headerPanel.add(titleLabel);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JMenuItem saveMenu = new JMenuItem("Save");
        saveMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        saveMenu.addActionListener(e -> saveData());
        JMenuItem loadMenu = new JMenuItem("Load");
        loadMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loadMenu.addActionListener(e -> loadData());
        JMenuItem exportMenu = new JMenuItem("Export as CSV");
        exportMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        exportMenu.addActionListener(e -> exportCSV());
        JMenuItem importMenu = new JMenuItem("Import from CSV");
        importMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        importMenu.addActionListener(e -> importCSV());
        JMenuItem exitMenu = new JMenuItem("Exit");
        exitMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        exitMenu.addActionListener(e -> System.exit(0));
        fileMenu.add(saveMenu);
        fileMenu.add(loadMenu);
        fileMenu.add(exportMenu);
        fileMenu.add(importMenu);
        fileMenu.addSeparator();
        fileMenu.add(exitMenu);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        autoSaveMenuItem = new JCheckBoxMenuItem("Auto-save", autoSave);
        autoSaveMenuItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        autoSaveMenuItem.addActionListener(e -> autoSave = autoSaveMenuItem.isSelected());
        viewMenu.add(autoSaveMenuItem);

        JCheckBoxMenuItem darkModeMenuItem = new JCheckBoxMenuItem("Dark Mode", false);
        darkModeMenuItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        darkModeMenuItem.addActionListener(e -> {
            darkMode = darkModeMenuItem.isSelected();
            applyDarkMode(darkMode);
        });
        viewMenu.add(darkModeMenuItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JMenuItem aboutMenu = new JMenuItem("About");
        aboutMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        aboutMenu.addActionListener(e -> JOptionPane.showMessageDialog(this, "Attendance Calculator Pro v" + APP_VERSION + "\nMade by LOQ", "About", JOptionPane.INFORMATION_MESSAGE));
        JMenuItem helpContentMenu = new JMenuItem("Help");
        helpContentMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        helpContentMenu.addActionListener(e -> JOptionPane.showMessageDialog(this, "Keyboard Shortcuts:\n"
                + "Ctrl+S - Save | Ctrl+L - Load | Ctrl+E - Export | Ctrl+A - Select All\n"
                + "Enter - Calculate | Click table headers to sort | Use Search field to filter\n\n"
                + "How to use:\n"
                + "1. Enter subject name, total classes, attended classes, and required percentage.\n"
                + "2. Click Calculate (or press Enter) to add to the table.\n"
                + "3. Status column shows if you're safe or need more classes.\n"
                + "4. Use File menu to save/load or export/import data.\n"
                + "5. Toggle Dark Mode from View menu.\n"
                + "6. Use Search field to filter subjects in the table.", "Help", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(helpContentMenu);
        helpMenu.add(aboutMenu);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // Keyboard shortcuts
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("control S"), "save");
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                saveData();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control L"), "load");
        actionMap.put("load", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                loadData();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control A"), "selectAll");
        actionMap.put("selectAll", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                subjectTable.selectAll();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control E"), "export");
        actionMap.put("export", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportCSV();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control R"), "resetFields");
        actionMap.put("resetFields", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                subjectField.setText("");
                totalClassesField.setText("");
                attendedClassesField.setText("");
                requiredPercentageField.setText("75");
                subjectField.requestFocus();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("DELETE"), "deleteRow");
        actionMap.put("deleteRow", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int selectedRow = subjectTable.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                    tableModel.removeRow(modelRow);
                    updateOverallAttendance();
                }
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control Z"), "undo");
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                undoLastAction();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("F1"), "showHelp");
        actionMap.put("showHelp", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Keyboard Shortcuts:\n"
                        + "Ctrl+S - Save | Ctrl+L - Load | Ctrl+E - Export | Ctrl+A - Select All\n"
                        + "Ctrl+R - Reset Fields | Ctrl+N - New Subject | F1 - Help | Delete - Remove Row\n"
                        + "Enter - Calculate | Click table headers to sort | Use Search field to filter\n\n"
                        + "How to use:\n"
                        + "1. Enter subject name, total classes, attended classes, and required percentage.\n"
                        + "2. Click Calculate (or press Enter) to add to the table.\n"
                        + "3. Status column shows if you're safe or need more classes.\n"
                        + "4. Use File menu to save/load or export/import data.\n"
                        + "5. Toggle Dark Mode from View menu.\n"
                        + "6. Use Search field to filter subjects in the table.", "Help - Attendance Calculator Pro", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control N"), "newSubject");
        actionMap.put("newSubject", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                subjectField.setText("");
                totalClassesField.setText("");
                attendedClassesField.setText("");
                requiredPercentageField.setText("75");
                subjectField.requestFocus();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("control P"), "printTable");
        actionMap.put("printTable", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                printAttendanceTable();
            }
        });

        // Top Panel for Inputs
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Add New Subject", 0, 0, new Font("Segoe UI", Font.BOLD, 12)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel subLabel = new JLabel("Subject Name:");
        subLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(subLabel, gbc);
        subjectField = new JTextField(15);
        subjectField.setFont(fieldFont);
        subjectField.setToolTipText("Enter the name of the subject");
        gbc.gridx = 1; gbc.gridy = 0; inputPanel.add(subjectField, gbc);

        JLabel totLabel = new JLabel("Total Classes:");
        totLabel.setFont(labelFont);
        gbc.gridx = 2; gbc.gridy = 0; inputPanel.add(totLabel, gbc);
        totalClassesField = new JTextField(10);
        totalClassesField.setFont(fieldFont);
        totalClassesField.setToolTipText("Total number of classes held");
        gbc.gridx = 3; gbc.gridy = 0; inputPanel.add(totalClassesField, gbc);

        JLabel attLabel = new JLabel("Attended Classes:");
        attLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(attLabel, gbc);
        attendedClassesField = new JTextField(15);
        attendedClassesField.setFont(fieldFont);
        attendedClassesField.setToolTipText("Number of classes you attended");
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(attendedClassesField, gbc);

        JLabel reqLabel = new JLabel("Required %:");
        reqLabel.setFont(labelFont);
        gbc.gridx = 2; gbc.gridy = 1; inputPanel.add(reqLabel, gbc);
        requiredPercentageField = new JTextField("75", 10);
        requiredPercentageField.setFont(fieldFont);
        requiredPercentageField.setToolTipText("Minimum attendance percentage required (default: 75%)");
        gbc.gridx = 3; gbc.gridy = 1; inputPanel.add(requiredPercentageField, gbc);

        // Enter key triggers calculate
        java.awt.event.KeyListener enterKeyListener = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    calculateAndAdd();
                }
            }
        };
        subjectField.addKeyListener(enterKeyListener);
        totalClassesField.addKeyListener(enterKeyListener);
        attendedClassesField.addKeyListener(enterKeyListener);
        requiredPercentageField.addKeyListener(enterKeyListener);

        JButton calculateButton = new JButton("Calculate & Add");
        calculateButton.setBackground(new Color(39, 174, 96));
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFocusPainted(false);
        calculateButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        calculateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calculateButton.setToolTipText("Calculate attendance and add subject to table (Enter)");
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; 
        gbc.insets = new Insets(15, 5, 5, 5);
        inputPanel.add(calculateButton, gbc);

        // Center Panel for Table
        String[] columns = {"Subject", "Total", "Attended", "Current %", "Required %", "Status / Needed", "Trend"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 1 || column == 2;
            }
        };
        subjectTable = new JTable(tableModel);
        subjectTable.setRowHeight(32);
        subjectTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subjectTable.setRowSorter(new javax.swing.table.TableRowSorter<>(tableModel));
        subjectTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        subjectTable.getTableHeader().setBackground(new Color(236, 240, 241));

        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (row >= 0 && row < tableModel.getRowCount()) {
                    try {
                        int total = Integer.parseInt(tableModel.getValueAt(row, 1).toString());
                        int attended = Integer.parseInt(tableModel.getValueAt(row, 2).toString());
                        if (total > 0 && attended >= 0 && attended <= total) {
                            double pct = ((double) attended / total) * 100;
                            tableModel.setValueAt(String.format("%.2f%%", pct), row, 3);
                            double req = Double.parseDouble(tableModel.getValueAt(row, 4).toString().replace("%", ""));
                            String status;
                            if (pct >= req) {
                                int canMiss = (int) Math.floor(((double) attended * 100 / req) - total);
                                status = canMiss > 0 ? "Safe! Can miss " + canMiss + " classes." : "On track.";
                            } else {
                                double r = req / 100.0;
                                int needed = (int) Math.ceil((r * total - attended) / (1 - r));
                                status = "Alert! Need " + needed + " more.";
                            }
                            tableModel.setValueAt(status, row, 5);
                            updateOverallAttendance();
                        }
                    } catch (Exception ignored) {}
                }
            }
        });
        subjectTable.setSelectionBackground(new Color(189, 195, 199));

        // Custom renderer for row colors based on attendance percentage
        subjectTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String currentPctStr = (String) table.getModel().getValueAt(row, 3);
                    try {
                        double pct = Double.parseDouble(currentPctStr.replace("%", ""));
                        if (pct >= 90) {
                            c.setBackground(new Color(200, 240, 200)); // Dark green - Excellent
                        } else if (pct >= 75) {
                            c.setBackground(new Color(230, 255, 230)); // Light green - Good
                        } else if (pct >= 60) {
                            c.setBackground(new Color(255, 255, 200)); // Light yellow - Warning
                        } else if (pct >= 50) {
                            c.setBackground(new Color(255, 220, 180)); // Light orange - Danger
                        } else {
                            c.setBackground(new Color(255, 200, 200)); // Light red - Critical
                        }
                    } catch (Exception e) {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(subjectTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Subject Details", 0, 0, new Font("Segoe UI", Font.BOLD, 12)));

        JPopupMenu tableContextMenu = new JPopupMenu();
        JMenuItem ctxDelete = new JMenuItem("Delete Selected Row");
        ctxDelete.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxDelete.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                tableModel.removeRow(modelRow);
                updateOverallAttendance();
            }
        });
        JMenuItem ctxClearAll = new JMenuItem("Clear All Rows");
        ctxClearAll.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxClearAll.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) return;
            int confirm = JOptionPane.showConfirmDialog(null, "Clear all rows?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tableModel.setRowCount(0);
                updateOverallAttendance();
            }
        });
        JMenuItem ctxExport = new JMenuItem("Export as CSV");
        ctxExport.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ctxExport.addActionListener(e -> exportCSV());
        tableContextMenu.add(ctxDelete);
        tableContextMenu.add(ctxClearAll);
        tableContextMenu.addSeparator();
        tableContextMenu.add(ctxExport);
        subjectTable.setComponentPopupMenu(tableContextMenu);

        // Filter/search panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        filterPanel.add(new JLabel("Search:"));
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterPanel.add(searchField);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String query = searchField.getText().trim().toLowerCase();
                javax.swing.table.TableRowSorter<DefaultTableModel> sorter = (javax.swing.table.TableRowSorter<DefaultTableModel>) subjectTable.getRowSorter();
                if (query.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + query));
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.add(filterPanel, BorderLayout.NORTH);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel for actions and summary
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.setBackground(new Color(192, 57, 43));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setToolTipText("Delete the selected row from table (Delete key)");
        
        JButton clearButton = new JButton("Clear All");
        clearButton.setBackground(new Color(149, 165, 166));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.setToolTipText("Clear all subjects from the table");

        JButton predictButton = new JButton("Predict Attendance");
        predictButton.setBackground(new Color(142, 68, 173));
        predictButton.setForeground(Color.WHITE);
        predictButton.setFocusPainted(false);
        predictButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        predictButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        predictButton.setToolTipText("Predict attendance after N more classes");

        actionPanel.add(deleteButton);
        actionPanel.add(clearButton);
        actionPanel.add(predictButton);

        JButton customPctButton = new JButton("Set Custom %");
        customPctButton.setBackground(new Color(211, 84, 0));
        customPctButton.setForeground(Color.WHITE);
        customPctButton.setFocusPainted(false);
        customPctButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        customPctButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        customPctButton.setToolTipText("Set custom required percentage for selected subject");
        actionPanel.add(customPctButton);

        overallAttendanceLabel = new JLabel("Overall Attendance: 0.00% (0 / 0)");
        overallAttendanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        overallAttendanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel statsLabel = new JLabel("Subjects: 0 | Highest: 0% | Lowest: 0% | Avg: 0%");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.add(overallAttendanceLabel, BorderLayout.NORTH);
        summaryPanel.add(statsLabel, BorderLayout.SOUTH);

        bottomPanel.add(actionPanel, BorderLayout.WEST);
        bottomPanel.add(summaryPanel, BorderLayout.EAST);

        // Main Layout wrapper
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainContent.add(inputPanel, BorderLayout.NORTH);
        mainContent.add(tableWrapper, BorderLayout.CENTER);
        mainContent.add(bottomPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        statusBar.setBackground(new Color(52, 152, 219));
        JLabel rowCountLabel = new JLabel("Rows: 0");
        rowCountLabel.setForeground(Color.WHITE);
        rowCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.add(rowCountLabel);
        JLabel statusTimeLabel = new JLabel();
        statusTimeLabel.setForeground(Color.WHITE);
        statusTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.add(statusTimeLabel);
        add(statusBar, BorderLayout.SOUTH);

        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, e -> {
            statusTimeLabel.setText("Date: " + java.time.LocalDate.now() + " | Time: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
        clockTimer.start();
        statusTimeLabel.setText("Date: " + java.time.LocalDate.now() + " | Time: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));

        // Action Listeners
        calculateButton.addActionListener(e -> calculateAndAdd());
        
        deleteButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this row?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                    tableModel.removeRow(modelRow);
                    updateOverallAttendance();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to delete.", "Delete Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        clearButton.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) return;
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all data?", "Confirm Clear", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tableModel.setRowCount(0);
                updateOverallAttendance();
            }
        });

        predictButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a subject row to predict.", "Predict", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String input = JOptionPane.showInputDialog(this, "Enter number of future classes to predict:", "10");
            if (input == null) return;
            try {
                int futureClasses = Integer.parseInt(input.trim());
                int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                int total = (int) tableModel.getValueAt(modelRow, 1);
                int attended = (int) tableModel.getValueAt(modelRow, 2);
                String subject = (String) tableModel.getValueAt(modelRow, 0);
                double predictedPct = ((double) (attended + futureClasses) / (total + futureClasses)) * 100;
                JOptionPane.showMessageDialog(this, String.format(
                    "Prediction for %s after %d more classes (attending all):\n\nCurrent: %d/%d (%.2f%%)\nPredicted: %d/%d (%.2f%%)",
                    subject, futureClasses, attended, total, ((double)attended/total)*100,
                    attended + futureClasses, total + futureClasses, predictedPct),
                    "Attendance Prediction", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Predict Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        customPctButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a subject row.", "Custom %", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String input = JOptionPane.showInputDialog(this, "Enter custom required percentage:", "75");
            if (input == null) return;
            try {
                double customPct = Double.parseDouble(input.trim());
                if (customPct < 0 || customPct > 100) {
                    JOptionPane.showMessageDialog(this, "Percentage must be between 0 and 100.", "Custom % Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                int total = (int) tableModel.getValueAt(modelRow, 1);
                int attended = (int) tableModel.getValueAt(modelRow, 2);
                double currentPct = ((double) attended / total) * 100;
                String status;
                if (currentPct >= customPct) {
                    int canMiss = (int) Math.floor(((double) attended * 100 / customPct) - total);
                    status = canMiss > 0 ? "Safe! You can miss " + canMiss + " classes." : "On track.";
                } else {
                    double r = customPct / 100.0;
                    double needed = (r * total - attended) / (1 - r);
                    int neededClasses = (int) Math.ceil(needed);
                    status = "Alert! Need " + neededClasses + " more classes.";
                }
                tableModel.setValueAt(String.format("%.0f%%", customPct), modelRow, 4);
                tableModel.setValueAt(status, modelRow, 5);
                updateOverallAttendance();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Custom % Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
        Color bg = dark ? new Color(43, 43, 43) : UIManager.getColor("Panel.background");
        Color fg = dark ? Color.WHITE : Color.BLACK;
        Color tableBg = dark ? new Color(55, 55, 55) : Color.WHITE;
        Color tableFg = dark ? new Color(200, 200, 200) : Color.BLACK;
        Color headerBg = dark ? new Color(60, 60, 60) : new Color(236, 240, 241);

        getContentPane().setBackground(bg);
        for (Component c : getContentPane().getComponents()) {
            c.setBackground(bg);
            c.setForeground(fg);
            if (c instanceof JPanel) {
                for (Component inner : ((JPanel) c).getComponents()) {
                    inner.setBackground(bg);
                    inner.setForeground(fg);
            }
        }
        rowCountLabel.setText("Rows: " + tableModel.getRowCount());
    }
        subjectTable.setBackground(tableBg);
        subjectTable.setForeground(tableFg);
        subjectTable.getTableHeader().setBackground(headerBg);
        subjectTable.getTableHeader().setForeground(fg);
        subjectTable.setGridColor(dark ? new Color(70, 70, 70) : new Color(200, 200, 200));
        overallAttendanceLabel.setForeground(fg);
        repaint();
    }

    private void calculateAndAdd() {
        try {
            String subject = subjectField.getText().trim();
            if (subject.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a subject name.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int total = Integer.parseInt(totalClassesField.getText().trim());
            int attended = Integer.parseInt(attendedClassesField.getText().trim());
            double required = Double.parseDouble(requiredPercentageField.getText().trim());

            if (total <= 0 || attended < 0 || required < 0 || required > 100) {
                JOptionPane.showMessageDialog(this, "Please enter valid positive numbers. Percentage must be between 0 and 100.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (attended > total) {
                JOptionPane.showMessageDialog(this, "Attended classes cannot be greater than total classes.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double currentPercentage = ((double) attended / total) * 100;
            String currentPercentageStr = String.format("%.2f%%", currentPercentage);
            String status = "";

            if (currentPercentage >= required) {
                int canMiss = (int) Math.floor(((double) attended * 100 / required) - total);
                if (canMiss > 0) {
                    status = "Safe! You can miss " + canMiss + " upcoming classes.";
                } else {
                    status = "On track. Cannot miss the next class.";
                }
            } else {
                double r = required / 100.0;
                double needed = (r * total - attended) / (1 - r);
                int neededClasses = (int) Math.ceil(needed);
                status = "Alert! Need to attend " + neededClasses + " more classes.";
            }

            String trend;
            java.util.List<Double> history = attendanceHistory.getOrDefault(subject, new ArrayList<>());
            if (history.size() >= 2) {
                double lastPct = history.get(history.size() - 1);
                if (currentPercentage > lastPct) trend = "UP";
                else if (currentPercentage < lastPct) trend = "DOWN";
                else trend = "STABLE";
            } else {
                trend = "NEW";
            }
            history.add(currentPercentage);
            attendanceHistory.put(subject, history);

            Object[] row = {
                    subject,
                    total,
                    attended,
                    currentPercentageStr,
                    String.format("%.0f%%", required),
                    status,
                    trend
            };
            saveUndoState();
            tableModel.addRow(row);
            updateOverallAttendance();

            subjectField.setText("");
            totalClassesField.setText("");
            attendedClassesField.setText("");
            subjectField.requestFocus();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for classes/days and percentage.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateOverallAttendance() {
        int totalClassesAll = 0;
        int totalAttendedAll = 0;
        
        // Find statsLabel - it's the second label in summaryPanel
        JLabel statsLabel = null;
        if (bottomPanel.getComponent(1) instanceof JPanel) {
            JPanel summaryPanel = (JPanel) bottomPanel.getComponent(1);
            if (summaryPanel.getComponentCount() > 1 && summaryPanel.getComponent(1) instanceof JLabel) {
                statsLabel = (JLabel) summaryPanel.getComponent(1);
            }
        }

        double highestPct = 0, lowestPct = 100, totalPct = 0;
        int rowCount = tableModel.getRowCount();
        
        for (int i = 0; i < rowCount; i++) {
            totalClassesAll += (int) tableModel.getValueAt(i, 1);
            totalAttendedAll += (int) tableModel.getValueAt(i, 2);
            double pct = ((double) (int) tableModel.getValueAt(i, 2) / (int) tableModel.getValueAt(i, 1)) * 100;
            if (pct > highestPct) highestPct = pct;
            if (pct < lowestPct) lowestPct = pct;
            totalPct += pct;
        }

        if (totalClassesAll == 0) {
            overallAttendanceLabel.setText("Overall Attendance: 0.00% (0 / 0)");
            overallAttendanceLabel.setForeground(Color.BLACK);
            if (statsLabel != null) statsLabel.setText("Subjects: 0 | Highest: 0% | Lowest: 0% | Avg: 0%");
        } else {
            double overallPercent = ((double) totalAttendedAll / totalClassesAll) * 100;
            overallAttendanceLabel.setText(String.format("Overall Attendance: %.2f%% (%d / %d)", overallPercent, totalAttendedAll, totalClassesAll));
            double avgPct = totalPct / rowCount;
            if (statsLabel != null) {
                statsLabel.setText(String.format("Subjects: %d | Highest: %.2f%% | Lowest: %.2f%% | Avg: %.2f%%", rowCount, highestPct, lowestPct, avgPct));
            }
            
            double required = 75.0;
            try {
                required = Double.parseDouble(requiredPercentageField.getText().trim());
            } catch (Exception ignored) {}

            if (overallPercent < required) {
                overallAttendanceLabel.setForeground(new Color(192, 57, 43));
            } else {
                overallAttendanceLabel.setForeground(new Color(39, 174, 96));
            }
        }
    }

    private void printAttendanceTable() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to print.", "Print", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            subjectTable.print(JTable.PrintMode.FIT_WIDTH);
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveUndoState() {
        int rowCount = tableModel.getRowCount();
        int colCount = tableModel.getColumnCount();
        Object[][] state = new Object[rowCount][colCount];
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                state[i][j] = tableModel.getValueAt(i, j);
            }
        }
        undoStack.push(state);
        if (undoStack.size() > 20) undoStack.remove(0);
    }

    private void undoLastAction() {
        if (undoStack.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nothing to undo.", "Undo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[][] prevState = undoStack.pop();
        tableModel.setRowCount(0);
        for (Object[] row : prevState) {
            tableModel.addRow(row);
        }
        updateOverallAttendance();
    }

    private void saveData() {
        saveDataQuiet();
        JOptionPane.showMessageDialog(this, "Data saved successfully!", "Save", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveDataQuiet() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(databaseFile))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String subject = (String) tableModel.getValueAt(i, 0);
                int total = (int) tableModel.getValueAt(i, 1);
                int attended = (int) tableModel.getValueAt(i, 2);
                String currentPct = (String) tableModel.getValueAt(i, 3);
                String requiredPct = (String) tableModel.getValueAt(i, 4);
                String status = (String) tableModel.getValueAt(i, 5);
                pw.printf("%s,%d,%d,%s,%s,%s%n", escapeCsv(subject), total, attended, currentPct, requiredPct, escapeCsv(status));
            }
        } catch (IOException ex) {
            if ( SwingUtilities.getWindowAncestor(this) != null ) {
                JOptionPane.showMessageDialog(this, "Error saving data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String escapeCsv(String s) {
        if (s.contains(",")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private void loadData() {
        File file = new File(databaseFile);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "No saved data found.", "Load", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Loading will replace current data. Continue?", "Confirm Load", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        tableModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader(databaseFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> fields = parseCsvLine(line);
                if (fields.size() >= 6) {
                    tableModel.addRow(fields.toArray());
                }
            }
            updateOverallAttendance();
            JOptionPane.showMessageDialog(this, "Data loaded successfully!", "Load", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                if (inQuotes && sb.length() > 0 && sb.charAt(sb.length() - 1) == '"') {
                    sb.append('"');
                    inQuotes = false;
                } else if (!inQuotes) {
                    inQuotes = true;
                } else {
                    sb.append(c);
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result;
    }

    private void exportCSV() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Export", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export as CSV");
        fileChooser.setSelectedFile(new File("attendance_export.csv"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("Subject,Total,Attended,Current %,Required %,Status/Needed");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String subject = (String) tableModel.getValueAt(i, 0);
                    int total = (int) tableModel.getValueAt(i, 1);
                    int attended = (int) tableModel.getValueAt(i, 2);
                    String currentPct = (String) tableModel.getValueAt(i, 3);
                    String requiredPct = (String) tableModel.getValueAt(i, 4);
                    String status = (String) tableModel.getValueAt(i, 5);
                    pw.printf("%s,%d,%d,%s,%s,%s%n", escapeCsv(subject), total, attended, currentPct, requiredPct, escapeCsv(status));
                }
                JOptionPane.showMessageDialog(this, "Exported to: " + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import from CSV");
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                int imported = 0;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() >= 6) {
                        tableModel.addRow(fields.toArray());
                        imported++;
                    }
                }
                updateOverallAttendance();
                JOptionPane.showMessageDialog(this, "Imported " + imported + " subjects successfully!", "Import", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error importing data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Use modern system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            AttendanceCalculator app = new AttendanceCalculator();
            java.awt.Image icon = createAppIcon();
            if (icon != null) app.setIconImage(icon);
            app.setVisible(true);
            app.toFront();
            app.requestFocus();
            app.setState(java.awt.Frame.NORMAL);
        });
    }

    private static java.awt.Image createAppIcon() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(52, 152, 219));
        g2d.fillRoundRect(0, 0, 32, 32, 6, 6);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2d.drawString("A", 9, 24);
        g2d.dispose();
        return img;
    }
}
