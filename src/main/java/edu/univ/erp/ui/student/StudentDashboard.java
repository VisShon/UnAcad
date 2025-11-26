package edu.univ.erp.ui.student; 

import java.awt.*; 
import java.awt.Desktop; 

import java.io.File; 
import java.io.IOException; 

import java.util.List; 
import java.util.Map; 

import javax.swing.*; 
import javax.swing.border.EmptyBorder; 
import javax.swing.table.DefaultTableModel; 

import edu.univ.erp.api.common.APIResponse; 
import edu.univ.erp.api.reports.ReportAPI; 
import edu.univ.erp.api.student.StudentAPI; 
import edu.univ.erp.api.types.CourseRow; 
import edu.univ.erp.api.types.SectionRow; 

import edu.univ.erp.auth.session.UserSession; 

import edu.univ.erp.ui.components.NavigationBar; 
import edu.univ.erp.ui.components.SideBar; 
import edu.univ.erp.ui.components.UIComponents;

public class StudentDashboard extends JFrame {

    private JPanel mainPanel; 
    private CardLayout cardLayout; 
    private final DefaultTableModel catalogModel; 
    private final JTable catalogTable; 
    private final DefaultTableModel sectionModel; 
    private final JTable sectionTable; 
    private final DefaultTableModel enrollModel; 
    private final JTable enrollTable; 
    private final DefaultTableModel timetableModel; 
    private final JTable timetableTable; 
    private final DefaultTableModel gradesModel; 
    private final JTable gradesTable;

    public StudentDashboard() {
        setTitle(
            "Student Dashboard"
        );
        setSize(
            900,
            650
        );
        setLocationRelativeTo(
            null
        );
        setDefaultCloseOperation(
            JFrame.EXIT_ON_CLOSE
        );
        setLayout(
            new BorderLayout()
        );

        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        add(
            navBar,
            BorderLayout.NORTH
        );
        cardLayout = new CardLayout();
        mainPanel = new JPanel(
            cardLayout
        );
        
        add(
            mainPanel,
            BorderLayout.CENTER
        );

        // Sidebar
        List<String[]> entries = List.of(
            new String[]{"catalog", "📚  Catalog"},
            new String[]{"sections", "🗂️  Sections"},
            new String[]{"enrollments", "🗒  My Enrollments"},
            new String[]{"timetable", "🗓  Timetable"},
            new String[]{"grades", "🏆  Grades"}
        );

        SideBar sidebar = new SideBar(
            entries,
            (key, btn) -> switchTab(key),
            null,
            false
        );
        add(
            sidebar,
            BorderLayout.WEST
        );

        // -------- Catalog Panel --------
        catalogModel = new DefaultTableModel(
            new Object[]{"Course ID", "Code", "Title", "Credits"},
            0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        catalogTable = UIComponents.table(catalogModel);

        JButton refreshCatalog = UIComponents.primaryButton(
            "Refresh Catalog",
            UIComponents.PRIMARY_BG
        );
        JButton viewSections = UIComponents.primaryButton(
            "View Sections",
            UIComponents.SECONDARY_BG
        );

        JPanel catalogPanel = new JPanel(
            new BorderLayout()
        );
        catalogPanel.add(
            UIComponents.topActionBar(refreshCatalog, viewSections),
            BorderLayout.NORTH
        );
        catalogPanel.add(
            new JScrollPane(catalogTable),
            BorderLayout.CENTER
        );

        mainPanel.add(
            catalogPanel,
            "catalog"
        );

        // -------- Sections Panel --------
        sectionModel = new DefaultTableModel(
            new Object[]{"Section ID", "Instructor", "Day/Time", "Capacity", "Enrolled"},
            0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sectionTable = UIComponents.table(sectionModel);

        JButton registerBtn = UIComponents.primaryButton(
            "Register Section",
            UIComponents.PRIMARY_BG
        );
        JButton backToCatalog = UIComponents.primaryButton(
            "Back",
            UIComponents.SECONDARY_BG
        );

        JPanel sectionPanel = new JPanel(
            new BorderLayout()
        );
        sectionPanel.add(
            UIComponents.topActionBar(backToCatalog, registerBtn),
            BorderLayout.NORTH
        );
        sectionPanel.add(
            new JScrollPane(sectionTable),
            BorderLayout.CENTER
        );

        mainPanel.add(
            sectionPanel,
            "sections"
        );

        // -------- Enrollments Panel --------
        enrollModel = new DefaultTableModel(
            new Object[]{"Enrollment ID", "Section ID", "Course", "Day/Time", "Room", "Instructor"},
            0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        enrollTable = UIComponents.table(enrollModel);

        JButton dropBtn = UIComponents.primaryButton(
            "Drop Enrollment",
            UIComponents.PRIMARY_BG
        );

        JPanel enrollPanel = new JPanel(
            new BorderLayout()
        );
        enrollPanel.add(
            UIComponents.topActionBar(dropBtn),
            BorderLayout.NORTH
        );
        enrollPanel.add(
            new JScrollPane(enrollTable),
            BorderLayout.CENTER
        );

        mainPanel.add(
            enrollPanel,
            "enrollments"
        );

        // -------- Timetable Panel --------
        timetableModel = new DefaultTableModel(
            new Object[]{"Course Code", "Day/Time", "Room", "Instructor"},
            0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        timetableTable = UIComponents.table(timetableModel);

        JButton refreshTimetableBtn = UIComponents.primaryButton(
            "Refresh Timetable",
            UIComponents.PRIMARY_BG
        );

        JPanel timetablePanel = new JPanel(
            new BorderLayout()
        );
        timetablePanel.add(
            UIComponents.topActionBar(refreshTimetableBtn),
            BorderLayout.NORTH
        );
        timetablePanel.add(
            new JScrollPane(timetableTable),
            BorderLayout.CENTER
        );

        mainPanel.add(
            timetablePanel,
            "timetable"
        );

        // -------- Grades Panel --------
        gradesModel = new DefaultTableModel(
            new Object[]{"Course", "Component", "Score", "Final Grade"},
            0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        gradesTable = UIComponents.table(gradesModel);

        JButton downloadTranscriptBtn = UIComponents.primaryButton(
            "Download Transcript (PDF)",
            UIComponents.PRIMARY_BG
        );

        JPanel gradesPanel = new JPanel(
            new BorderLayout()
        );
        gradesPanel.add(
            UIComponents.topActionBar(downloadTranscriptBtn),
            BorderLayout.NORTH
        );
        gradesPanel.add(
            new JScrollPane(gradesTable),
            BorderLayout.CENTER
        );

        mainPanel.add(
            gradesPanel,
            "grades"
        );

        // Events
        refreshCatalog.addActionListener(e -> loadCatalog());
        viewSections.addActionListener(e -> {
            int sel = catalogTable.getSelectedRow();
            if (sel == -1) { JOptionPane.showMessageDialog(this, "Select a course first"); return; }
            int courseId = (int) catalogModel.getValueAt(sel, 0);
            loadSections(courseId);
            switchTab("sections");
        });
        backToCatalog.addActionListener(e -> switchTab("catalog"));
        refreshTimetableBtn.addActionListener(e -> loadTimetable());
        registerBtn.addActionListener(e -> {
            int sel = sectionTable.getSelectedRow();
            if (sel == -1) { JOptionPane.showMessageDialog(this, "Select a section"); return; }
            int sectionId = (int) sectionModel.getValueAt(sel, 0);
            APIResponse<Void> r = StudentAPI.registerSection(sectionId);
            if (r.success) {
                JOptionPane.showMessageDialog(this, r.message);
                loadEnrollments();
                int courseId = (int) catalogModel.getValueAt(catalogTable.getSelectedRow(), 0);
                loadSections(courseId);  // Safe now if selection exists
            } else {
                JOptionPane.showMessageDialog(this, r.message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dropBtn.addActionListener(e -> {
            int sel = enrollTable.getSelectedRow();
            if (sel == -1) { JOptionPane.showMessageDialog(this, "Select an enrollment to drop"); return; }
            int sectionId = (int) enrollModel.getValueAt(sel, 1);
            APIResponse<Void> r = StudentAPI.dropSection(sectionId);
            if (r.success) {
                JOptionPane.showMessageDialog(this, r.message);
                loadEnrollments();
            } else {
                JOptionPane.showMessageDialog(this, r.message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        downloadTranscriptBtn.addActionListener(e -> {
            int studentId = UserSession.getUserId();
            if (studentId <= 0) {
                JOptionPane.showMessageDialog(this, "Not logged in", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                APIResponse<String> r = ReportAPI.transcriptPdf(studentId);
                if (r.success && r.data != null) {
                    String filePath = r.data;
                    File file = new File(filePath);
                    if (file.exists()) {
                        // Open file with default application
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(file);
                        }
                        JOptionPane.showMessageDialog(this, 
                                "Transcript generated successfully!\nFile: " + filePath,
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                                "Transcript generated but file not found: " + filePath,
                                "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, r.message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | SecurityException ex) {
                JOptionPane.showMessageDialog(this, "Failed to generate transcript: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Load initial data
        loadCatalog();
        loadEnrollments();
        loadTimetable();
        loadGrades();
    }

    private void switchTab(String key) { 
        cardLayout.show(mainPanel, key); 
    }

    private void loadCatalog() {
        APIResponse<List<Map<String, Object>>> r = StudentAPI.listCatalog();
        catalogModel.setRowCount(0);
        if (!r.success) { JOptionPane.showMessageDialog(this, r.message); return; }
        @SuppressWarnings("unchecked")
        List<CourseRow> rows = (List<CourseRow>) (List<?>) r.data;
        for (CourseRow cr : rows) {
            catalogModel.addRow(new Object[]{cr.courseId, cr.code, cr.title, cr.credits});
        }
    }

    private void loadSections(int courseId) {
        APIResponse<List<Map<String, Object>>> r = StudentAPI.listSections(courseId);
        sectionModel.setRowCount(0);
        if (!r.success) { JOptionPane.showMessageDialog(this, r.message); return; }
        @SuppressWarnings("unchecked")
        List<SectionRow> rows = (List<SectionRow>) (List<?>) r.data;
        for (SectionRow sr : rows) {
            sectionModel.addRow(new Object[]{sr.sectionId, sr.instructorName, sr.dayTime, sr.capacity, sr.enrolled});
        }
    }

    private void loadEnrollments() {
        APIResponse<List<Map<String, Object>>> r = StudentAPI.myEnrollments();
        enrollModel.setRowCount(0);
        if (!r.success) { JOptionPane.showMessageDialog(this, r.message); return; }
        List<Map<String,Object>> rows = r.data;
        for (Map<String,Object> m : rows) {
            enrollModel.addRow(new Object[]{
                    m.get("enrollment_id"),
                    m.get("section_id"),
                    m.get("code") + " - " + m.get("title"),
                    m.get("day_time"),
                    m.get("room"),
                    m.get("instructor")
            });
        }
    }

    private void loadTimetable() {
        APIResponse<List<Map<String, Object>>> r = StudentAPI.myTimetable();
        timetableModel.setRowCount(0);
        if (!r.success) { 
            // Don't show error dialog for timetable, just return silently
            return; 
        }
        List<Map<String,Object>> rows = r.data;
        for (Map<String,Object> m : rows) {
            timetableModel.addRow(new Object[]{
                    m.get("code"),
                    m.get("day_time"),
                    m.get("room"),
                    m.get("instructor")
            });
        }
    }

    private void loadGrades() {
        APIResponse<List<Map<String, Object>>> r = StudentAPI.myGrades();
        gradesModel.setRowCount(0);
        if (!r.success) { JOptionPane.showMessageDialog(this, r.message); return; }
        List<Map<String,Object>> rows = r.data;
        for (Map<String,Object> m : rows) {
            gradesModel.addRow(new Object[]{
                    m.get("code"),
                    m.get("component"),
                    m.get("score"),
                    m.get("final_grade")
            });
        }
    }
}