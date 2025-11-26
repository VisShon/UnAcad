package edu.univ.erp.data;

import edu.univ.erp.api.types.CourseRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public static List<CourseRow> listCourses() throws Exception {
        List<CourseRow> out = new ArrayList<>();
        String sql = "SELECT course_id, code, title, credits FROM courses ORDER BY code";

        try (Connection c = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CourseRow r = new CourseRow();
                r.courseId = rs.getInt("course_id");
                r.code = rs.getString("code");
                r.title = rs.getString("title");
                r.credits = rs.getInt("credits");
                out.add(r);
            }
        }
        return out;
    }

    public static boolean createCourse(String code, String title, int credits) throws Exception {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection c = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, title);
            ps.setInt(3, credits);
            return ps.executeUpdate() > 0;
        }
    }
}