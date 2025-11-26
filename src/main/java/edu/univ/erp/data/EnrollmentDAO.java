package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrollmentDAO {

    public static class EnrollmentException extends Exception {
        public EnrollmentException(String m) { super(m); }
    }

    /** REGISTER */
    public static boolean register(int studentId, int sectionId) throws Exception {
        try (Connection conn = DataSourceFactory.getErpDB().getConnection()) {

            conn.setAutoCommit(false);

            // Check duplicate
            String dup = """
                SELECT 1 FROM enrollments 
                WHERE student_id=? AND section_id=? AND status='ENROLLED'
            """;
            try (PreparedStatement ps = conn.prepareStatement(dup)) {
                ps.setInt(1, studentId);
                ps.setInt(2, sectionId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) throw new EnrollmentException("Already enrolled");
            }

            // Lock section row
            int capacity;
            String capQuery = "SELECT capacity FROM sections WHERE section_id=? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(capQuery)) {
                ps.setInt(1, sectionId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new EnrollmentException("Section not found");
                capacity = rs.getInt("capacity");
            }

            // Count enrolled
            int enrolled;
            String count = """
                SELECT COUNT(*) AS cnt FROM enrollments 
                WHERE section_id=? AND status='ENROLLED'
            """;
            try (PreparedStatement ps = conn.prepareStatement(count)) {
                ps.setInt(1, sectionId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                enrolled = rs.getInt("cnt");
            }

            if (enrolled >= capacity)
                throw new EnrollmentException("Section is full");

            // Insert
            String insert = "INSERT INTO enrollments(student_id,section_id,status) VALUES (?,?, 'ENROLLED')";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, studentId);
                ps.setInt(2, sectionId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        }
    }

    /** DROP */
    public static boolean drop(int studentId, int sectionId) throws Exception {
        String sql = """
            UPDATE enrollments SET status='DROPPED'
            WHERE student_id=? AND section_id=? AND status='ENROLLED'
        """;

        try (Connection c = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            return ps.executeUpdate() > 0;
        }
    }

    /** LIST enrolled sections with course info */
    public static List<Map<String,Object>> listEnrollmentsForStudent(int studentId) throws Exception {
        String sql = """
            SELECT e.enrollment_id, e.section_id, 
                   c.code, c.title, 
                   s.day_time, s.room,
                   i.department AS instructor
            FROM enrollments e
            JOIN sections s ON e.section_id=s.section_id
            JOIN courses c ON s.course_id=c.course_id
            LEFT JOIN instructors i ON s.instructor_id=i.instructor_id
            WHERE e.student_id=? AND e.status='ENROLLED'
        """;

        List<Map<String,Object>> out = new ArrayList<>();

        try (Connection c = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> m = new HashMap<>();
                    m.put("enrollment_id", rs.getInt("enrollment_id"));
                    m.put("section_id", rs.getInt("section_id"));
                    m.put("code", rs.getString("code"));
                    m.put("title", rs.getString("title"));
                    m.put("day_time", rs.getString("day_time"));
                    m.put("room", rs.getString("room"));
                    m.put("instructor", rs.getString("instructor"));
                    out.add(m);
                }
            }
        }
        return out;
    }

    public static List<Map<String, Object>> timetableForStudent(int studentId) throws Exception {

        String sql = """
            SELECT 
                c.code,
                s.day_time,
                s.room,
                i.department AS instructor
            FROM enrollments e
            JOIN sections s ON e.section_id = s.section_id
            JOIN courses c ON s.course_id = c.course_id
            LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
            WHERE e.student_id = ? 
              AND e.status = 'ENROLLED'
            ORDER BY s.day_time
        """;

        List<Map<String, Object>> out = new ArrayList<>();

        try (Connection c = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("code", rs.getString("code"));
                    row.put("day_time", rs.getString("day_time"));
                    row.put("room", rs.getString("room"));
                    row.put("instructor", rs.getString("instructor"));
                    out.add(row);
                }
            }
        }

        return out;
    }

    // Add this method to EnrollmentDAO
    public static List<Map<String, Object>> listEnrollmentsForSection(int sectionId) throws Exception {
        String sql = """
            SELECT e.enrollment_id, e.student_id, s.roll_no AS student_name
            FROM enrollments e
            JOIN students s ON e.student_id = s.user_id
            WHERE e.section_id = ? AND e.status = 'ENROLLED'
            ORDER BY s.roll_no
        """;

        List<Map<String, Object>> out = new ArrayList<>();

        try (Connection c = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("enrollment_id", rs.getInt("enrollment_id"));
                    m.put("student_id", rs.getInt("student_id"));
                    m.put("name", rs.getString("student_name"));
                    out.add(m);
                }
            }
        }
        return out;
    }
}