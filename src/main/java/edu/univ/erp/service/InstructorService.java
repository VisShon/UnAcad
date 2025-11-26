package edu.univ.erp.service;

import edu.univ.erp.access.MaintenanceManager;
import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.data.GradesDAO;
import edu.univ.erp.data.SectionDAO;

import java.util.Map;
import java.util.List;

/**
 * Handles instructor operations:
 * - view my sections
 * - enter grades
 * - compute final grades
 * - simple stats
 */
public class InstructorService {

    public static APIResponse<List<Map<String, Object>>> mySections(int instructorId) {
        try {
            // Reuse SectionDAO, filtering by instructor
            List<Map<String, Object>> rows = SectionDAO.listSectionsForInstructor(instructorId);
            return APIResponse.<List<Map<String, Object>>>success("Sections loaded").withData(rows);
        } catch (Exception e) {
            return APIResponse.error("Failed to load instructor sections: " + e.getMessage());
        }
    }

    public static APIResponse<Void> enterScore(int enrollmentId, String component, double score) {
        if (MaintenanceManager.isReadOnly())
            return APIResponse.error("System is in maintenance mode");

        try {
            boolean ok = GradesDAO.insertOrUpdateScore(enrollmentId, component, score);
            return ok ? APIResponse.success("Score saved")
                    : APIResponse.error("Cannot save score");
        } catch (Exception e) {
            return APIResponse.error("Failed to enter score: " + e.getMessage());
        }
    }

    public static APIResponse<Void> computeFinalGrade(int enrollmentId) {
        if (MaintenanceManager.isReadOnly())
            return APIResponse.error("System is in maintenance mode");

        try {
            String grade = GradesDAO.computeFinal(enrollmentId);
            return APIResponse.success("Final grade computed: " + grade);
        } catch (Exception e) {
            return APIResponse.error("Failed to compute final grade: " + e.getMessage());
        }
    }

    public static APIResponse<Map<String, Object>> classStats(int sectionId) {
        try {
            Map<String, Object> stats = GradesDAO.getClassStats(sectionId);
            return APIResponse.<Map<String, Object>>success("Stats loaded").withData(stats);
        } catch (Exception e) {
            return APIResponse.error("Failed to compute statistics: " + e.getMessage());
        }
    }

    // Add this method for CSV export
    public static APIResponse<List<Map<String, Object>>> getSectionGrades(int sectionId) {
        try {
            List<Map<String, Object>> grades = GradesDAO.getGradesForSection(sectionId);
            return APIResponse.<List<Map<String, Object>>>success("Grades loaded").withData(grades);
        } catch (Exception e) {
            return APIResponse.error("Failed to load grades: " + e.getMessage());
        }
    }
}