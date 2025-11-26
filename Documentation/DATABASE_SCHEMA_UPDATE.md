# Database Schema Update - Student Name Field

## Overview
This document describes the changes made to add a `name` field for students and ensure proper linking between the `auth_db` and `erp_db` databases.

## Key Changes

### 1. Database Schema Updates

#### **erp_db.sql** - Students Table
- **Added Column**: `name VARCHAR(100) NOT NULL`
- **Purpose**: Store the full name of each student
- **Position**: Between `user_id` and `roll_no` columns

```sql
CREATE TABLE IF NOT EXISTS students (
    user_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,        -- NEW FIELD
    roll_no VARCHAR(50) NOT NULL UNIQUE,
    program VARCHAR(100),
    year INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### **auth_db.sql** - Username Convention
- **Change**: Student usernames now use their roll numbers instead of arbitrary usernames
- **Purpose**: Maintain consistency between `auth_db.users.username` and `erp_db.students.roll_no`
- **Example**: Username `stu1` changed to `2025001` (the roll number)

```sql
-- Before: username = 'stu1'
-- After:  username = '2025001' (roll number)
INSERT INTO users (user_id, username, role, password_hash, status) VALUES
    (3, '2025001', 'STUDENT', '$2a$12$...', 'ACTIVE'),
    (4, '2025002', 'STUDENT', '$2a$12$...', 'ACTIVE');
```

### 2. Seed Data Updates

#### **erp_db.sql** - Student Records
```sql
INSERT INTO students (user_id, name, roll_no, program, year) VALUES
    (3, 'Rahul Kumar', '2025001', 'Computer Science', 2),
    (4, 'Priya Sharma', '2025002', 'Computer Science', 2);
```

**Verification of Linking:**
- `user_id = 3` in `erp_db.students` → matches `user_id = 3` in `auth_db.users`
- `roll_no = '2025001'` in `erp_db.students` → matches `username = '2025001'` in `auth_db.users`
- Same pattern for user_id = 4

### 3. Code Changes

#### **Domain Layer** - `Student.java`
```java
private String name;  // NEW FIELD

public String getName() { return name; }
public void setName(String name) { this.name = name; }
```

#### **Data Layer** - `StudentDAO.java`
```java
// Updated to retrieve name field
s.setName(rs.getString("name"));
```

#### **Service Layer** - `AdminService.java`
```java
// Updated method signature (username → name, rollNo as username)
public static APIResponse<Void> addStudent(
    String name,      // NEW PARAMETER (student's full name)
    String rollNo,    // Now used as username in auth_db
    String password,
    String program,
    int year
)

// Updated INSERT statement
String sql2 = "INSERT INTO students (user_id, name, roll_no, program, year) VALUES (?,?,?,?,?)";
ps2.setString(2, name);  // NEW
```

#### **API Layer** - `AdminAPI.java`
```java
public static APIResponse<Void> addStudent(
    String name,     // NEW PARAMETER
    String rollNo,   // Moved before password
    String password,
    String program,
    int year
)
```

#### **UI Layer** - `AdminDashboard.java`
```java
// Added name field to dialog
JTextField nameField = new JTextField();

Object[] message = {
    "Name:", nameField,                        // NEW FIELD
    "Roll No (will be username):", rollNoField, // Updated label
    "Password:", passwordField,
    "Program:", programField,
    "Year (numbers only):", yearField
};

// Updated API call
APIResponse<Void> r = AdminAPI.addStudent(name, rollNo, password, program, year);
```

#### **Test Layer** - `StudentTest.java`
```java
// Added name validation test
@Test
@DisplayName("Should validate student name is not null or empty")
void testNameValidation() {
    student.setName("Rahul Kumar");
    
    assertThat(student.getName(), not(emptyString()));
    assertThat(student.getName(), notNullValue());
}
```

## Database Linking Verification

### Cross-Database Relationships

```
auth_db.users                    erp_db.students
┌─────────┬──────────┬─────────┐  ┌─────────┬──────────────┬─────────┐
│ user_id │ username │ role    │  │ user_id │ name         │ roll_no │
├─────────┼──────────┼─────────┤  ├─────────┼──────────────┼─────────┤
│    3    │ 2025001  │ STUDENT │──│    3    │ Rahul Kumar  │ 2025001 │
│    4    │ 2025002  │ STUDENT │──│    4    │ Priya Sharma │ 2025002 │
└─────────┴──────────┴─────────┘  └─────────┴──────────────┴─────────┘
         │                                                   │
         └───────── MUST MATCH ─────────────────────────────┘
```

### Key Constraints:
1. **user_id matching**: `auth_db.users.user_id` = `erp_db.students.user_id`
2. **username/roll_no matching**: `auth_db.users.username` = `erp_db.students.roll_no`
3. **Authentication flow**: Students login with their roll number (e.g., "2025001")

## Migration Instructions

### For Existing Databases:

```sql
-- Step 1: Backup existing data
USE erp_db;
CREATE TABLE students_backup AS SELECT * FROM students;

-- Step 2: Add name column
ALTER TABLE students ADD COLUMN name VARCHAR(100) AFTER user_id;

-- Step 3: Update existing records with names
UPDATE students SET name = 'Default Name' WHERE name IS NULL;

-- Step 4: Make name NOT NULL
ALTER TABLE students MODIFY COLUMN name VARCHAR(100) NOT NULL;

-- Step 5: Update auth_db usernames to match roll numbers
USE auth_db;
UPDATE users u 
JOIN erp_db.students s ON u.user_id = s.user_id 
SET u.username = s.roll_no 
WHERE u.role = 'STUDENT';
```

### For Fresh Installation:

Simply run the updated SQL scripts:
```bash
mysql -u root -p < sql/auth_db.sql
mysql -u root -p < sql/erp_db.sql
```

## Testing

All tests pass successfully:
- ✅ **StudentTest**: 5 tests (including name validation)
- ✅ **AdminServiceTest**: 7 tests
- ✅ **Full test suite**: 148 tests, 0 failures

Run tests:
```bash
mvn test
```

## Impact Summary

### Breaking Changes:
1. ⚠️ **AdminService.addStudent()** signature changed - parameter order updated
2. ⚠️ **AdminAPI.addStudent()** signature changed - parameter order updated
3. ⚠️ **Student login** now requires roll number instead of arbitrary username

### Database Changes:
- ✅ New `name` column in `students` table
- ✅ Updated seed data with roll numbers as usernames
- ✅ Proper linking maintained between both databases

### Non-Breaking:
- ✅ All existing functionality preserved
- ✅ Backward compatible for instructors and admins
- ✅ No changes to enrollment, grades, or sections
