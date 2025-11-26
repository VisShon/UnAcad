package edu.univ.erp.service;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.access.MaintenanceManager;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.SectionDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin Service Tests")
class AdminServiceTest {

    @Test
    @DisplayName("Should prevent course creation during maintenance")
    void testCreateCourseDuringMaintenance() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class)) {
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(true);
            
            APIResponse<Void> response = AdminService.createCourse("CS101", "Intro to CS", 3);
            
            assertFalse(response.success);
            assertThat(response.message, containsString("maintenance"));
        }
    }

    @Test
    @DisplayName("Should prevent section creation during maintenance")
    void testCreateSectionDuringMaintenance() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class)) {
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(true);
            
            APIResponse<Void> response = AdminService.createSection(1, 1, "MWF 10-11", "Room 101", 30, "Spring", 2025);
            
            assertFalse(response.success);
            assertThat(response.message, containsString("maintenance"));
        }
    }

    @Test
    @DisplayName("Should create course successfully")
    void testCreateCourseSuccess() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class);
             MockedStatic<CourseDAO> mockedDAO = mockStatic(CourseDAO.class)) {
            
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(false);
            mockedDAO.when(() -> CourseDAO.createCourse(anyString(), anyString(), anyInt()))
                     .thenReturn(true);
            
            APIResponse<Void> response = AdminService.createCourse("CS101", "Intro to CS", 3);
            
            assertTrue(response.success);
            assertThat(response.message, containsString("created"));
        }
    }

    @Test
    @DisplayName("Should create section successfully")
    void testCreateSectionSuccess() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class);
             MockedStatic<SectionDAO> mockedDAO = mockStatic(SectionDAO.class)) {
            
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(false);
            mockedDAO.when(() -> SectionDAO.createSection(anyInt(), anyInt(), anyString(), 
                                                         anyString(), anyInt(), anyString(), anyInt()))
                     .thenReturn(true);
            
            APIResponse<Void> response = AdminService.createSection(1, 1, "MWF 10-11", "Room 101", 30, "Spring", 2025);
            
            assertTrue(response.success);
            assertThat(response.message, containsString("created"));
        }
    }

    @Test
    @DisplayName("Should reject non-admin user creation with addUser")
    void testAddUserRejectNonAdmin() {
        APIResponse<Void> response = AdminService.addUser("student1", "STUDENT", "password");
        
        assertFalse(response.success);
        assertThat(response.message, containsString("addStudent or addInstructor"));
    }

    @Test
    @DisplayName("Should list users successfully")
    void testListUsers() {
        APIResponse<List<Map<String, Object>>> response = AdminService.listUsers();
        
        assertNotNull(response);
        assertNotNull(response.message);
    }

    @Test
    @DisplayName("Should list sections successfully")
    void testListSections() {
        APIResponse<List<Map<String, Object>>> response = AdminService.listSections();
        
        assertNotNull(response);
        assertNotNull(response.message);
    }
}
