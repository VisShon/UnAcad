package edu.univ.erp.service;

import edu.univ.erp.access.MaintenanceManager;
import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.api.types.CourseRow;
import edu.univ.erp.api.types.SectionRow;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.GradesDAO;
import edu.univ.erp.data.SectionDAO;

import java.util.List;
import java.util.Map;

/**
 * Handles all student-related business logic:
 * catalog, sections, register, drop, timetable, grades.
 */

public class StudentService {

    @SuppressWarnings("unchecked")
    public static APIResponse<List<Map<String, Object>>> listCatalog() {
        try {
            List<CourseRow> rows = CourseDAO.listCourses();
            return APIResponse.<List<Map<String, Object>>>success("Catalog loaded").withData((List<Map<String, Object>>)(List<?>)rows);
        } catch (Exception e) {
            return APIResponse.error("Failed to load catalog: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static APIResponse<List<Map<String, Object>>> listSections(int courseId) {
        try {
            List<SectionRow> rows = SectionDAO.listSectionsByCourse(courseId);
            return APIResponse.<List<Map<String, Object>>>success("Sections loaded").withData((List<Map<String, Object>>)(List<?>)rows);
        } catch (Exception e) {
            return APIResponse.error("Failed to load sections: " + e.getMessage());
        }
    }

    public static APIResponse<Void> registerSection(int studentId, int sectionId) {
        if (MaintenanceManager.isReadOnly())
            return APIResponse.error("System is in maintenance mode");

        try {
            boolean ok = EnrollmentDAO.register(studentId, sectionId);
            return ok ? APIResponse.success("Registered successfully")
                    : APIResponse.error("Registration failed");
        } catch (EnrollmentDAO.EnrollmentException ex) {
            return APIResponse.error(ex.getMessage());
        } catch (Exception e) {
            return APIResponse.error("Registration failed: " + e.getMessage());
        }
    }

    public static APIResponse<Void> dropSection(int studentId, int sectionId) {
        if (MaintenanceManager.isReadOnly())
            return APIResponse.error("System is in maintenance mode");

        try {
            boolean ok = EnrollmentDAO.drop(studentId, sectionId);
            return ok ? APIResponse.success("Dropped successfully")
                    : APIResponse.error("Drop failed");
        } catch (Exception e) {
            return APIResponse.error("Drop failed: " + e.getMessage());
        }
    }

    public static APIResponse<List<Map<String, Object>>> myEnrollments(int studentId) {
        try {
            List<Map<String, Object>> rows = EnrollmentDAO.listEnrollmentsForStudent(studentId);
            return APIResponse.<List<Map<String, Object>>>success("Enrollments loaded").withData(rows);
        } catch (Exception e) {
            return APIResponse.error("Failed to load enrollments: " + e.getMessage());
        }
    }

    public static APIResponse<List<Map<String, Object>>> myTimetable(int studentId) {
        try {
            List<Map<String, Object>> rows = EnrollmentDAO.timetableForStudent(studentId);
            return APIResponse.<List<Map<String, Object>>>success("Timetable loaded").withData(rows);
        } catch (Exception e) {
            return APIResponse.error("Failed to load timetable: " + e.getMessage());
        }
    }

    public static APIResponse<List<Map<String, Object>>> myGrades(int studentId) {
        try {
            List<Map<String, Object>> rows = GradesDAO.getGradesForStudent(studentId);
            return APIResponse.<List<Map<String, Object>>>success("Grades loaded").withData(rows);
        } catch (Exception e) {
            return APIResponse.error("Failed to load grades: " + e.getMessage());
        }
    }
}