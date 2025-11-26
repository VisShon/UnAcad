package edu.univ.erp.ui.admin;

import java.util.List;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JSeparator;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import edu.univ.erp.api.admin.AdminAPI;
import edu.univ.erp.ui.components.UIComponents;
import edu.univ.erp.api.catalog.CatalogAPI;
import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.api.maintenance.MaintenanceAPI;
import edu.univ.erp.api.types.CourseRow;
import edu.univ.erp.ui.components.NavigationBar;
import edu.univ.erp.ui.components.SideBar;
import net.miginfocom.swing.MigLayout;

public class AdminDashboard extends JFrame {

    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    private NavigationBar navBar;
    private SideBar sidebar;

    private final DefaultTableModel usersModel;
    private final JTable usersTable;

    private final DefaultTableModel coursesModel;
    private final JTable coursesTable;

    private final DefaultTableModel sectionsModel;
    private final JTable sectionsTable;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        navBar = new NavigationBar();
        add(navBar, BorderLayout.NORTH);


        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.setBackground(Color.WHITE);;
        add(mainPanel, BorderLayout.CENTER);

        List<String[]> adminEntries = List.of(
            new String[]{"users", "🫂  Users"},
            new String[]{"courses", "📚  Courses"},
            new String[]{"sections", "🗂️  Sections"}
        );
        sidebar = new SideBar(
            adminEntries,
            (key, button) -> switchTab(key, button),
            () -> toggleMaintenance(),
            true
        );

        add(sidebar, BorderLayout.WEST);

        usersModel = new DefaultTableModel(
            new Object[]{
                "User ID", 
                "Username", 
                "Role", 
                "Status"
            }, 
            0 
        ) {
            public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };

        usersTable = UIComponents.table(usersModel);

        JButton refreshUsers = UIComponents.primaryButton(
            "Refresh Users",
            UIComponents.PRIMARY_BG
        );
        JButton addStudentButton = UIComponents.primaryButton(
            "Add Student",
            UIComponents.SECONDARY_BG
        );
        JButton addInstructorButton = UIComponents.primaryButton(
            "Add Instructor",
            UIComponents.SECONDARY_BG
        );
        JButton addAdminButton = UIComponents.primaryButton(
            "Add Admin",
            UIComponents.SECONDARY_BG
        );

        JPanel usersPanel = new JPanel(new BorderLayout());

        usersPanel.add(
            UIComponents.topActionBar(
                refreshUsers, 
                addStudentButton, 
                addInstructorButton, 
                addAdminButton
            ), 
            BorderLayout.NORTH
        );

        usersPanel.add(
            new JScrollPane(usersTable),
            BorderLayout.CENTER
        );

        mainPanel.add(usersPanel, "users");


        coursesModel = new DefaultTableModel(
            new Object[]{
                "Course ID", 
                "Code", 
                "Title", 
                "Credits"
            }, 
            0
        ) {
            public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };

        coursesTable = UIComponents.table(coursesModel);
        JButton refreshCourses = UIComponents.primaryButton(
            "Refresh Courses",
            UIComponents.PRIMARY_BG
        );
        JButton addCourseButton = UIComponents.primaryButton(
            "Add Course",
            UIComponents.SECONDARY_BG
        );

        JPanel coursesPanel = new JPanel(
            new BorderLayout()
        );

        coursesPanel.add(
            UIComponents.topActionBar(
                refreshCourses, 
                addCourseButton
            ), 
            BorderLayout.NORTH
        );

        coursesPanel.add(
            new JScrollPane(coursesTable), 
            BorderLayout.CENTER
        );

        mainPanel.add(coursesPanel, "courses");

        sectionsModel = new DefaultTableModel(
            new Object[]{
                "Section ID",
                "Course",
                "Instructor",
                "Day/Time",
                "Capacity", 
                "Enrolled"
            }, 
            0 
        ) {
            public boolean isCellEditable(int r, int c) { 
                return false;
            }
        };

        sectionsTable = UIComponents.table(sectionsModel);

        JButton refreshSections = UIComponents.primaryButton(
            "Refresh Sections",
            UIComponents.PRIMARY_BG
        );
        JButton addSectionButton = UIComponents.primaryButton(
            "Add Section",
            UIComponents.SECONDARY_BG
        );

        JPanel sectionsPanel = new JPanel(new BorderLayout());

        sectionsPanel.add(
            UIComponents.topActionBar(
                refreshSections,
                addSectionButton
            ), 
            BorderLayout.NORTH
        );

        sectionsPanel.add(
            new JScrollPane(sectionsTable), 
            BorderLayout.CENTER
        );

        mainPanel.add(
            sectionsPanel, 
            "sections"
        );

        refreshUsers.addActionListener(e -> loadUsers());
        addStudentButton.addActionListener(e -> showAddStudentDialog());
        addInstructorButton.addActionListener(e -> showAddInstructorDialog());
        addAdminButton.addActionListener(e -> showAddAdminDialog());

        refreshCourses.addActionListener(e -> loadCourses());
        addCourseButton.addActionListener(e -> showAddCourseDialog());

        refreshSections.addActionListener(e -> loadSections());
        addSectionButton.addActionListener(e -> showAddSectionDialog());

        loadUsers();
        loadCourses();
        loadSections();
    }

    private void toggleMaintenance() {
        boolean current = MaintenanceAPI.isReadOnly();
        APIResponse res = AdminAPI.toggleMaintenance(!current);

        if (res.success) {
            JOptionPane.showMessageDialog(this, res.message);
            navBar.updateMaintenanceBanner();
        } else {
            JOptionPane.showMessageDialog(
                this,
                res.message,
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void switchTab(String name, JButton button) {
        cardLayout.show(mainPanel, name);
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