package edu.univ.erp.auth.store;

import edu.univ.erp.auth.hash.PasswordHasher;
import edu.univ.erp.data.DataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AuthStore talks ONLY to the auth_db.
 * It does not store sessions; it only performs login queries.
 */

public class AuthStore {

    /**
     * Attempts login with username & password.
     *
     * @return AuthResult on success, null on failure
     */
    public static AuthResult login(String username, String password) {
        String sql = "SELECT user_id, password_hash, role FROM users WHERE username = ? AND status='ACTIVE'";

        try (Connection conn = DataSourceFactory.getAuthDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null; // username not found

                int userId = rs.getInt("user_id");
                String hash = rs.getString("password_hash");
                String role = rs.getString("role");

                // BCrypt verify
                if (!PasswordHasher.verify(password, hash)) {
                    return null;
                }

                return new AuthResult(userId, role);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lists all users from auth_db (admin only).
     */
    public static List<Map<String, Object>> listUsers() throws Exception {
        List<Map<String, Object>> out = new ArrayList<>();
        String sql = "SELECT user_id, username, role, status FROM users ORDER BY user_id";

        try (Connection conn = DataSourceFactory.getAuthDB().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("user_id", rs.getInt("user_id"));
                m.put("username", rs.getString("username"));
                m.put("role", rs.getString("role"));
                m.put("status", rs.getString("status"));
                out.add(m);
            }
        }
        return out;
    }

    /**
     * Helper class for returning typed result from AuthStore.login()
     */
    public static class AuthResult {
        public final int userId;
        public final String role;

        public AuthResult(int userId, String role) {
            this.userId = userId;
            this.role = role;
        }
    }
}