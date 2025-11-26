package edu.univ.erp.service;

import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.access.MaintenanceManager;
import edu.univ.erp.data.GradesDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Instructor Service Tests")
class InstructorServiceTest {

    @Test
    @DisplayName("Should prevent score entry during maintenance")
    void testEnterScoreDuringMaintenance() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class)) {
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(true);
            
            APIResponse<Void> response = InstructorService.enterScore(1, "Quiz", 85.0);
            
            assertFalse(response.success);
            assertThat(response.message, containsString("maintenance"));
        }
    }

    @Test
    @DisplayName("Should prevent compute final grade during maintenance")
    void testComputeFinalGradeDuringMaintenance() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class)) {
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(true);
            
            APIResponse<Void> response = InstructorService.computeFinalGrade(1);
            
            assertFalse(response.success);
            assertThat(response.message, containsString("maintenance"));
        }
    }

    @Test
    @DisplayName("Should enter score successfully when not in maintenance")
    void testEnterScoreSuccess() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class);
             MockedStatic<GradesDAO> mockedDAO = mockStatic(GradesDAO.class)) {
            
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(false);
            mockedDAO.when(() -> GradesDAO.insertOrUpdateScore(anyInt(), anyString(), anyDouble()))
                     .thenReturn(true);
            
            APIResponse<Void> response = InstructorService.enterScore(1, "Quiz", 85.0);
            
            assertTrue(response.success);
            assertThat(response.message, containsString("saved"));
        }
    }

    @Test
    @DisplayName("Should handle score entry failure")
    void testEnterScoreFailure() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class);
             MockedStatic<GradesDAO> mockedDAO = mockStatic(GradesDAO.class)) {
            
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(false);
            mockedDAO.when(() -> GradesDAO.insertOrUpdateScore(anyInt(), anyString(), anyDouble()))
                     .thenReturn(false);
            
            APIResponse<Void> response = InstructorService.enterScore(1, "Quiz", 85.0);
            
            assertFalse(response.success);
        }
    }

    @Test
    @DisplayName("Should retrieve class stats successfully")
    void testClassStats() {
        APIResponse<Map<String, Object>> response = InstructorService.classStats(1);
        
        assertNotNull(response);
        assertNotNull(response.message);
    }

    @Test
    @DisplayName("Should compute final grade with proper message")
    void testComputeFinalGradeSuccess() {
        try (MockedStatic<MaintenanceManager> mockedMaintenance = mockStatic(MaintenanceManager.class);
             MockedStatic<GradesDAO> mockedDAO = mockStatic(GradesDAO.class)) {
            
            mockedMaintenance.when(MaintenanceManager::isReadOnly).thenReturn(false);
            mockedDAO.when(() -> GradesDAO.computeFinal(anyInt())).thenReturn("A");
            
            APIResponse<Void> response = InstructorService.computeFinalGrade(1);
            
            assertTrue(response.success);
            assertThat(response.message, containsString("A"));
        }
    }
}
