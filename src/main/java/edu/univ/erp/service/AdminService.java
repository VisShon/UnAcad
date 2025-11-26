package edu.univ.erp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import edu.univ.erp.access.MaintenanceManager;
import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.auth.hash.PasswordHasher;
import edu.univ.erp.auth.store.AuthStore;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.DataSourceFactory;  // Add this import
import edu.univ.erp.data.SectionDAO;  // Add this import

/**
 * Handles admin operations:
 * - add user (auth_db)
 * - create student/instructor profile
 * - create courses & sections
 * - assign instructor
 * - maintenance mode
 */
public class AdminService {

    // Add a user to AUTH DB + profile to ERP DB
    // Note: For simplicity, addUser is kept for basic auth; new methods for profiles
    public static APIResponse<Void> addUser(String username, String role, String password) {
        if (!role.equalsIgnoreCase("ADMIN")) {
            return APIResponse.error("Use addStudent or addInstructor for non-admin roles");
        }
        try {
            // Insert into auth_db
            try (Connection c = DataSourceFactory.getAuthDB().getConnection()) {
                String sql = "INSERT INTO users (username, role, password_hash) VALUES (?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, username);
                    ps.setString(2, role.toUpperCase());
                    ps.setString(3, PasswordHasher.hash(password));
                    ps.executeUpdate();
                }
            }
            return APIResponse.success("Admin user added successfully");
        } catch (SQLException e) {
            return APIResponse.error("Failed to add admin user: " + e.getMessage());
        }
    }

    // Add student: auth + ERP profile
    // Note: username should be the roll number for consistency
    public static APIResponse<Void> addStudent(String name, String rollNo, String password, String program, int year) {
        try {
            // Insert into auth_db and get user_id (use roll number as username)
            int userId;
            try (Connection c = DataSourceFactory.getAuthDB().getConnection()) {
                String sql = "INSERT INTO users (username, role, password_hash) VALUES (?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, rollNo);  // Use roll number as username
                    ps.setString(2, "STUDENT");
                    ps.setString(3, PasswordHasher.hash(password));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        userId = rs.next() ? rs.getInt(1) : -1;
                    }
                }
            }
            if (userId == -1) throw new Exception("Failed to get user ID");

            // Insert into ERP students
            try (Connection erp = DataSourceFactory.getErpDB().getConnection()) {
                String sql2 = "INSERT INTO students (user_id, name, roll_no, program, year) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps2 = erp.prepareStatement(sql2)) {
                    ps2.setInt(1, userId);
                    ps2.setString(2, name);
                    ps2.setString(3, rollNo);
                    ps2.setString(4, program);
                    ps2.setInt(5, year);
                    ps2.executeUpdate();
                }
            }
            return APIResponse.success("Student added successfully");
        } catch (Exception e) {
            return APIResponse.error("Failed to add student: " + e.getMessage());
        }
    }

    // Add instructor: auth + ERP profile
    public static APIResponse<Void> addInstructor(String username, String password, String department) {
        try {
            // Insert into auth_db and get user_id
            int userId;
            try (Connection c = DataSourceFactory.getAuthDB().getConnection()) {
                String sql = "INSERT INTO users (username, role, password_hash) VALUES (?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, "INSTRUCTOR");
                    ps.setString(3, PasswordHasher.hash(password));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        userId = rs.next() ? rs.getInt(1) : -1;
                    }
                }
            }
            if (userId == -1) throw new Exception("Failed to get user ID");

            // Insert into ERP instructors
            try (Connection erp = DataSourceFactory.getErpDB().getConnection()) {
                String sql2 = "INSERT INTO instructors (instructor_id, department) VALUES (?,?)";
                try (PreparedStatement ps2 = erp.prepareStatement(sql2)) {
                    ps2.setInt(1, userId);
                    ps2.setString(2, department);
                    ps2.executeUpdate();
                }
            }
            return APIResponse.success("Instructor added successfully");
        } catch (Exception e) {
            return APIResponse.error("Failed to add instructor: " + e.getMessage());
        }
    }

    // Create a course
    public static APIResponse<Void> createCourse(String code, String title, int credits) {
        if (MaintenanceManager.isReadOnly())
            return APIResponse.error("System is in maintenance mode");

        try {
            boolean ok = CourseDAO.createCourse(code, title, credits);
            return ok ? APIResponse.success("Course created")
                    : APIResponse.error("Failed to create course");
        } catch (Exception e) {
            return APIResponse.error("Failed: " + e.getMessage());
        }
    }

    // Create a section
    public static APIResponse<Void> createSection(int courseId, int instructorId, String dayTime, String room, int cap, String semester, int year) {
        if (MaintenanceManager.isReadOnly())
            return APIResponse.error("System is in maintenance mode");

        try {
            boolean ok = SectionDAO.createSection(courseId, instructorId, dayTime, room, cap, semester, year);
            return ok ? APIResponse.success("Section created")
                    : APIResponse.error("Failed to create section");
        } catch (Exception e) {
            return APIResponse.error("Error: " + e.getMessage());
        }
    }

    // Toggle maintenance mode
    public static APIResponse<Void> toggleMaintenance(boolean on) {
        try {
            MaintenanceManager.setReadOnly(on);  // Now resolves
            return APIResponse.success("Maintenance mode updated");
        } catch (Exception e) {
            return APIResponse.error("Cannot update maintenance: " + e.getMessage());
        }
    }

    // List all users
    public static APIResponse<List<Map<String, Object>>> listUsers() {
        try {
            List<Map<String, Object>> users = AuthStore.listUsers();
            return APIResponse.<List<Map<String, Object>>>success("Users loaded").withData(users);
        } catch (Exception e) {
            return APIResponse.error("Failed to load users: " + e.getMessage());
        }
    }

    // List all sections
    public static APIResponse<List<Map<String, Object>>> listSections() {
        try {
            List<Map<String, Object>> sections = SectionDAO.listAllSections();
            return APIResponse.<List<Map<String, Object>>>success("Sections loaded").withData(sections);
        } catch (Exception e) {
            return APIResponse.error("Failed to load sections: " + e.getMessage());
        }
    }
}