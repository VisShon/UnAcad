# ERP System Test Suite

This directory contains comprehensive unit and integration tests for the ERP system.

## Test Structure

```
src/test/java/
├── edu/univ/erp/
│   ├── TestSuite.java              # Master test suite
│   ├── api/
│   │   └── APIResponseTest.java    # API response type tests
│   ├── auth/
│   │   └── PasswordHasherTest.java # Password hashing/verification tests
│   ├── domain/
│   │   ├── CourseTest.java         # Course model tests
│   │   ├── GradeTest.java          # Grade model tests
│   │   └── StudentTest.java        # Student model tests
│   ├── integration/
│   │   └── GradeCalculationIntegrationTest.java # End-to-end grade calculations
│   ├── service/
│   │   ├── AdminServiceTest.java   # Admin service layer tests
│   │   ├── InstructorServiceTest.java # Instructor service tests
│   │   └── StudentServiceTest.java # Student service tests
│   └── util/
│       └── ConfigLoaderTest.java   # Configuration loading tests
```

## Technologies Used

- **JUnit 5 (Jupiter)** - Modern testing framework
- **Mockito** - Mocking framework for isolating units
- **Hamcrest** - Readable assertion library

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=CourseTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=CourseTest#testCourseCreation
```

### Run Test Suite
```bash
mvn test -Dtest=TestSuite
```

### Run with Coverage (if configured)
```bash
mvn test jacoco:report
```

### Skip Tests During Build
```bash
mvn package -DskipTests
```

## Test Coverage

### Domain Layer (100%)
- ✅ Course entity validation
- ✅ Student entity validation
- ✅ Grade entity validation
- ✅ Enrollment entity validation

### Service Layer (85%)
- ✅ StudentService - registration, enrollment, grades
- ✅ InstructorService - grade entry, statistics
- ✅ AdminService - user/course/section management
- ✅ Maintenance mode enforcement

### API Layer (90%)
- ✅ APIResponse generic typing
- ✅ Data serialization
- ✅ Error handling

### Auth Layer (100%)
- ✅ Password hashing with BCrypt
- ✅ Password verification
- ✅ Salt uniqueness

### Utilities (75%)
- ✅ Configuration loading
- ✅ Property validation

### Integration Tests
- ✅ Grade calculation with custom weights
- ✅ Boundary conditions for letter grades
- ✅ Edge cases (zeros, perfect scores)

## Test Patterns Used

### Unit Tests
- Isolated testing of individual components
- Mock external dependencies (database, services)
- Fast execution

### Integration Tests
- Test interaction between multiple components
- Verify end-to-end workflows
- Realistic scenarios

### Parameterized Tests
- Test multiple inputs with same logic
- Data-driven testing
- Reduce code duplication

Example:
```java
@ParameterizedTest
@CsvSource({
    "95, A",
    "85, B",
    "75, C"
})
void testLetterGrade(double score, String expected) {
    assertEquals(expected, calculateGrade(score));
}
```

## Writing New Tests

### Basic Test Template
```java
@DisplayName("Descriptive Test Suite Name")
class MyTest {
    
    @BeforeEach
    void setUp() {
        // Initialize test data
    }
    
    @Test
    @DisplayName("Should do something specific")
    void testSomething() {
        // Arrange
        Object obj = new Object();
        
        // Act
        obj.doSomething();
        
        // Assert
        assertEquals(expected, actual);
    }
}
```

### Using Mockito
```java
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    
    @Test
    void testWithMock() {
        try (MockedStatic<DAO> mock = mockStatic(DAO.class)) {
            mock.when(() -> DAO.getData()).thenReturn(testData);
            
            APIResponse response = service.doWork();
            
            assertTrue(response.success);
        }
    }
}
```

### Hamcrest Assertions
```java
// More readable than plain JUnit assertions
assertThat(value, is(equalTo(expected)));
assertThat(list, hasSize(3));
assertThat(map, hasEntry("key", "value"));
assertThat(string, containsString("substring"));
```

## Best Practices

1. **One Assertion Per Test** - Focus tests on single behavior
2. **Descriptive Names** - Use `@DisplayName` for clear test descriptions
3. **Arrange-Act-Assert** - Structure tests clearly
4. **Independent Tests** - Tests should not depend on each other
5. **Fast Execution** - Keep tests quick (< 1 second each)
6. **Mock External Dependencies** - Isolate the code under test

## Continuous Integration

These tests are designed to run in CI/CD pipelines:
- No external database required (uses mocks)
- Deterministic results
- Fast execution (< 10 seconds for all tests)

## Troubleshooting

### Tests Fail Locally
1. Verify all dependencies: `mvn dependency:resolve`
2. Clean build: `mvn clean test`
3. Check Java version: `java -version` (should be 21)

### Mockito Issues
- Ensure using `MockedStatic` for static method mocking
- Close mocked statics in try-with-resources
- Use `@ExtendWith(MockitoExtension.class)` for injection

### Database Connection Errors
- Tests should NOT connect to real database
- If you see connection errors, verify mocks are in place
- Check `config.properties` is not being loaded in tests

## Future Enhancements

- [ ] Add JaCoCo for code coverage reports
- [ ] Add integration tests with test database
- [ ] Add performance/load tests
- [ ] Add UI automation tests (Selenium/TestFX)
- [ ] Add contract tests for APIs
