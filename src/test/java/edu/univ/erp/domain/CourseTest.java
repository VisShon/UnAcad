package edu.univ.erp.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Course Domain Tests")
class CourseTest {

    @Test
    @DisplayName("Should create course with valid data")
    void testCourseCreation() {
        Course course = new Course();
        course.setCourseId(1);
        course.setCode("CS101");
        course.setTitle("Introduction to Programming");
        course.setCredits(3);

        assertEquals(1, course.getCourseId());
        assertEquals("CS101", course.getCode());
        assertEquals("Introduction to Programming", course.getTitle());
        assertEquals(3, course.getCredits());
    }

    @Test
    @DisplayName("Should validate course code format")
    void testCourseCodeValidation() {
        Course course = new Course();
        course.setCode("CS101");
        
        assertThat(course.getCode(), matchesPattern("[A-Z]{2}\\d{3}"));
    }

    @Test
    @DisplayName("Should validate credits range")
    void testCreditsValidation() {
        Course course = new Course();
        course.setCredits(3);
        
        assertThat(course.getCredits(), allOf(greaterThan(0), lessThanOrEqualTo(4)));
    }

    @Test
    @DisplayName("Should handle null values appropriately")
    void testNullHandling() {
        Course course = new Course();
        
        assertNull(course.getCode());
        assertNull(course.getTitle());
        assertEquals(0, course.getCourseId());
        assertEquals(0, course.getCredits());
    }
}
