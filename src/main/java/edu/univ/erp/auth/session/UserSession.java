package edu.univ.erp.auth.session;

/**
 * Holds the logged-in user's session (user_id and role).
 *
 * UI and API use UserSession.getUserId() and getRole()
 * to know who is currently logged in.
 */

public class UserSession {

    private static int userId = -1;
    private static String role = null;

    public static void createSession(int id, String r) {
        userId = id;
        role = r;
    }

    public static void clear() {
        userId = -1;
        role = null;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isLoggedIn() {
        return userId > 0 && role != null;
    }
}