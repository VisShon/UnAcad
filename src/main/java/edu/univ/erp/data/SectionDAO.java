package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.univ.erp.api.types.SectionRow;

public class SectionDAO {

    public static List<SectionRow> listSectionsByCourse(int courseId) throws Exception {
        String sql = """
            SELECT s.section_id, s.day_time, s.room, s.capacity,
                   i.department AS instructor_name,
                   (SELECT COUNT(*) FROM enrollments e 
                     WHERE e.section_id = s.section_id AND e.status='ENROLLED') AS enrolled
            FROM sections s
            LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
            WHERE s.course_id = ?
            ORDER BY s.section_id
        """;

        List<SectionRow> out = new ArrayList<>();

        try (Connection c = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SectionRow r = new SectionRow();
                    r.sectionId = rs.getInt("section_id");
                    r.instructorName = rs.getString("instructor_name");
                    r.dayTime = rs.getString("day_time");
                    r.capacity = rs.getInt("capacity");
                    r.enrolled = rs.getInt("enrolled");
                    out.add(r);
                }
            }
        }
        return out;
    }

    public static boolean createSection(int courseId, int instId, String dayTime, String room, int cap, String semester, int year) throws Exception {
        String sql = """
            INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, courseId);
            if (instId == 0) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, instId);
            }
            ps.setString(3, dayTime);
            ps.setString(4, room);
            ps.setInt(5, cap);
            ps.setString(6, semester);
            ps.setInt(7, year);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<Map<String, Object>> listSectionsForInstructor(int instructorId) throws Exception {

        String sql = """
        SELECT s.section_id, s.course_id, c.code, c.title,
               s.day_time, s.room, s.capacity,
               (SELECT COUNT(*) FROM enrollments e
                WHERE e.section_id = s.section_id AND e.status='ENROLLED') AS enrolled
        FROM sections s
        JOIN courses c ON s.course_id = c.course_id
        WHERE s.instructor_id = ?
        ORDER BY s.section_id
    """;

        List<Map<String, Object>> out = new ArrayList<>();

        try (Connection conn = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, instructorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("section_id", rs.getInt("section_id"));
                    m.put("course_id", rs.getInt("course_id"));
                    m.put("code", rs.getString("code"));
                    m.put("title", rs.getString("title"));
                    m.put("day_time", rs.getString("day_time"));
                    m.put("room", rs.getString("room"));
                    m.put("capacity", rs.getInt("capacity"));
                    m.put("enrolled", rs.getInt("enrolled"));
                    out.add(m);
                }
            }
        }

        return out;
    }

    public static List<Map<String, Object>> listAllSections() throws Exception {
        String sql = """
            SELECT s.section_id, s.course_id, c.code, c.title,
                   i.department AS instructor_name,
                   s.day_time, s.room, s.capacity,
                   (SELECT COUNT(*) FROM enrollments e
                    WHERE e.section_id = s.section_id AND e.status='ENROLLED') AS enrolled
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            LEFT JOIN instructors i ON s.instructor_id = i.instructor_id
            ORDER BY s.section_id
        """;

        List<Map<String, Object>> out = new ArrayList<>();

        try (Connection conn = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("section_id", rs.getInt("section_id"));
                    m.put("course_id", rs.getInt("course_id"));
                    m.put("code", rs.getString("code"));
                    m.put("title", rs.getString("title"));
                    m.put("instructor", rs.getString("instructor_name"));
                    m.put("day_time", rs.getString("day_time"));
                    m.put("room", rs.getString("room"));
                    m.put("capacity", rs.getInt("capacity"));
                    m.put("enrolled", rs.getInt("enrolled"));
                    out.add(m);
                }
            }
        }

        return out;
    }

}