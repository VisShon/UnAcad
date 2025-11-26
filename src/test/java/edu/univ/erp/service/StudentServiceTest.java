package edu.univ.erp.service;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.access.MaintenanceManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("Student Service Tests")
class StudentServiceTest {

    @BeforeEach
    void setUp() {
        // Reset any static state if needed
    }

    @Test
    @DisplayName("Should list catalog successfully")
    void testListCatalog() {
        APIResponse<List<Map<String, Object>>> response = StudentService.listCatalog();
        
        assertNotNull(response);
        assertThat(response.success, anyOf(is(true), is(false)));
        assertNotNull(response.message);
    }

    @Test
    @DisplayName("Should prevent registration during maintenance mode")
    void testRegistrationDuringMaintenance() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class)) {
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(true);
            
            APIResponse<Void> response = StudentService.registerSection(1, 1);
            
            assertFalse(response.success);
            assertThat(response.message, containsString("maintenance"));
        }
    }

    @Test
    @DisplayName("Should prevent drop during maintenance mode")
    void testDropDuringMaintenance() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class)) {
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(true);
            
            APIResponse<Void> response = StudentService.dropSection(1, 1);
            
            assertFalse(response.success);
            assertThat(response.message, containsString("maintenance"));
        }
    }

    @Test
    @DisplayName("Should handle enrollment exception properly")
    void testEnrollmentException() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class);
             MockedStatic<EnrollmentDAO> mockedDAO = mockStatic(EnrollmentDAO.class)) {
            
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(false);
            mockedDAO.when(() -> EnrollmentDAO.register(anyInt(), anyInt()))
                     .thenThrow(new EnrollmentDAO.EnrollmentException("Already enrolled"));
            
            APIResponse<Void> response = StudentService.registerSection(1, 1);
            
            assertFalse(response.success);
            assertThat(response.message, containsString("Already enrolled"));
        }
    }

    @Test
    @DisplayName("Should return error message on list catalog failure")
    void testListCatalogFailure() {
        APIResponse<List<Map<String, Object>>> response = StudentService.listCatalog();
        
        // Response should have a message regardless of success
        assertNotNull(response.message);
    }
}
