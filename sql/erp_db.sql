-- ERP DB schema + seed data for University ERP (erp_db)
-- This DB stores domain data: students, instructors, courses, sections, enrollments, grades, settings.
-- The auth_db.users.user_id values are matched here via explicit user_id values (no foreign key enforced across DBs).

CREATE DATABASE IF NOT EXISTS erp_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_db;

-- Students table (user_id corresponds to auth_db.users.user_id)
CREATE TABLE IF NOT EXISTS students (
                                        user_id INT PRIMARY KEY,
                                        name VARCHAR(100) NOT NULL,
                                        roll_no VARCHAR(50) NOT NULL UNIQUE,
                                        program VARCHAR(100),
                                        year INT,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Instructors table (instructor_id corresponds to auth_db.users.user_id)
CREATE TABLE IF NOT EXISTS instructors (
                                           instructor_id INT PRIMARY KEY,
                                           department VARCHAR(100),
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Courses
CREATE TABLE IF NOT EXISTS courses (
                                       course_id INT AUTO_INCREMENT PRIMARY KEY,
                                       code VARCHAR(30) NOT NULL UNIQUE,
                                       title VARCHAR(255) NOT NULL,
                                       credits INT NOT NULL,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sections
CREATE TABLE IF NOT EXISTS sections (
                                        section_id INT AUTO_INCREMENT PRIMARY KEY,
                                        course_id INT NOT NULL,
                                        instructor_id INT NULL,
                                        day_time VARCHAR(100),
                                        room VARCHAR(50),
                                        capacity INT NOT NULL DEFAULT 0,
                                        semester VARCHAR(20),
                                        year INT,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
    -- Note: instructor_id points to auth/instructors user id; we do not add FK across DBs here.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Enrollments
CREATE TABLE IF NOT EXISTS enrollments (
                                           enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
                                           student_id INT NOT NULL,
                                           section_id INT NOT NULL,
                                           status VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           UNIQUE KEY uq_student_section (student_id, section_id)
    -- FK constraints can be added within same DB if desired:
    -- FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Grades
CREATE TABLE IF NOT EXISTS grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    component VARCHAR(100) NOT NULL,
    score DOUBLE NULL,
    final_grade VARCHAR(5) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_enrollment_component (enrollment_id, component),
    INDEX idx_grades_enrollment (enrollment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Settings (key/value) e.g., maintenance flag
CREATE TABLE IF NOT EXISTS settings (
                                        `key` VARCHAR(100) PRIMARY KEY,
                                        `value` VARCHAR(100),
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed domain data:
-- Students and Instructors must use the same user_id values as the auth_db.users table inserted earlier.
-- IMPORTANT: roll_no in erp_db.students MUST match username in auth_db.users for student authentication

INSERT INTO students (user_id, name, roll_no, program, year) VALUES
                                                           (3, 'Rahul Kumar', '2025001', 'Computer Science', 2),
                                                           (4, 'Priya Sharma', '2025002', 'Computer Science', 2);

INSERT INTO instructors (instructor_id, department) VALUES
    (2, 'Computer Science');

-- Courses
INSERT INTO courses (course_id, code, title, credits) VALUES
                                                          (1, 'CS101', 'Introduction to Programming', 4),
                                                          (2, 'CS102', 'Data Structures', 4);

-- Sections (one assigned to inst1)
INSERT INTO sections (section_id, course_id, instructor_id, day_time, room, capacity, semester, year) VALUES
                                                                                                          (1, 1, 2, 'Mon/Wed 09:00-10:30', 'Room 101', 30, 'Spring', 2025),
                                                                                                          (2, 2, NULL, 'Tue/Thu 11:00-12:30', 'Room 102', 25, 'Spring', 2025);

-- Enrollments: stu1 enrolled in section 1
INSERT INTO enrollments (enrollment_id, student_id, section_id, status) VALUES
    (1, 3, 1, 'ENROLLED');

-- Grades for enrollment 1 (components for demonstration)
INSERT INTO grades (grade_id, enrollment_id, component, score, final_grade) VALUES
                                                                                (1, 1, 'Quiz 1', 85.0, NULL),
                                                                                (2, 1, 'Midterm', 78.0, NULL),
                                                                                (3, 1, 'Final', 91.0, NULL);

-- Settings: maintenance flag OFF by default
INSERT INTO settings (`key`, `value`) VALUES
    ('maintenance', 'OFF')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

-- Helpful queries for testing:
-- USE erp_db;
-- SELECT * FROM students;
-- SELECT s.section_id, c.code, c.title, s.capacity,
--    (SELECT COUNT(*) FROM enrollments e WHERE e.section_id = s.section_id AND e.status='ENROLLED') AS enrolled
-- FROM sections s JOIN courses c USING (course_id);