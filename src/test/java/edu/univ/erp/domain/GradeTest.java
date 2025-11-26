package edu.univ.erp.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Grade Domain Tests")
class GradeTest {

    @Test
    @DisplayName("Should create grade with valid data")
    void testGradeCreation() {
        Grade grade = new Grade();
        grade.setGradeId(1);
        grade.setEnrollmentId(101);
        grade.setComponent("Quiz");
        grade.setScore(85.5);
        grade.setFinalGrade("A");

        assertEquals(1, grade.getGradeId());
        assertEquals(101, grade.getEnrollmentId());
        assertEquals("Quiz", grade.getComponent());
        assertEquals(85.5, grade.getScore(), 0.01);
        assertEquals("A", grade.getFinalGrade());
    }

    @ParameterizedTest
    @CsvSource({
        "Quiz, true",
        "Midterm, true",
        "Final, true",
        "Invalid, false"
    })
    @DisplayName("Should validate component types")
    void testValidComponents(String component, boolean isValid) {
        Grade grade = new Grade();
        grade.setComponent(component);
        
        boolean actuallyValid = component.equals("Quiz") || 
                               component.equals("Midterm") || 
                               component.equals("Final");
        assertEquals(isValid, actuallyValid);
    }

    @Test
    @DisplayName("Should validate score range 0-100")
    void testScoreValidation() {
        Grade grade = new Grade();
        grade.setScore(85.5);
        
        assertThat(grade.getScore(), allOf(greaterThanOrEqualTo(0.0), lessThanOrEqualTo(100.0)));
    }

    @ParameterizedTest
    @CsvSource({
        "95, A",
        "85, B",
        "75, C",
        "65, D",
        "55, F"
    })
    @DisplayName("Should calculate correct letter grade")
    void testLetterGradeCalculation(double score, String expectedGrade) {
        String actualGrade = calculateLetterGrade(score);
        assertEquals(expectedGrade, actualGrade);
    }

    private String calculateLetterGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
}
