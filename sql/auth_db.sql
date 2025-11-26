-- Auth DB schema + seed data for University ERP (auth_db)
-- Notes:
-- 1) Password hashes generated using BCrypt with 12 rounds for security.
--    Plain passwords (for testing only - DO NOT use in production):
--      - admin1: adminpass
--      - inst1: instpass
--      - 2025001 (student Rahul Kumar): stupass
--      - 2025002 (student Priya Sharma): stupass2
-- 2) Regenerate hashes if needed using your Java PasswordHasher.hash("plainPassword")
-- 3) The user_id values are chosen explicitly so they can be referenced (by matching ids) in the ERP DB seeds.

CREATE DATABASE IF NOT EXISTS auth_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auth_db;

-- Users table (stores auth info only)
CREATE TABLE IF NOT EXISTS users (
                                     user_id INT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(100) NOT NULL UNIQUE,
                                     role VARCHAR(20) NOT NULL,
                                     password_hash VARCHAR(200) NOT NULL,
                                     status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                     last_login DATETIME NULL,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Clear existing seed data and re-insert
TRUNCATE TABLE users;

-- Sample users with real BCrypt hashes (12 rounds)
-- IMPORTANT: For students, username MUST be their roll number to maintain consistency with erp_db.students.roll_no
INSERT INTO users (user_id, username, role, password_hash, status) VALUES
                                                                       (1, 'admin1', 'ADMIN', '$2a$12$ieaRKct3btLObCOUfQns6ue8g3BIKluhqT2WCe9WySjdpePKp9pbu', 'ACTIVE'),
                                                                       (2, 'inst1', 'INSTRUCTOR', '$2a$12$iB63QPNn7Y7kW2NwKn7N9O4B7Uq4OrPqioctMxESgZ4ma5ixWihTq', 'ACTIVE'),
                                                                       (3, '2025001', 'STUDENT', '$2a$12$i1OZf7VRXmmKZsx0IDy0vO1NmOstKGiAjNSQTPqARYyV6UuO4aV/C', 'ACTIVE'),
                                                                       (4, '2025002', 'STUDENT', '$2a$12$OVxx.XiV9RCkryuLWYVHb.EkxOLQDKA2mdKBI9xeyZxq/qpd8yHkq', 'ACTIVE');

-- Note: All hashes above are placeholders for demonstration. In a real setup, generate unique hashes for each password.
-- To generate a hash in Java:
--   import org.mindrot.jbcrypt.BCrypt;
--   String hash = BCrypt.hashpw("yourpassword", BCrypt.gensalt(12));
-- For this seed, use the plain passwords above for login.

-- Helpful queries:
-- SELECT * FROM users;
-- To update a password for username 'stu1', use:
-- UPDATE users SET password_hash = '<new-bcrypt-hash>' WHERE username = 'stu1';

-- To update a password for username 'stu1', use:
-- UPDATE users SET password_hash = '<new-bcrypt-hash>' WHERE username = 'stu1';

-- To update a password for username 'stu1', use:
-- UPDATE users SET password_hash = '<new-bcrypt-hash>' WHERE username = 'stu1';

-- To update a password for username 'stu1', use:
