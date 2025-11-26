package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradesDAO {

    /** Get all grade components for a student */
    public static List<Map<String,Object>> getGradesForStudent(int studentId) throws Exception {
        String sql = """
            SELECT c.code, g.component, g.score, g.final_grade
            FROM grades g
            JOIN enrollments e ON g.enrollment_id=e.enrollment_id
            JOIN sections s ON e.section_id=s.section_id
            JOIN courses c ON s.course_id=c.course_id
            WHERE e.student_id=?
            ORDER BY c.code, g.component
        """;

        List<Map<String,Object>> out = new ArrayList<>();

        try (Connection conn = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> m = new HashMap<>();
                    m.put("code", rs.getString("code"));
                    m.put("component", rs.getString("component"));
                    m.put("score", rs.getObject("score"));
                    m.put("final_grade", rs.getString("final_grade"));
                    out.add(m);
                }
            }
        }
        return out;
    }

    public static boolean insertOrUpdateScore(int enrollmentId, String component, double score) throws Exception {

        String sql = """
        INSERT INTO grades (enrollment_id, component, score)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE score = VALUES(score)
    """;

        try (Connection conn = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);
            ps.setString(2, component);
            ps.setDouble(3, score);

            return ps.executeUpdate() > 0;
        }
    }

    public static String computeFinal(int enrollmentId) throws Exception {

        // Simple example weighting
        double quiz = 0, mid = 0, fin = 0;

        String sql = "SELECT component, score FROM grades WHERE enrollment_id = ?";

        try (Connection conn = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String c = rs.getString("component").toLowerCase();
                    double s = rs.getDouble("score");

                    if (c.contains("quiz")) quiz = s;
                    else if (c.contains("mid")) mid = s;
                    else if (c.contains("final")) fin = s;
                }
            }
        }

        double total = quiz * 0.20 + mid * 0.30 + fin * 0.50;

        String letter = (total >= 90) ? "A" :
                (total >= 80) ? "B" :
                        (total >= 70) ? "C" :
                                (total >= 60) ? "D" : "F";

        // save in DB
        String upd = "UPDATE grades SET final_grade = ? WHERE enrollment_id = ?";

        try (Connection conn = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(upd)) {

            ps.setString(1, letter);
            ps.setInt(2, enrollmentId);
            ps.executeUpdate();
        }

        return letter;
    }

    public static Map<String, Object> getClassStats(int sectionId) throws Exception {

        String sql = """
        SELECT AVG(score) AS avg_score,
               MIN(score) AS min_score,
               MAX(score) AS max_score
        FROM grades g
        JOIN enrollments e ON g.enrollment_id = e.enrollment_id
        WHERE e.section_id = ?
    """;

        try (Connection conn = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("avg", rs.getDouble("avg_score"));
                    m.put("min", rs.getDouble("min_score"));
                    m.put("max", rs.getDouble("max_score"));
                    return m;
                }
            }
        }
        return null;
    }

    /** Get all grade components for a specific enrollment */
    public static List<Map<String, Object>> getGradesForEnrollment(int enrollmentId) throws Exception {
        String sql = """
            SELECT component, score, final_grade
            FROM grades
            WHERE enrollment_id = ?
            ORDER BY component
        """;

        List<Map<String, Object>> out = new ArrayList<>();

        try (Connection conn = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("component", rs.getString("component"));
                    m.put("score", rs.getObject("score"));
                    m.put("final_grade", rs.getString("final_grade"));
                    out.add(m);
                }
            }
        }
        return out;
    }

    // Add this method to the existing GradesDAO class
    public static List<Map<String, Object>> getGradesForSection(int sectionId) {
        String sql = """
            SELECT e.enrollment_id, e.student_id, s.roll_no AS student_name,
                   c.code AS course, g.component, g.score, g.final_grade
            FROM enrollments e
            JOIN students s ON e.student_id = s.user_id
            JOIN sections sec ON e.section_id = sec.section_id
            JOIN courses c ON sec.course_id = c.course_id
            LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id
            WHERE e.section_id = ? AND e.status = 'ENROLLED'
            ORDER BY s.roll_no, g.component
        """;

        List<Map<String, Object>> results = new ArrayList<>();

        try (java.sql.Connection conn = DataSourceFactory.getErpDB().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("enrollment_id", rs.getInt("enrollment_id"));
                    m.put("student_id", rs.getInt("student_id"));
                    m.put("name", rs.getString("student_name"));
                    m.put("course", rs.getString("course"));
                    m.put("component", rs.getString("component"));
                    m.put("score", rs.getObject("score"));
                    m.put("final_grade", rs.getString("final_grade"));
                    results.add(m);
                }
            }
        } catch (Exception ex) {
            // propagate as unchecked to keep signature unchanged; callers can handle if needed
            throw new RuntimeException("Error fetching grades for section: " + ex.getMessage(), ex);
        }

        return results;
        
    }
}