package edu.univ.erp.ui.instructor;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.api.instructor.InstructorAPI;
import edu.univ.erp.ui.components.NavigationBar;
import edu.univ.erp.ui.components.SideBar;
import edu.univ.erp.ui.components.UIComponents;
import edu.univ.erp.util.SearchFilter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;

public class InstructorDashboard extends JFrame {

    private JPanel mainPanel;
    private CardLayout cardLayout;
    private NavigationBar navBar;
    private SideBar sidebar;

    private final DefaultTableModel sectionsModel;
    private final JTable sectionsTable;

    public InstructorDashboard() {
        setTitle("Instructor Dashboard");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        navBar = new NavigationBar();
        add(navBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel, BorderLayout.CENTER);

        List<String[]> entries = new ArrayList<>();
        entries.add(new String[]{"sections", "🗂️  Sections"});

        sidebar = new SideBar(
                entries,
                (key, btn) -> switchTab(key),
                null,
                false
        );
        add(sidebar, BorderLayout.WEST);

        sectionsModel = new DefaultTableModel(
                new Object[]{"Section ID", "Course", "Enrolled"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        sectionsTable = UIComponents.table(sectionsModel);

        JButton refreshBtn = UIComponents.primaryButton("Refresh Sections", UIComponents.PRIMARY_BG);
        JButton enterGradesBtn = UIComponents.primaryButton("Enter Grades", UIComponents.SECONDARY_BG);
        JButton exportCsvBtn = UIComponents.primaryButton("Export Grades", UIComponents.SECONDARY_BG);

        JPanel sectionsPanel = new JPanel(new BorderLayout());
        JPanel sectionsTopBar = new JPanel(new BorderLayout());
        sectionsTopBar.setBackground(Color.WHITE);

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftButtons.setBackground(Color.WHITE);
        leftButtons.add(refreshBtn);
        leftButtons.add(enterGradesBtn);
        leftButtons.add(exportCsvBtn);

        sectionsTopBar.add(leftButtons, BorderLayout.WEST);

        JTextField sectionsSearchField = UIComponents.searchField();
        sectionsTopBar.add(sectionsSearchField, BorderLayout.EAST);

        sectionsPanel.add(sectionsTopBar, BorderLayout.NORTH);
        sectionsPanel.add(new JScrollPane(sectionsTable), BorderLayout.CENTER);

        mainPanel.add(sectionsPanel, "sections");

    // Events
        SearchFilter.attachSearchFilter(sectionsSearchField, sectionsTable, sectionsModel);
        refreshBtn.addActionListener(e -> loadSections());
        enterGradesBtn.addActionListener(e -> {
            int sectionId = getSelectedSectionId();
            if (sectionId == -1) return;
            String courseInfo = getSelectedCourseInfo();
            new GradeEntryDialog(this, sectionId, courseInfo).setVisible(true);
        });

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

    private void switchTab(String key) { 
        cardLayout.show(mainPanel, key); 
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