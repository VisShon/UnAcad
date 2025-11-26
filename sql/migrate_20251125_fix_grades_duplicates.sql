-- Migration: Fix duplicate grade components and add unique constraint
-- Date: 2025-11-25
-- Ensure correct schema selected (prevents ERROR 1046 No database selected)
USE erp_db;
-- 1. Inspect duplicates (optional)
SELECT enrollment_id, component, COUNT(*) AS cnt
FROM grades
GROUP BY enrollment_id, component
HAVING cnt > 1;

-- 2. Remove duplicates, keeping the highest grade_id (assumed latest)
DELETE g1
FROM grades g1
JOIN grades g2
  ON g1.enrollment_id = g2.enrollment_id
 AND g1.component = g2.component
 AND g1.grade_id < g2.grade_id;

-- 3. Add unique constraint if not already present (ignore duplicate error if already exists)
ALTER TABLE grades
ADD UNIQUE KEY uq_enrollment_component (enrollment_id, component);

-- 4. (Optional) Add supporting index if missing (ignore duplicate error if already exists)
ALTER TABLE grades
ADD INDEX idx_grades_enrollment (enrollment_id);

-- 5. Verify result
SELECT enrollment_id, component, COUNT(*) AS cnt
FROM grades
GROUP BY enrollment_id, component
HAVING cnt > 1; -- should return zero rows
