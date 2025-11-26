package edu.univ.erp.api.instructor;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.auth.session.UserSession;
import edu.univ.erp.service.InstructorService;

import java.util.List;
import java.util.Map;

public class InstructorAPI {

    public static APIResponse<List<Map<String, Object>>> mySections() {
        int instId = UserSession.getUserId();
        if (instId <= 0) return APIResponse.error("Not logged in");
        return InstructorService.mySections(instId);
    }

    public static APIResponse<Void> enterScore(int enrollmentId, String component, double score) {
        int instId = UserSession.getUserId();
        if (instId <= 0) return APIResponse.error("Not logged in");
        return InstructorService.enterScore(enrollmentId, component, score);
    }

    public static APIResponse<Void> computeFinalGrade(int enrollmentId) {
        int instId = UserSession.getUserId();
        if (instId <= 0) return APIResponse.error("Not logged in");
        return InstructorService.computeFinalGrade(enrollmentId);
    }

    public static APIResponse<Map<String, Object>> getClassStats(int sectionId) {
        int instId = UserSession.getUserId();
        if (instId <= 0) return APIResponse.error("Not logged in");
        return InstructorService.classStats(sectionId);
    }

    // Add this method for CSV export
    public static APIResponse<List<Map<String, Object>>> getSectionGrades(int sectionId) {
        int instId = UserSession.getUserId();
        if (instId <= 0) return APIResponse.error("Not logged in");
        return InstructorService.getSectionGrades(sectionId);
    }
}