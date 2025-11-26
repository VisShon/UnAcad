package edu.univ.erp.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;  // Add this import
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import edu.univ.erp.api.admin.AdminAPI;
import edu.univ.erp.api.catalog.CatalogAPI;
import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.api.maintenance.MaintenanceAPI;
import edu.univ.erp.api.types.CourseRow;
import edu.univ.erp.ui.common.NavigationBar;
import net.miginfocom.swing.MigLayout;

public class AdminDashboard extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();

    // Users tab
    private final DefaultTableModel usersModel;
    private final JTable usersTable;

    // Courses tab
    private final DefaultTableModel coursesModel;
    private final JTable coursesTable;

    // Sections tab
    private final DefaultTableModel sectionsModel;
    private final JTable sectionsTable;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Navigation bar
        NavigationBar navBar = new NavigationBar();
        setLayout(new BorderLayout());
        add(navBar, BorderLayout.NORTH);

        // --- Users Tab ---
        usersModel = new DefaultTableModel(new Object[]{"User ID", "Username", "Role", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        usersTable = new JTable(usersModel);
        usersTable.setAutoCreateRowSorter(true);
        JScrollPane usersScroll = new JScrollPane(usersTable);

        JButton refreshUsers = new JButton("Refresh Users");
        JButton addStudentBtn = new JButton("Add Student");
        JButton addInstructorBtn = new JButton("Add Instructor");
        JButton addAdminBtn = new JButton("Add Admin");

        JPanel usersPanel = new JPanel(new BorderLayout());
        JPanel usersTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usersTop.add(refreshUsers);
        usersTop.add(addStudentBtn);
        usersTop.add(addInstructorBtn);
        usersTop.add(addAdminBtn);
        usersPanel.add(usersTop, BorderLayout.NORTH);
        usersPanel.add(usersScroll, BorderLayout.CENTER);

        tabs.addTab("Users", usersPanel);

        // --- Courses Tab ---
        coursesModel = new DefaultTableModel(new Object[]{"Course ID", "Code", "Title", "Credits"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        coursesTable = new JTable(coursesModel);
        coursesTable.setAutoCreateRowSorter(true);
        JScrollPane coursesScroll = new JScrollPane(coursesTable);

        JButton refreshCourses = new JButton("Refresh Courses");
        JButton addCourseBtn = new JButton("Add Course");

        JPanel coursesPanel = new JPanel(new BorderLayout());
        JPanel coursesTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        coursesTop.add(refreshCourses);
        coursesTop.add(addCourseBtn);
        coursesPanel.add(coursesTop, BorderLayout.NORTH);
        coursesPanel.add(coursesScroll, BorderLayout.CENTER);

        tabs.addTab("Courses", coursesPanel);

        // --- Sections Tab ---
        sectionsModel = new DefaultTableModel(new Object[]{"Section ID", "Course", "Instructor", "Day/Time", "Capacity", "Enrolled"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        sectionsTable = new JTable(sectionsModel);
        sectionsTable.setAutoCreateRowSorter(true);
        JScrollPane sectionsScroll = new JScrollPane(sectionsTable);

        JButton refreshSections = new JButton("Refresh Sections");
        JButton addSectionBtn = new JButton("Add Section");

        JPanel sectionsPanel = new JPanel(new BorderLayout());
        JPanel sectionsTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sectionsTop.add(refreshSections);
        sectionsTop.add(addSectionBtn);
        sectionsPanel.add(sectionsTop, BorderLayout.NORTH);
        sectionsPanel.add(sectionsScroll, BorderLayout.CENTER);

        tabs.addTab("Sections", sectionsPanel);

        // --- Maintenance Tab ---
        JButton toggleMaintenanceBtn = new JButton("Toggle Maintenance Mode");
        JLabel maintenanceStatusLabel = new JLabel("Current: " + (MaintenanceAPI.isReadOnly() ? "ON" : "OFF"));

        JPanel maintenancePanel = new JPanel(new MigLayout("wrap 1", "[center]", "[]20[]"));
        maintenancePanel.add(new JLabel("Maintenance Mode Control", SwingConstants.CENTER));
        maintenancePanel.add(toggleMaintenanceBtn);
        maintenancePanel.add(maintenanceStatusLabel);

        tabs.addTab("Maintenance", maintenancePanel);

        add(tabs, BorderLayout.CENTER);

        // Events
        refreshUsers.addActionListener(e -> loadUsers());
        addStudentBtn.addActionListener(e -> showAddStudentDialog());
        addInstructorBtn.addActionListener(e -> showAddInstructorDialog());
        addAdminBtn.addActionListener(e -> showAddAdminDialog());

        refreshCourses.addActionListener(e -> loadCourses());
        addCourseBtn.addActionListener(e -> showAddCourseDialog());

        refreshSections.addActionListener(e -> loadSections());
        addSectionBtn.addActionListener(e -> showAddSectionDialog());

        toggleMaintenanceBtn.addActionListener(e -> {
            boolean current = MaintenanceAPI.isReadOnly();
            APIResponse<Void> r = AdminAPI.toggleMaintenance(!current);
            if (r.success) {
                JOptionPane.showMessageDialog(this, r.message);
                maintenanceStatusLabel.setText("Current: " + (!current ? "ON" : "OFF"));
                navBar.updateMaintenanceBanner();
            } else {
                JOptionPane.showMessageDialog(this, r.message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Load initial data
        loadUsers();
        loadCourses();
        loadSections();
    }

    private void loadUsers() {
        APIResponse<List<Map<String, Object>>> r = AdminAPI.listUsers();
        usersModel.setRowCount(0);
        if (!r.success) {
            JOptionPane.showMessageDialog(this, r.message);
            return;
        }
        List<Map<String, Object>> users = r.data;
        for (Map<String, Object> u : users) {
            usersModel.addRow(new Object[]{u.get("user_id"), u.get("username"), u.get("role"), u.get("status")});
        }
    }

    private void loadCourses() {
        APIResponse<List<Map<String, Object>>> r = CatalogAPI.listCourses();
        coursesModel.setRowCount(0);
        if (!r.success) {
            JOptionPane.showMessageDialog(this, r.message);
            return;
        }
        @SuppressWarnings("unchecked")
        List<CourseRow> courses = (List<CourseRow>) (List<?>) r.data;
        for (CourseRow c : courses) {
            coursesModel.addRow(new Object[]{c.courseId, c.code, c.title, c.credits});
        }
    }

    private void loadSections() {
        APIResponse<List<Map<String, Object>>> r = AdminAPI.listSections();
        sectionsModel.setRowCount(0);
        if (!r.success) {
            JOptionPane.showMessageDialog(this, r.message);
            return;
        }
        List<Map<String, Object>> sections = r.data;
        for (Map<String, Object> s : sections) {
            String courseInfo = s.get("code") + " - " + s.get("title");
            String instructor = s.get("instructor") != null ? (String) s.get("instructor") : "Unassigned";
            sectionsModel.addRow(new Object[]{
                    s.get("section_id"),
                    courseInfo,
                    instructor,
                    s.get("day_time"),
                    s.get("capacity"),
                    s.get("enrolled")
            });
        }
    }


    private void showAddStudentDialog() {
        JTextField nameField = new JTextField();
        JTextField rollNoField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField programField = new JTextField();
        JTextField yearField = new JTextField();

        // Restrict yearField to digits only
        ((AbstractDocument) yearField.getDocument()).setDocumentFilter(new DigitFilter());

        Object[] message = {
                "Name:", nameField,
                "Roll No (will be username):", rollNoField,
                "Password:", passwordField,
                "Program:", programField,
                "Year (numbers only):", yearField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Student", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String rollNo = rollNoField.getText().trim();
            String password = new String(passwordField.getPassword());
            String program = programField.getText().trim();
            String yearText = yearField.getText().trim();

            // Validation
            if (name.isEmpty() || rollNo.isEmpty() || password.isEmpty() || program.isEmpty() || yearText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!yearText.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Year must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int year;
            try {
                year = Integer.parseInt(yearText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Year must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            APIResponse<Void> r = AdminAPI.addStudent(name, rollNo, password, program, year);
            JOptionPane.showMessageDialog(this, r.message, r.success ? "Success" : "Error", r.success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (r.success) loadUsers();
        }
    }

    private void showAddInstructorDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField departmentField = new JTextField();

        Object[] message = {
                "Username:", usernameField,
                "Password:", passwordField,
                "Department:", departmentField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Instructor", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String department = departmentField.getText().trim();

            APIResponse<Void> r = AdminAPI.addInstructor(username, password, department);
            JOptionPane.showMessageDialog(this, r.message, r.success ? "Success" : "Error", r.success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (r.success) loadUsers();
        }
    }

    private void showAddAdminDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] message = {
                "Username:", usernameField,
                "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Admin", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            APIResponse<Void> r = AdminAPI.addUser(username, "ADMIN", password);
            JOptionPane.showMessageDialog(this, r.message, r.success ? "Success" : "Error", r.success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (r.success) loadUsers();
        }
    }


    private void showAddCourseDialog() {
        JTextField codeField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField creditsField = new JTextField();

        // Restrict creditsField to digits only
        ((AbstractDocument) creditsField.getDocument()).setDocumentFilter(new DigitFilter());

        Object[] message = {
                "Code:", codeField,
                "Title:", titleField,
                "Credits (numbers only):", creditsField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Course", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            String creditsText = creditsField.getText().trim();

            if (code.isEmpty() || title.isEmpty() || creditsText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!creditsText.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Credits must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int credits;
            try {
                credits = Integer.parseInt(creditsText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Credits must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            APIResponse<Void> r = AdminAPI.createCourse(code, title, credits);
            JOptionPane.showMessageDialog(this, r.message, r.success ? "Success" : "Error", r.success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (r.success) loadCourses();
        }
    }


    private void showAddSectionDialog() {
        JComboBox<String> courseCombo = new JComboBox<>();
        JComboBox<String> instructorCombo = new JComboBox<>();
        JTextField dayTimeField = new JTextField();
        JTextField roomField = new JTextField();
        JTextField capacityField = new JTextField();
        JTextField semesterField = new JTextField("Spring");
        JTextField yearField = new JTextField("2025");

        // Restrict capacity, year to digits only
        ((AbstractDocument) capacityField.getDocument()).setDocumentFilter(new DigitFilter());
        ((AbstractDocument) yearField.getDocument()).setDocumentFilter(new DigitFilter());

        // Load courses
        APIResponse<List<Map<String, Object>>> coursesResp = CatalogAPI.listCourses();
        if (coursesResp.success && coursesResp.data != null) {
            @SuppressWarnings("unchecked")
            List<CourseRow> courses = (List<CourseRow>) (List<?>) coursesResp.data;
            for (CourseRow course : courses) {
                courseCombo.addItem(course.courseId + " - " + course.code + " - " + course.title);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to load courses: " + coursesResp.message,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Load instructors
        instructorCombo.addItem("0 - Unassigned"); // Allow unassigned
        APIResponse<List<Map<String, Object>>> usersResp = AdminAPI.listUsers();
        if (usersResp.success && usersResp.data != null) {
            List<Map<String, Object>> users = usersResp.data;
            for (Map<String, Object> user : users) {
                String role = (String) user.get("role");
                if ("INSTRUCTOR".equalsIgnoreCase(role)) {
                    int userId = ((Number) user.get("user_id")).intValue();
                    String username = (String) user.get("username");
                    instructorCombo.addItem(userId + " - " + username);
                }
            }
        }

        Object[] message = {
                "Course:", courseCombo,
                "Instructor:", instructorCombo,
                "Day/Time:", dayTimeField,
                "Room:", roomField,
                "Capacity:", capacityField,
                "Semester:", semesterField,
                "Year:", yearField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Section", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            // Validation
            if (dayTimeField.getText().trim().isEmpty() || roomField.getText().trim().isEmpty() ||
                    capacityField.getText().trim().isEmpty() || semesterField.getText().trim().isEmpty() ||
                    yearField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!capacityField.getText().trim().matches("\\d+") || !yearField.getText().trim().matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Capacity and Year must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String courseStr = (String) courseCombo.getSelectedItem();
                String instructorStr = (String) instructorCombo.getSelectedItem();
                
                if (courseStr == null || instructorStr == null) {
                    JOptionPane.showMessageDialog(this, "Please select a course and instructor.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int courseId = Integer.parseInt(courseStr.split(" - ")[0]);
                int instructorId = Integer.parseInt(instructorStr.split(" - ")[0]);
                String dayTime = dayTimeField.getText().trim();
                String room = roomField.getText().trim();
                int capacity = Integer.parseInt(capacityField.getText().trim());
                String semester = semesterField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());

                if (capacity <= 0) {
                    JOptionPane.showMessageDialog(this, "Capacity must be greater than 0.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Use instructorId = 0 for unassigned (NULL in DB)
                int actualInstructorId = (instructorId == 0) ? 0 : instructorId;
                APIResponse<Void> r = AdminAPI.createSection(courseId, actualInstructorId, dayTime, room, capacity, semester, year);
                JOptionPane.showMessageDialog(this, r.message, r.success ? "Success" : "Error", r.success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                if (r.success) loadSections();
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                JOptionPane.showMessageDialog(this, "Invalid input format: " + e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static class DigitFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string.matches("\\d*")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text.matches("\\d*")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

}