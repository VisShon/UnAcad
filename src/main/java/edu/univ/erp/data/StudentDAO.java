package edu.univ.erp.data;

import edu.univ.erp.domain.Student;

import java.sql.*;

public class StudentDAO {

    public static Student getStudent(int userId) throws Exception {
        String sql = "SELECT * FROM students WHERE user_id = ?";

        try (Connection c = DataSourceFactory.getErpDB().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Student s = new Student();
                s.setUserId(userId);
                s.setName(rs.getString("name"));
                s.setRollNo(rs.getString("roll_no"));
                s.setProgram(rs.getString("program"));
                s.setYear(rs.getInt("year"));
                return s;
            }
        }
    }
}