package edu.univ.erp.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Grade Calculation Integration Tests")
class GradeCalculationIntegrationTest {

    @ParameterizedTest
    @CsvSource({
        "20, 30, 91, 20, 30, 50, 58.5, F",
        "100, 100, 100, 20, 30, 50, 100.0, A",
        "80, 85, 90, 20, 30, 50, 86.5, B",
        "70, 75, 80, 20, 30, 50, 76.5, C",
        "60, 65, 70, 20, 30, 50, 66.5, D",
        "90, 80, 100, 25, 25, 50, 92.5, A",
        "85, 85, 85, 33, 33, 34, 85.0, B"
    })
    @DisplayName("Should calculate weighted final grade correctly")
    void testWeightedGradeCalculation(double quiz, double midterm, double finalExam,
                                     double quizWeight, double midtermWeight, double finalWeight,
                                     double expectedTotal, String expectedGrade) {
        // Normalize weights to decimal
        double qw = quizWeight / 100.0;
        double mw = midtermWeight / 100.0;
        double fw = finalWeight / 100.0;
        
        // Calculate weighted total
        double total = (quiz * qw) + (midterm * mw) + (finalExam * fw);
        
        // Determine letter grade
        String letterGrade = calculateLetterGrade(total);
        
        assertEquals(expectedTotal, total, 0.01, "Total should match expected");
        assertEquals(expectedGrade, letterGrade, "Letter grade should match expected");
    }

    @Test
    @DisplayName("Should handle edge case: all zeros")
    void testAllZeroScores() {
        double total = (0 * 0.20) + (0 * 0.30) + (0 * 0.50);
        String grade = calculateLetterGrade(total);
        
        assertEquals(0.0, total, 0.01);
        assertEquals("F", grade);
    }

    @Test
    @DisplayName("Should handle edge case: perfect scores")
    void testPerfectScores() {
        double total = (100 * 0.20) + (100 * 0.30) + (100 * 0.50);
        String grade = calculateLetterGrade(total);
        
        assertEquals(100.0, total, 0.01);
        assertEquals("A", grade);
    }

    @Test
    @DisplayName("Should validate weights sum to 100%")
    void testWeightsValidation() {
        double quizWeight = 20.0;
        double midtermWeight = 30.0;
        double finalWeight = 50.0;
        
        double sum = quizWeight + midtermWeight + finalWeight;
        
        assertEquals(100.0, sum, 0.01, "Weights should sum to 100%");
    }

    @Test
    @DisplayName("Should handle boundary score of 90 (A grade)")
    void testBoundaryGradeA() {
        String grade = calculateLetterGrade(90.0);
        assertEquals("A", grade);
        
        grade = calculateLetterGrade(89.9);
        assertEquals("B", grade);
    }

    @Test
    @DisplayName("Should handle boundary score of 80 (B grade)")
    void testBoundaryGradeB() {
        String grade = calculateLetterGrade(80.0);
        assertEquals("B", grade);
        
        grade = calculateLetterGrade(79.9);
        assertEquals("C", grade);
    }

    @Test
    @DisplayName("Should handle boundary score of 70 (C grade)")
    void testBoundaryGradeC() {
        String grade = calculateLetterGrade(70.0);
        assertEquals("C", grade);
        
        grade = calculateLetterGrade(69.9);
        assertEquals("D", grade);
    }

    @Test
    @DisplayName("Should handle boundary score of 60 (D grade)")
    void testBoundaryGradeD() {
        String grade = calculateLetterGrade(60.0);
        assertEquals("D", grade);
        
        grade = calculateLetterGrade(59.9);
        assertEquals("F", grade);
    }

    private String calculateLetterGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
}
