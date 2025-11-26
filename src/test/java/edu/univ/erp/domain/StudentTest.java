package edu.univ.erp.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Student Domain Tests")
class StudentTest {

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student();
    }

    @Test
    @DisplayName("Should create student with valid data")
    void testStudentCreation() {
        student.setUserId(1);
        student.setName("Rahul Kumar");
        student.setRollNo("2025CS001");
        student.setProgram("Computer Science");
        student.setYear(1);

        assertEquals(1, student.getUserId());
        assertEquals("Rahul Kumar", student.getName());
        assertEquals("2025CS001", student.getRollNo());
        assertEquals("Computer Science", student.getProgram());
        assertEquals(1, student.getYear());
    }

    @Test
    @DisplayName("Should validate student name is not null or empty")
    void testNameValidation() {
        student.setName("Rahul Kumar");
        
        assertThat(student.getName(), not(emptyString()));
        assertThat(student.getName(), notNullValue());
    }

    @Test
    @DisplayName("Should validate roll number format")
    void testRollNumberFormat() {
        student.setRollNo("2025CS001");
        
        assertThat(student.getRollNo(), matchesPattern("\\d{4}[A-Z]{2}\\d{3}"));
    }

    @Test
    @DisplayName("Should validate year range")
    void testYearValidation() {
        student.setYear(2);
        
        assertThat(student.getYear(), allOf(greaterThan(0), lessThanOrEqualTo(4)));
    }

    @Test
    @DisplayName("Should handle student program")
    void testProgramHandling() {
        String program = "Computer Science";
        student.setProgram(program);
        
        assertThat(student.getProgram(), equalTo(program));
        assertThat(student.getProgram(), not(emptyString()));
    }
}
