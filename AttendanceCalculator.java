import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AttendanceCalculator extends JFrame {
    private JTextField subjectField;
    private JTextField totalClassesField;
    private JTextField attendedClassesField;
    private JTextField requiredPercentageField;
    private DefaultTableModel tableModel;
    private JTable subjectTable;

    public AttendanceCalculator() {
        setTitle("Attendance Calculator");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top Panel for Inputs
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        inputPanel.add(new JLabel("Subject Name:"));
        subjectField = new JTextField();
        inputPanel.add(subjectField);

        inputPanel.add(new JLabel("Total Days/Classes:"));
        totalClassesField = new JTextField();
        inputPanel.add(totalClassesField);

        inputPanel.add(new JLabel("Attended Days/Classes:"));
        attendedClassesField = new JTextField();
        inputPanel.add(attendedClassesField);

        inputPanel.add(new JLabel("Required Percentage (%):"));
        requiredPercentageField = new JTextField("75"); // Default 75%
        inputPanel.add(requiredPercentageField);

        JButton calculateButton = new JButton("Calculate & Add Subject");
        calculateButton.setBackground(new Color(60, 130, 250));
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFocusPainted(false);
        calculateButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        inputPanel.add(new JLabel("")); // Empty cell for alignment
        inputPanel.add(calculateButton);

        // Bottom Panel for Table
        String[] columns = {"Subject", "Total", "Attended", "Current %", "Required %", "Status / Needed"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        subjectTable = new JTable(tableModel);
        subjectTable.setRowHeight(25);
        subjectTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(subjectTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        // Add components to Frame
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Button Action Listener
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateAndAdd();
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
                // Calculate how many classes can be safely missed
                // (attended) / (total + miss) = required / 100
                // attended = (required / 100) * (total + miss)
                // (attended * 100 / required) = total + miss
                // miss = (attended * 100 / required) - total
                int canMiss = (int) Math.floor(((double) attended * 100 / required) - total);
                if (canMiss > 0) {
                    status = "Safe! You can miss " + canMiss + " upcoming classes.";
                } else {
                    status = "On track. Cannot miss the next class.";
                }
            } else {
                // Calculate how many consecutive classes needed to attend
                // (attended + needed) / (total + needed) = required / 100
                double r = required / 100.0;
                double needed = (r * total - attended) / (1 - r);
                int neededClasses = (int) Math.ceil(needed);
                status = "Alert! Need to attend " + neededClasses + " more classes.";
            }

            // Add data row to the table
            Object[] row = {
                    subject,
                    total,
                    attended,
                    currentPercentageStr,
                    String.format("%.0f%%", required),
                    status
            };
            tableModel.addRow(row);

            // Clear inputs for the next entry except required percentage
            subjectField.setText("");
            totalClassesField.setText("");
            attendedClassesField.setText("");
            subjectField.requestFocus();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for classes/days and percentage.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Set System Look and Feel for better native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AttendanceCalculator().setVisible(true);
            }
        });
    }
}
