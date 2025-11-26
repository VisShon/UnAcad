package edu.univ.erp.ui.instructor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.api.instructor.InstructorAPI;
import edu.univ.erp.ui.common.NavigationBar;

public class InstructorDashboard extends JFrame {

    private final DefaultTableModel sectionsModel;
    private final JTable sectionsTable;

    public InstructorDashboard() {
        setTitle("Instructor Dashboard");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add NavigationBar (includes logout)
        NavigationBar navBar = new NavigationBar();

        sectionsModel = new DefaultTableModel(new Object[]{"Section ID", "Course", "Enrolled"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        sectionsTable = new JTable(sectionsModel);
        sectionsTable.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(sectionsTable);

        JButton refreshBtn = new JButton("Refresh Sections");
        JButton enterGradesBtn = new JButton("Enter Grades for Selected");
        JButton exportCsvBtn = new JButton("Export Grades CSV");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(refreshBtn);
        topPanel.add(enterGradesBtn);
        topPanel.add(exportCsvBtn);

        // Layout: NavigationBar at top, content below
        setLayout(new BorderLayout());
        add(navBar, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(scroll, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> loadSections());
        enterGradesBtn.addActionListener(e -> {
            int sectionId = getSelectedSectionId();
            if (sectionId == -1) return;
            String courseInfo = getSelectedCourseInfo();
            new GradeEntryDialog(this, sectionId, courseInfo).setVisible(true);
        });

        // Add action listener for CSV export
        exportCsvBtn.addActionListener(e -> {
            int sectionId = getSelectedSectionId();
            if (sectionId == -1) return;
            APIResponse<List<Map<String, Object>>> r = InstructorAPI.getSectionGrades(sectionId);
            if (!r.success) {
                JOptionPane.showMessageDialog(this, r.message);
                return;
            }
            List<Map<String, Object>> grades = r.data;

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("grades_section_" + sectionId + ".csv"));
            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println("Student ID,Name,Course,Component,Score,Final Grade");
                    for (Map<String, Object> g : grades) {
                        String line = Objects.toString(g.get("student_id"), "") + "," +
                                Objects.toString(g.get("name"), "") + "," +
                                Objects.toString(g.get("course"), "") + "," +
                                Objects.toString(g.get("component"), "") + "," +
                                Objects.toString(g.get("score"), "") + "," +
                                Objects.toString(g.get("final_grade"), "");
                        writer.println(line);
                    }
                    JOptionPane.showMessageDialog(this, "Grades exported successfully to " + file.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage());
                }
            }
        });

        loadSections();
    }

    private int getSelectedSectionId() {
        int sel = sectionsTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section first.");
            return -1;
        }
        return (int) sectionsModel.getValueAt(sel, 0);
    }

    private String getSelectedCourseInfo() {
        int sel = sectionsTable.getSelectedRow();
        if (sel == -1) return null;
        return (String) sectionsModel.getValueAt(sel, 1);
    }

    private void loadSections() {
        APIResponse<List<Map<String, Object>>> r = InstructorAPI.mySections();
        sectionsModel.setRowCount(0);
        if (!r.success) {
            JOptionPane.showMessageDialog(this, r.message);
            return;
        }
        List<Map<String, Object>> sections = r.data;
        for (Map<String, Object> s : sections) {
            sectionsModel.addRow(new Object[]{s.get("section_id"), s.get("code") + " - " + s.get("title"), s.get("enrolled")});
        }
    }
}