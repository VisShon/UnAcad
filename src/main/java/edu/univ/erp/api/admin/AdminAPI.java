package edu.univ.erp.api.admin;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.api.maintenance.MaintenanceAPI;
import edu.univ.erp.service.AdminService;

import java.util.List;
import java.util.Map;

public class AdminAPI {

    public static APIResponse<Void> addUser(String username, String role, String plainPassword) {
        return AdminService.addUser(username, role, plainPassword);
    }

    public static APIResponse<Void> addStudent(String name, String rollNo, String password, String program, int year) {
        return AdminService.addStudent(name, rollNo, password, program, year);
    }

    public static APIResponse<Void> addInstructor(String username, String password, String department) {
        return AdminService.addInstructor(username, password, department);
    }

    public static APIResponse<Void> createCourse(String code, String title, int credits) {
        return AdminService.createCourse(code, title, credits);
    }

    public static APIResponse<Void> createSection(int courseId, int instructorId, String dayTime, String room, int cap, String semester, int year) {
        return AdminService.createSection(courseId, instructorId, dayTime, room, cap, semester, year);
    }

    public static APIResponse<Void> toggleMaintenance(boolean on) {
        return MaintenanceAPI.setMaintenance(on);
    }

    public static APIResponse<List<Map<String, Object>>> listUsers() {
        return AdminService.listUsers();
    }

    public static APIResponse<List<Map<String, Object>>> listSections() {
        return AdminService.listSections();
    }
}