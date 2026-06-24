import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    public AttendanceCalculator() {
        setTitle("Attendance Calculator Pro v" + APP_VERSION);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        JLabel titleLabel = new JLabel("Attendance Calculator Pro");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
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
        helpContentMenu.addActionListener(e -> JOptionPane.showMessageDialog(this, "1. Enter subject name, total classes, attended classes, and required percentage.\n2. Click Calculate to add to the table.\n3. Status column shows if you're safe or need more classes.\n4. Use File menu to save/load or export data.", "Help", JOptionPane.INFORMATION_MESSAGE));
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

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (autoSave && tableModel.getRowCount() > 0) {
                    saveDataQuiet();
                }
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
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; 
        gbc.insets = new Insets(15, 5, 5, 5);
        inputPanel.add(calculateButton, gbc);

        // Center Panel for Table
        String[] columns = {"Subject", "Total", "Attended", "Current %", "Required %", "Status / Needed"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        subjectTable = new JTable(tableModel);
        subjectTable.setRowHeight(32);
        subjectTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subjectTable.setRowSorter(new javax.swing.table.TableRowSorter<>(tableModel));
        subjectTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        subjectTable.getTableHeader().setBackground(new Color(236, 240, 241));
        subjectTable.setSelectionBackground(new Color(189, 195, 199));

        // Custom renderer for row colors
        subjectTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    String status = (String) table.getModel().getValueAt(row, 5);
                    if (status.startsWith("Alert")) {
                        c.setBackground(new Color(255, 230, 230)); // Light red
                    } else {
                        c.setBackground(new Color(230, 255, 230)); // Light green
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(subjectTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Subject Details", 0, 0, new Font("Segoe UI", Font.BOLD, 12)));

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
        
        JButton clearButton = new JButton("Clear All");
        clearButton.setBackground(new Color(149, 165, 166));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        actionPanel.add(deleteButton);
        actionPanel.add(clearButton);

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

        // Action Listeners
        calculateButton.addActionListener(e -> calculateAndAdd());
        
        deleteButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = subjectTable.convertRowIndexToModel(selectedRow);
                tableModel.removeRow(modelRow);
                updateOverallAttendance();
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
    }

    private void applyDarkMode(boolean dark) {
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

            Object[] row = {
                    subject,
                    total,
                    attended,
                    currentPercentageStr,
                    String.format("%.0f%%", required),
                    status
            };
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
            new AttendanceCalculator().setVisible(true);
        });
    }
}
