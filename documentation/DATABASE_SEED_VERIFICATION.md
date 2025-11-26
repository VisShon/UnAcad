# Database Seed Data Cross-Reference

## Verification Table: auth_db ↔ erp_db Linking

### Student Records

| auth_db.users |            |          | erp_db.students |                |          |            |      | Status |
|---------------|------------|----------|-----------------|----------------|----------|------------|------|--------|
| user_id       | username   | role     | user_id         | name           | roll_no  | program    | year | ✓/✗    |
| 3             | **2025001** | STUDENT  | 3               | Rahul Kumar    | **2025001** | CS         | 2    | ✅     |
| 4             | **2025002** | STUDENT  | 4               | Priya Sharma   | **2025002** | CS         | 2    | ✅     |

**Verification Points:**
- ✅ `user_id` values match across both databases (3, 4)
- ✅ `username` in auth_db equals `roll_no` in erp_db
- ✅ Students can login using their roll numbers
- ✅ No orphaned records

### Instructor Records

| auth_db.users |          |            | erp_db.instructors |              | Status |
|---------------|----------|------------|--------------------|--------------| ------ |
| user_id       | username | role       | instructor_id      | department   | ✓/✗    |
| 2             | inst1    | INSTRUCTOR | 2                  | CS           | ✅     |

**Verification Points:**
- ✅ `user_id` matches `instructor_id` (value = 2)
- ✅ Instructor record properly linked

### Admin Records

| auth_db.users |          |        | Notes                  |
|---------------|----------|--------|------------------------|
| user_id       | username | role   |                        |
| 1             | admin1   | ADMIN  | No ERP profile needed  |

**Verification Points:**
- ✅ Admin user created in auth_db
- ℹ️ Admin doesn't need ERP profile (no students/instructors entry)

## Login Credentials for Testing

| Role       | Username (Login) | Password | Database Records |
|------------|------------------|----------|------------------|
| Admin      | admin1           | adminpass | auth_db only     |
| Instructor | inst1            | instpass  | auth_db + instructors |
| Student 1  | **2025001**      | stupass   | auth_db + students |
| Student 2  | **2025002**      | stupass2  | auth_db + students |

**Important Notes:**
1. Students must login with their **roll number**, not a separate username
2. Roll numbers are used as usernames in `auth_db.users`
3. This ensures consistency when authenticating students

## Database Integrity Rules

### Primary Linking Rules
```
1. students.user_id = users.user_id (WHERE role = 'STUDENT')
2. students.roll_no = users.username (WHERE role = 'STUDENT')
3. instructors.instructor_id = users.user_id (WHERE role = 'INSTRUCTOR')
```

### When Adding New Student via UI
```java
// AdminService.addStudent(name, rollNo, password, program, year)
1. INSERT auth_db.users: username=rollNo, role='STUDENT'
2. Get generated user_id
3. INSERT erp_db.students: user_id=<generated>, name=name, roll_no=rollNo
```

**This ensures:**
- Roll number is always the username
- user_id links both databases
- Student can login with roll number

## Testing Database Linking

### SQL Verification Query
```sql
-- Check all students are properly linked
SELECT 
    u.user_id,
    u.username AS 'auth_username',
    u.role,
    s.user_id AS 'student_user_id',
    s.name,
    s.roll_no AS 'erp_roll_no',
    CASE 
        WHEN u.user_id = s.user_id AND u.username = s.roll_no 
        THEN '✅ LINKED' 
        ELSE '❌ BROKEN' 
    END AS status
FROM auth_db.users u
LEFT JOIN erp_db.students s ON u.user_id = s.user_id
WHERE u.role = 'STUDENT';
```

Expected Output:
```
+----------+----------------+---------+-----------------+---------------+-------------+-----------+
| user_id  | auth_username  | role    | student_user_id | name          | erp_roll_no | status    |
+----------+----------------+---------+-----------------+---------------+-------------+-----------+
| 3        | 2025001        | STUDENT | 3               | Rahul Kumar   | 2025001     | ✅ LINKED |
| 4        | 2025002        | STUDENT | 4               | Priya Sharma  | 2025002     | ✅ LINKED |
+----------+----------------+---------+-----------------+---------------+-------------+-----------+
```

### Check Instructor Linking
```sql
-- Check all instructors are properly linked
SELECT 
    u.user_id,
    u.username,
    u.role,
    i.instructor_id,
    i.department,
    CASE 
        WHEN u.user_id = i.instructor_id 
        THEN '✅ LINKED' 
        ELSE '❌ BROKEN' 
    END AS status
FROM auth_db.users u
LEFT JOIN erp_db.instructors i ON u.user_id = i.instructor_id
WHERE u.role = 'INSTRUCTOR';
```

Expected Output:
```
+----------+----------+------------+---------------+-------------+-----------+
| user_id  | username | role       | instructor_id | department  | status    |
+----------+----------+------------+---------------+-------------+-----------+
| 2        | inst1    | INSTRUCTOR | 2             | CS          | ✅ LINKED |
+----------+----------+------------+---------------+-------------+-----------+
```

## Common Issues and Solutions

### Issue 1: Student can't login
**Symptom:** Student enters roll number but authentication fails
**Check:**
```sql
SELECT user_id, username, role FROM auth_db.users WHERE username = '2025001';
```
**Solution:** Ensure username in auth_db matches roll_no

### Issue 2: Student profile not found after login
**Symptom:** Login succeeds but no student data
**Check:**
```sql
SELECT * FROM erp_db.students WHERE user_id = 3;
```
**Solution:** Ensure user_id exists in students table

### Issue 3: Username mismatch
**Symptom:** user_id matches but username ≠ roll_no
**Fix:**
```sql
UPDATE auth_db.users u
JOIN erp_db.students s ON u.user_id = s.user_id
SET u.username = s.roll_no
WHERE u.role = 'STUDENT' AND u.username != s.roll_no;
```
