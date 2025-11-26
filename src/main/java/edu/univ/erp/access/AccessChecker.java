package edu.univ.erp.access;

import edu.univ.erp.auth.session.UserSession;

/**
 * Checks whether actions are allowed based on user role / ownership.
 * UI and Service layers call this before doing DB operations.
 */
public class AccessChecker {

    /**
     * Returns true if the logged-in user is an admin.
     */
    public static boolean isAdmin() {
        String role = UserSession.getRole();
        return role != null && role.equalsIgnoreCase("ADMIN");
    }

    /**
     * Returns true if logged-in user is a student.
     */
    public static boolean isStudent() {
        String role = UserSession.getRole();
        return role != null && role.equalsIgnoreCase("STUDENT");
    }

    /**
     * Returns true if logged-in user is an instructor.
     */
    public static boolean isInstructor() {
        String role = UserSession.getRole();
        return role != null && role.equalsIgnoreCase("INSTRUCTOR");
    }

    /**
     * Students can only modify *their own* data.
     */
    public static boolean studentOwns(int studentId) {
        return isStudent() && UserSession.getUserId() == studentId;
    }

    /**
     * Instructors can only manage sections assigned to them.
     */
    public static boolean instructorOwns(int instructorId) {
        return isInstructor() && UserSession.getUserId() == instructorId;
    }

    /**
     * Students and instructors cannot modify data during maintenance mode.
     */
    public static boolean isBlockedByMaintenance() {
        return !isAdmin() && MaintenanceManager.isReadOnly();
    }

    /**
     * Generic allowed-check method:
     *
     * isAllowed("STUDENT") → if current user is student
     * isAllowed("ADMIN") → if current user is admin
     */
    public static boolean isAllowed(String requiredRole) {
        String role = UserSession.getRole();
        return role != null && role.equalsIgnoreCase(requiredRole);
    }
}