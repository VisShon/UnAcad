package edu.univ.erp.api.student;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.auth.session.UserSession;
import edu.univ.erp.service.StudentService;

import java.util.List;
import java.util.Map;

public class StudentAPI {

    public static APIResponse<List<Map<String, Object>>> listCatalog() {
        return StudentService.listCatalog();
    }

    public static APIResponse<List<Map<String, Object>>> listSections(int courseId) {
        return StudentService.listSections(courseId);
    }

    public static APIResponse<Void> registerSection(int sectionId) {
        int studentId = UserSession.getUserId();
        if (studentId <= 0) return APIResponse.error("Not logged in");
        return StudentService.registerSection(studentId, sectionId);
    }

    public static APIResponse<Void> dropSection(int sectionId) {
        int studentId = UserSession.getUserId();
        if (studentId <= 0) return APIResponse.error("Not logged in");
        return StudentService.dropSection(studentId, sectionId);
    }

    public static APIResponse<List<Map<String, Object>>> myEnrollments() {
        int studentId = UserSession.getUserId();
        if (studentId <= 0) return APIResponse.error("Not logged in");
        return StudentService.myEnrollments(studentId);
    }

    public static APIResponse<List<Map<String, Object>>> myTimetable() {
        int studentId = UserSession.getUserId();
        if (studentId <= 0) return APIResponse.error("Not logged in");
        return StudentService.myTimetable(studentId);
    }

    public static APIResponse<List<Map<String, Object>>> myGrades() {
        int studentId = UserSession.getUserId();
        if (studentId <= 0) return APIResponse.error("Not logged in");
        return StudentService.myGrades(studentId);
    }
}