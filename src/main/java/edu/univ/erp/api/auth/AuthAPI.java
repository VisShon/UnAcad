package edu.univ.erp.api.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.auth.hash.PasswordHasher;
import edu.univ.erp.auth.session.UserSession;
import edu.univ.erp.auth.store.AuthStore;
import edu.univ.erp.data.DataSourceFactory;

public class AuthAPI {

    public static APIResponse<String> login(String username, String password) {
        var result = AuthStore.login(username, password);
        if (result == null) {
            return APIResponse.error("Invalid username or password.");
        }

        UserSession.createSession(result.userId, result.role);
        return APIResponse.<String>success("Login successful").withData(result.role);
    }

    public static APIResponse<Void> logout() {
        UserSession.clear();
        return APIResponse.success("Logged out");
    }

    public static String currentUserRole() {
        return UserSession.getRole();
    }

    public static APIResponse<Void> changePassword(String oldPassword, String newPassword) {
        int userId = UserSession.getUserId();
        if (userId == -1) {
            return APIResponse.error("Not logged in");
        }

        try (Connection conn = DataSourceFactory.getAuthDB().getConnection()) {
            // Verify old password
            String sql = "SELECT password_hash FROM users WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    return APIResponse.error("User not found");
                }
                String storedHash = rs.getString("password_hash");
                if (!PasswordHasher.verify(oldPassword, storedHash)) {
                    return APIResponse.error("Current password is incorrect");
                }
            }

            // Update to new password
            String newHash = PasswordHasher.hash(newPassword);
            sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newHash);
                stmt.setInt(2, userId);
                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    return APIResponse.success("Password changed successfully");
                } else {
                    return APIResponse.error("Failed to update password");
                }
            }
        } catch (Exception e) {
            return APIResponse.error("Error changing password: " + e.getMessage());
        }
    }
}