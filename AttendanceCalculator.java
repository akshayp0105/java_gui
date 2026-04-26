import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AttendanceCalculator extends JFrame {
    private JTextField subjectField;
    private JTextField totalClassesField;
    private JTextField attendedClassesField;
    private JTextField requiredPercentageField;
    private DefaultTableModel tableModel;
    private JTable subjectTable;
    private JLabel overallAttendanceLabel;

    public AttendanceCalculator() {
        setTitle("Attendance Calculator Pro");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        JLabel titleLabel = new JLabel("Attendance Calculator & Tracker");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        headerPanel.add(titleLabel);

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
        gbc.gridx = 1; gbc.gridy = 0; inputPanel.add(subjectField, gbc);

        JLabel totLabel = new JLabel("Total Classes:");
        totLabel.setFont(labelFont);
        gbc.gridx = 2; gbc.gridy = 0; inputPanel.add(totLabel, gbc);
        totalClassesField = new JTextField(10);
        totalClassesField.setFont(fieldFont);
        gbc.gridx = 3; gbc.gridy = 0; inputPanel.add(totalClassesField, gbc);

        JLabel attLabel = new JLabel("Attended Classes:");
        attLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(attLabel, gbc);
        attendedClassesField = new JTextField(15);
        attendedClassesField.setFont(fieldFont);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(attendedClassesField, gbc);

        JLabel reqLabel = new JLabel("Required %:");
        reqLabel.setFont(labelFont);
        gbc.gridx = 2; gbc.gridy = 1; inputPanel.add(reqLabel, gbc);
        requiredPercentageField = new JTextField("75", 10);
        requiredPercentageField.setFont(fieldFont);
        gbc.gridx = 3; gbc.gridy = 1; inputPanel.add(requiredPercentageField, gbc);

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

        bottomPanel.add(actionPanel, BorderLayout.WEST);
        bottomPanel.add(overallAttendanceLabel, BorderLayout.EAST);

        // Main Layout wrapper
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainContent.add(inputPanel, BorderLayout.NORTH);
        mainContent.add(scrollPane, BorderLayout.CENTER);
        mainContent.add(bottomPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);

        // Action Listeners
        calculateButton.addActionListener(e -> calculateAndAdd());
        
        deleteButton.addActionListener(e -> {
            int selectedRow = subjectTable.getSelectedRow();
            if (selectedRow != -1) {
                tableModel.removeRow(selectedRow);
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
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            totalClassesAll += (int) tableModel.getValueAt(i, 1);
            totalAttendedAll += (int) tableModel.getValueAt(i, 2);
        }

        if (totalClassesAll == 0) {
            overallAttendanceLabel.setText("Overall Attendance: 0.00% (0 / 0)");
            overallAttendanceLabel.setForeground(Color.BLACK);
        } else {
            double overallPercent = ((double) totalAttendedAll / totalClassesAll) * 100;
            overallAttendanceLabel.setText(String.format("Overall Attendance: %.2f%% (%d / %d)", overallPercent, totalAttendedAll, totalClassesAll));
            
            double required = 75.0; // Default fallback
            try {
                required = Double.parseDouble(requiredPercentageField.getText().trim());
            } catch (Exception ignored) {}

            if (overallPercent < required) {
                overallAttendanceLabel.setForeground(new Color(192, 57, 43)); // Red
            } else {
                overallAttendanceLabel.setForeground(new Color(39, 174, 96)); // Green
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
