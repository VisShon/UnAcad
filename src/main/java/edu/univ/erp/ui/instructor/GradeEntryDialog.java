package edu.univ.erp.ui.instructor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.api.instructor.InstructorAPI;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.GradesDAO;
import net.miginfocom.swing.MigLayout;

public class GradeEntryDialog extends JDialog {

    private final int sectionId;
    private final DefaultTableModel studentsModel;
    private final JTable studentsTable;
    private final Map<Integer, Map<String, JTextField>> scoreFields; // enrollmentId -> component -> field
    private final JTextField quizWeightField;
    private final JTextField midtermWeightField;
    private final JTextField finalWeightField;

    public GradeEntryDialog(JFrame parent, int sectionId, String sectionInfo) {
        super(parent, "Enter Grades - " + sectionInfo, true);
        this.sectionId = sectionId;
        this.scoreFields = new HashMap<>();

        setSize(900, 600);
        setLocationRelativeTo(parent);

        // Students table
        studentsModel = new DefaultTableModel(new Object[]{"Enrollment ID", "Student", "Quiz", "Midterm", "Final", "Final Grade"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c >= 2 && c <= 4; // Only score columns are editable
            }
        };
        studentsTable = new JTable(studentsModel);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentsTable.setAutoCreateRowSorter(true);
        JScrollPane studentsScroll = new JScrollPane(studentsTable);

        // Weight configuration panel
        JPanel weightPanel = new JPanel(new MigLayout("wrap 2", "[][100]", "[]5[]5[]"));
        weightPanel.setBorder(BorderFactory.createTitledBorder("Grade Weights (must sum to 100%)"));
        
        weightPanel.add(new JLabel("Quiz Weight (%):"));
        quizWeightField = new JTextField("20", 10);
        weightPanel.add(quizWeightField);
        
        weightPanel.add(new JLabel("Midterm Weight (%):"));
        midtermWeightField = new JTextField("30", 10);
        weightPanel.add(midtermWeightField);
        
        weightPanel.add(new JLabel("Final Weight (%):"));
        finalWeightField = new JTextField("50", 10);
        weightPanel.add(finalWeightField);

        // Buttons
        JButton saveBtn = new JButton("Save Scores");
        JButton computeFinalBtn = new JButton("Compute Final Grades");
        JButton viewStatsBtn = new JButton("View Class Statistics");
        JButton closeBtn = new JButton("Close");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveBtn);
        buttonPanel.add(computeFinalBtn);
        buttonPanel.add(viewStatsBtn);
        buttonPanel.add(closeBtn);

        // Layout
        setLayout(new BorderLayout());
        add(weightPanel, BorderLayout.NORTH);
        add(studentsScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event handlers
        saveBtn.addActionListener(e -> saveScores());
        computeFinalBtn.addActionListener(e -> computeFinalGrades());
        viewStatsBtn.addActionListener(e -> showClassStats());
        closeBtn.addActionListener(e -> dispose());

        // Load students
        loadStudents();
    }

    private void loadStudents() {
        try {
            List<Map<String, Object>> enrollments = EnrollmentDAO.listEnrollmentsForSection(sectionId);
            studentsModel.setRowCount(0);
            scoreFields.clear();

            for (Map<String, Object> enrollment : enrollments) {
                int enrollmentId = (int) enrollment.get("enrollment_id");
                String studentName = (String) enrollment.get("name");

                // Get existing grades
                List<Map<String, Object>> grades = GradesDAO.getGradesForEnrollment(enrollmentId);
                Map<String, Double> existingScores = new HashMap<>();
                String finalGrade = null;
                
                for (Map<String, Object> grade : grades) {
                    String component = (String) grade.get("component");
                    Object scoreObj = grade.get("score");
                    if (scoreObj != null) {
                        existingScores.put(component.toLowerCase(), ((Number) scoreObj).doubleValue());
                    }
                    if (grade.get("final_grade") != null) {
                        finalGrade = (String) grade.get("final_grade");
                    }
                }

                // Add row with scores
                Object[] row = new Object[6];
                row[0] = enrollmentId;
                row[1] = studentName;
                row[2] = existingScores.getOrDefault("quiz", 0.0);
                row[3] = existingScores.getOrDefault("midterm", 0.0);
                row[4] = existingScores.getOrDefault("final", 0.0);
                row[5] = finalGrade != null ? finalGrade : "-";
                
                studentsModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load students: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveScores() {
        int saved = 0;
        int errors = 0;
        StringBuilder errorMessages = new StringBuilder();

        for (int row = 0; row < studentsModel.getRowCount(); row++) {
            try {
                int enrollmentId = (int) studentsModel.getValueAt(row, 0);
                String studentName = (String) studentsModel.getValueAt(row, 1);
                
                // Get scores from table
                Object quizObj = studentsModel.getValueAt(row, 2);
                Object midtermObj = studentsModel.getValueAt(row, 3);
                Object finalObj = studentsModel.getValueAt(row, 4);

                // Save Quiz
                if (quizObj != null && !quizObj.toString().trim().isEmpty()) {
                    try {
                        double quizScore = Double.parseDouble(quizObj.toString().trim());
                        if (quizScore < 0 || quizScore > 100) {
                            errorMessages.append(String.format("Invalid Quiz score for %s (must be 0-100)\n", studentName));
                            errors++;
                        } else {
                            APIResponse<Void> r = InstructorAPI.enterScore(enrollmentId, "Quiz", quizScore);
                            if (r.success) saved++;
                            else {
                                errors++;
                                errorMessages.append(String.format("%s: %s\n", studentName, r.message));
                            }
                        }
                    } catch (NumberFormatException e) {
                        errors++;
                        errorMessages.append(String.format("Invalid Quiz score format for %s\n", studentName));
                    }
                }

                // Save Midterm
                if (midtermObj != null && !midtermObj.toString().trim().isEmpty()) {
                    try {
                        double midtermScore = Double.parseDouble(midtermObj.toString().trim());
                        if (midtermScore < 0 || midtermScore > 100) {
                            errorMessages.append(String.format("Invalid Midterm score for %s (must be 0-100)\n", studentName));
                            errors++;
                        } else {
                            APIResponse<Void> r = InstructorAPI.enterScore(enrollmentId, "Midterm", midtermScore);
                            if (r.success) saved++;
                            else {
                                errors++;
                                errorMessages.append(String.format("%s: %s\n", studentName, r.message));
                            }
                        }
                    } catch (NumberFormatException e) {
                        errors++;
                        errorMessages.append(String.format("Invalid Midterm score format for %s\n", studentName));
                    }
                }

                // Save Final
                if (finalObj != null && !finalObj.toString().trim().isEmpty()) {
                    try {
                        double finalScore = Double.parseDouble(finalObj.toString().trim());
                        if (finalScore < 0 || finalScore > 100) {
                            errorMessages.append(String.format("Invalid Final score for %s (must be 0-100)\n", studentName));
                            errors++;
                        } else {
                            APIResponse<Void> r = InstructorAPI.enterScore(enrollmentId, "Final", finalScore);
                            if (r.success) saved++;
                            else {
                                errors++;
                                errorMessages.append(String.format("%s: %s\n", studentName, r.message));
                            }
                        }
                    } catch (NumberFormatException e) {
                        errors++;
                        errorMessages.append(String.format("Invalid Final score format for %s\n", studentName));
                    }
                }
            } catch (Exception e) {
                errors++;
                errorMessages.append(String.format("Error processing row %d: %s\n", row + 1, e.getMessage()));
            }
        }

        String message;
        if (saved > 0 && errors == 0) {
            message = String.format("Successfully saved %d score(s).", saved);
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
        } else if (saved > 0 && errors > 0) {
            message = String.format("Saved %d score(s). %d error(s) occurred:\n\n%s", saved, errors, errorMessages.toString());
            JOptionPane.showMessageDialog(this, message, "Partial Success", JOptionPane.WARNING_MESSAGE);
        } else {
            message = String.format("Failed to save scores. Errors:\n\n%s", errorMessages.toString());
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void computeFinalGrades() {
        // Validate weights
        double quizWeight, midtermWeight, finalWeight;
        try {
            quizWeight = Double.parseDouble(quizWeightField.getText().trim()) / 100.0;
            midtermWeight = Double.parseDouble(midtermWeightField.getText().trim()) / 100.0;
            finalWeight = Double.parseDouble(finalWeightField.getText().trim()) / 100.0;
            
            double sum = quizWeight + midtermWeight + finalWeight;
            if (Math.abs(sum - 1.0) > 0.01) {
                JOptionPane.showMessageDialog(this, 
                    "Weights must sum to 100%. Current sum: " + (sum * 100) + "%",
                    "Invalid Weights", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid weight format. Please enter numeric values.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int computed = 0;
        int errors = 0;
        StringBuilder errorMessages = new StringBuilder();

        for (int row = 0; row < studentsModel.getRowCount(); row++) {
            int enrollmentId = (int) studentsModel.getValueAt(row, 0);
            String studentName = (String) studentsModel.getValueAt(row, 1);
            
            try {
                // Get scores from table
                Object quizObj = studentsModel.getValueAt(row, 2);
                Object midtermObj = studentsModel.getValueAt(row, 3);
                Object finalObj = studentsModel.getValueAt(row, 4);
                
                double quizScore = (quizObj != null && !quizObj.toString().isEmpty()) ? 
                    Double.parseDouble(quizObj.toString()) : 0.0;
                double midtermScore = (midtermObj != null && !midtermObj.toString().isEmpty()) ? 
                    Double.parseDouble(midtermObj.toString()) : 0.0;
                double finalScore = (finalObj != null && !finalObj.toString().isEmpty()) ? 
                    Double.parseDouble(finalObj.toString()) : 0.0;
                
                // Calculate weighted total
                double total = (quizScore * quizWeight) + (midtermScore * midtermWeight) + (finalScore * finalWeight);
                
                // Determine letter grade
                String letterGrade = (total >= 90) ? "A" :
                                   (total >= 80) ? "B" :
                                   (total >= 70) ? "C" :
                                   (total >= 60) ? "D" : "F";
                
                // Update database via API
                APIResponse<Void> r = InstructorAPI.computeFinalGrade(enrollmentId);
                if (r.success) {
                    studentsModel.setValueAt(letterGrade, row, 5);
                    computed++;
                } else {
                    errors++;
                    errorMessages.append(String.format("%s: %s\n", studentName, r.message));
                }
            } catch (Exception e) {
                errors++;
                errorMessages.append(String.format("%s: %s\n", studentName, e.getMessage()));
            }
        }

        String message;
        if (computed > 0 && errors == 0) {
            message = String.format("Successfully computed final grades for %d students.", computed);
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
        } else if (computed > 0 && errors > 0) {
            message = String.format("Computed grades for %d students. %d error(s):\n\n%s", 
                computed, errors, errorMessages.toString());
            JOptionPane.showMessageDialog(this, message, "Partial Success", JOptionPane.WARNING_MESSAGE);
        } else {
            message = String.format("Failed to compute grades. Errors:\n\n%s", errorMessages.toString());
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showClassStats() {
        try {
            APIResponse<Map<String, Object>> r = InstructorAPI.getClassStats(sectionId);
            if (r.success && r.data != null) {
                Map<String, Object> stats = r.data;
                double avg = ((Number) stats.get("avg")).doubleValue();
                double min = ((Number) stats.get("min")).doubleValue();
                double max = ((Number) stats.get("max")).doubleValue();

                String message = """
                        Class Statistics:
                        
                        Average Score: %.2f
                        Minimum Score: %.2f
                        Maximum Score: %.2f
                        """.formatted(avg, min, max);

                JOptionPane.showMessageDialog(this, message, "Class Statistics", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No statistics available. Enter some scores first.",
                        "No Data", JOptionPane.WARNING_MESSAGE);
            }
        } catch (ClassCastException | NullPointerException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Failed to load statistics: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

