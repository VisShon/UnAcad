package edu.univ.erp.ui.student;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.api.reports.ReportAPI;
import edu.univ.erp.api.student.StudentAPI;
import edu.univ.erp.api.types.CourseRow;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.auth.session.UserSession;
import edu.univ.erp.ui.common.NavigationBar;

public class StudentDashboard extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();

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
        setTitle("Student Dashboard");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Navigation bar
        NavigationBar navBar = new NavigationBar();

        // Use BorderLayout to place navBar at top and tabs in center
        setLayout(new BorderLayout());
        add(navBar, BorderLayout.NORTH);

        // --- Catalog Tab ---
        catalogModel = new DefaultTableModel(new Object[]{"Course ID","Code","Title","Credits"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c){ return false; }
        };
        catalogTable = new JTable(catalogModel);
        catalogTable.setAutoCreateRowSorter(true);
        JScrollPane catalogScroll = new JScrollPane(catalogTable);

        JButton refreshCatalog = new JButton("Refresh Catalog");
        JButton viewSections = new JButton("View Sections for Selected Course");

        JPanel catalogPanel = new JPanel(new BorderLayout());
        JPanel catalogTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        catalogTop.add(refreshCatalog);
        catalogTop.add(viewSections);
        catalogPanel.add(catalogTop, BorderLayout.NORTH);
        catalogPanel.add(catalogScroll, BorderLayout.CENTER);

        tabs.addTab("Catalog", catalogPanel);

        // --- Sections Tab (for selected course) ---
        sectionModel = new DefaultTableModel(new Object[]{"Section ID","Instructor","Day/Time","Capacity","Enrolled"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c){ return false; }
        };
        sectionTable = new JTable(sectionModel);
        sectionTable.setAutoCreateRowSorter(true);
        JScrollPane sectionScroll = new JScrollPane(sectionTable);
        JButton registerBtn = new JButton("Register Selected Section");
        JButton backToCatalog = new JButton("Back to Catalog");

        JPanel sectionPanel = new JPanel(new BorderLayout());
        JPanel sectionTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sectionTop.add(backToCatalog);
        sectionTop.add(registerBtn);
        sectionPanel.add(sectionTop, BorderLayout.NORTH);
        sectionPanel.add(sectionScroll, BorderLayout.CENTER);

        tabs.addTab("Sections", sectionPanel);

        // --- Enrollments / Timetable Tab ---
        enrollModel = new DefaultTableModel(new Object[]{"Enrollment ID","Section ID","Course","Day/Time","Room","Instructor"},0) {
            @Override
            public boolean isCellEditable(int r, int c){ return false; }
        };
        enrollTable = new JTable(enrollModel);
        enrollTable.setAutoCreateRowSorter(true);
        JScrollPane enrollScroll = new JScrollPane(enrollTable);
        JButton dropBtn = new JButton("Drop Selected Enrollment");
        JPanel enrollPanel = new JPanel(new BorderLayout());
        JPanel enrollTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        enrollTop.add(dropBtn);
        enrollPanel.add(enrollTop, BorderLayout.NORTH);
        enrollPanel.add(enrollScroll, BorderLayout.CENTER);

        tabs.addTab("My Enrollments", enrollPanel);

        // --- Timetable Tab ---
        timetableModel = new DefaultTableModel(new Object[]{"Course Code","Day/Time","Room","Instructor"},0) {
            @Override
            public boolean isCellEditable(int r, int c){ return false; }
        };
        timetableTable = new JTable(timetableModel);
        timetableTable.setAutoCreateRowSorter(true);
        JScrollPane timetableScroll = new JScrollPane(timetableTable);
        JButton refreshTimetableBtn = new JButton("Refresh Timetable");
        JPanel timetableTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timetableTop.add(refreshTimetableBtn);
        JPanel timetablePanel = new JPanel(new BorderLayout());
        timetablePanel.add(timetableTop, BorderLayout.NORTH);
        timetablePanel.add(timetableScroll, BorderLayout.CENTER);

        tabs.addTab("Timetable", timetablePanel);

        // --- Grades Tab ---
        gradesModel = new DefaultTableModel(new Object[]{"Course","Component","Score","Final Grade"},0) {
            @Override
            public boolean isCellEditable(int r, int c){ return false; }
        };
        gradesTable = new JTable(gradesModel);
        gradesTable.setAutoCreateRowSorter(true);
        JScrollPane gradesScroll = new JScrollPane(gradesTable);
        JButton downloadTranscriptBtn = new JButton("Download Transcript (PDF)");
        JPanel gradesTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gradesTop.add(downloadTranscriptBtn);
        JPanel gradesPanel = new JPanel(new BorderLayout());
        gradesPanel.add(gradesTop, BorderLayout.NORTH);
        gradesPanel.add(gradesScroll, BorderLayout.CENTER);

        tabs.addTab("Grades", gradesPanel);

        // Add tabs to frame center
        add(tabs, BorderLayout.CENTER);

        // Events
        refreshCatalog.addActionListener(e -> loadCatalog());
        viewSections.addActionListener(e -> {
            int sel = catalogTable.getSelectedRow();
            if (sel == -1) { JOptionPane.showMessageDialog(this, "Select a course first"); return; }
            int courseId = (int) catalogModel.getValueAt(sel, 0);
            loadSections(courseId);
            tabs.setSelectedIndex(1);
        });
        backToCatalog.addActionListener(e -> tabs.setSelectedIndex(0));
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