package edu.univ.erp.data;

import edu.univ.erp.domain.Instructor;

import java.sql.*;

public class InstructorDAO {

    public static Instructor getInstructor(int userId) throws Exception {
        String sql = "SELECT * FROM instructors WHERE instructor_id = ?";

        try (Connection c = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Instructor i = new Instructor();
                i.setUserId(userId);
                i.setDepartment(rs.getString("department"));
                return i;
            }
        }
    }
}