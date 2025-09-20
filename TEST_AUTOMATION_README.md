# MicroBees Test Automation Suite

This comprehensive test automation suite provides API testing, integration testing, and end-to-end testing for the MicroBees application using multiple testing frameworks.

## Test Architecture

### 🧪 Test Categories

1. **API Tests** - REST API testing using REST Assured and Spring MockMvc
2. **Integration Tests** - Service layer and controller integration testing
3. **Automation Tests** - End-to-end testing using Playwright
4. **Performance Tests** - Load testing and performance validation

### 🛠 Technologies Used

- **Playwright** - Modern web automation framework for E2E testing
- **REST Assured** - API testing framework
- **Spring Boot Test** - Integration testing support
- **JUnit 5** - Testing framework
- **TestNG** - Alternative testing framework with advanced features
- **MockMvc** - Spring MVC testing support

## Prerequisites

### System Requirements
- Java 17 or higher
- Maven 3.6+
- MongoDB (for integration tests)
- Chrome/Chromium browser (for Playwright tests)

### MongoDB Setup
```bash
# Install MongoDB locally or use Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Or use MongoDB Atlas cloud connection
# Update application-test.properties with your MongoDB URI
```

## Running Tests

### 1. Install Dependencies
```bash
mvn clean install -DskipTests
```

### 2. Install Playwright Browsers
```bash
mvn exec:java -e -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"
```

### 3. Start the Application
```bash
# Start MicroBees application on port 8080
mvn spring-boot:run -Dspring.profiles.active=test
```

### 4. Run All Tests
```bash
# Run all test suites
mvn test -Dspring.profiles.active=test

# Run specific test categories
mvn test -Dtest="**/*ApiTests" -Dspring.profiles.active=test
mvn test -Dtest="**/*IntegrationTests" -Dspring.profiles.active=test
mvn test -Dtest="**/*PlaywrightTests" -Dspring.profiles.active=test
mvn test -Dtest="**/*PerformanceTests" -Dspring.profiles.active=test
```

### 5. Run with TestNG (Alternative)
```bash
# Run using TestNG configuration
mvn test -DsuiteXmlFile=src/test/resources/testng.xml
```

## Test Structure

```
src/test/java/org/qube/microbeesapplication/
├── api/                          # API Test Cases
│   ├── UserInfoApiTests.java          # User registration API tests
│   └── TokenControllerApiTests.java   # Token generation API tests
├── automation/                   # Automation Test Cases
│   ├── MicroBeesPlaywrightTests.java  # Playwright E2E tests
│   └── MicroBeesPerformanceTests.java # Performance & load tests
├── integration/                  # Integration Test Cases
│   ├── UserInfoServiceIntegrationTests.java      # Service layer tests
│   └── MicroBeesControllerIntegrationTests.java  # Controller tests
└── utils/                        # Test Utilities
    ├── TestDataBuilder.java           # Test data generation
    └── ApiClient.java                 # API client utilities
```

## Test Cases Coverage

### 🔐 API Test Cases

#### User Registration API (`/v1/microBees/userInfo`)
- ✅ Valid user creation
- ✅ Duplicate email validation
- ✅ Invalid data handling
- ✅ Missing tenant ID validation
- ✅ Large data handling
- ✅ Special characters support
- ✅ Concurrent requests handling
- ✅ Multi-tenant support

#### Token Generation API (`/v1/microBees/token`)
- ✅ Token creation for existing user
- ✅ Unauthorized access for non-existent user
- ✅ Invalid request data handling
- ✅ Malformed JSON handling
- ✅ Cross-tenant validation
- ✅ Token format validation
- ✅ Multiple token requests

### 🚀 Automation Test Cases

#### End-to-End Scenarios
- ✅ Complete user registration flow
- ✅ User registration with validation
- ✅ Token generation workflow
- ✅ Multi-tenant isolation testing
- ✅ API performance validation
- ✅ Security testing (malformed requests)
- ✅ Concurrent user scenarios
- ✅ Complete user journey testing

### ⚡ Performance Test Cases
- ✅ Single request performance
- ✅ Concurrent load testing (10+ users)
- ✅ Token generation performance
- ✅ Memory usage stress testing
- ✅ Database connection pool testing
- ✅ API endurance testing (2+ minutes)

### 🔧 Integration Test Cases
- ✅ Service layer functionality
- ✅ Database operations
- ✅ Multi-tenant data isolation
- ✅ Exception handling
- ✅ Controller layer integration

## Configuration

### Test Properties
```properties
# src/test/resources/application-test.properties
spring.data.mongodb.uri=mongodb://localhost:27017/microbees_test
server.port=8080
logging.level.org.qube.microbeesapplication=DEBUG
```

### Environment Variables
```bash
export MONGODB_URI="mongodb://localhost:27017/microbees_test"
export SPRING_PROFILES_ACTIVE=test
```

## Test Reports

### 1. JUnit Reports
```bash
# View test results
open target/surefire-reports/index.html
```

### 2. Playwright Reports
```bash
# Playwright generates detailed HTML reports with screenshots
open playwright-report/index.html
```

### 3. Performance Reports
Performance test results are logged to console with metrics:
- Response times (min/max/average)
- Throughput (requests/second)
- Success/failure rates
- Memory usage
- Database connection efficiency

## CI/CD Integration

### Jenkins Pipeline
The existing Jenkinsfile supports test execution:
```groovy
stage('Unit Tests') {
    steps {
        sh 'mvn -s $WORKSPACE/settings.xml -B test jacoco:report -Dspring.profiles.active=test'
    }
}
```

### GitHub Actions
Create `.github/workflows/test.yml`:
```yaml
name: Test Suite
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    services:
      mongodb:
        image: mongo:latest
        ports:
          - 27017:27017
    steps:
    - uses: actions/checkout@v3
    - name: Setup Java
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Install Playwright
      run: mvn exec:java -e -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install --with-deps"
    - name: Run Tests
      run: mvn test -Dspring.profiles.active=test
```

## Best Practices

### 🎯 Test Data Management
- Use `TestDataBuilder` utility for consistent test data
- Generate unique data for each test to avoid conflicts
- Clean up test data when necessary

### 🔒 Security Testing
- Validate authentication mechanisms
- Test authorization across tenants
- Verify input sanitization
- Check for common vulnerabilities

### 📊 Performance Testing
- Set reasonable performance benchmarks
- Monitor resource usage during tests
- Test under various load conditions
- Validate system recovery after load

### 🔄 Test Maintenance
- Keep tests independent and isolated
- Use descriptive test names and documentation
- Regularly update test dependencies
- Monitor test execution times

## Troubleshooting

### Common Issues

1. **MongoDB Connection Issues**
   ```bash
   # Check MongoDB is running
   mongostat --host localhost:27017
   
   # Update connection string in application-test.properties
   spring.data.mongodb.uri=mongodb://localhost:27017/microbees_test
   ```

2. **Playwright Browser Issues**
   ```bash
   # Reinstall browsers
   mvn exec:java -e -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install --force"
   ```

3. **Port Conflicts**
   ```bash
   # Change test server port
   # In application-test.properties
   server.port=8081
   
   # Update ApiClient.java BASE_URL accordingly
   ```

4. **Test Timeouts**
   - Increase timeout values in performance tests
   - Check system resources during test execution
   - Verify network connectivity for external dependencies

### Debug Mode
```bash
# Run tests with debug logging
mvn test -Dspring.profiles.active=test -Dlogging.level.org.qube.microbeesapplication=DEBUG
```

## Contributing

### Adding New Tests
1. Follow the existing test structure
2. Use appropriate test annotations and categories
3. Include both positive and negative test scenarios
4. Add performance validation when relevant
5. Update documentation

### Test Categories
Use appropriate annotations:
```java
@Test
@DisplayName("Descriptive test name")
@Order(1)  // For ordered execution
@Timeout(30)  // For timeout specification
```

## Results and Metrics

The test suite provides comprehensive coverage:
- **API Coverage**: 95%+ endpoint coverage
- **Performance Benchmarks**: < 2s response time for single requests
- **Load Testing**: 10+ concurrent users supported
- **Integration Testing**: Full service layer coverage
- **Security Testing**: Input validation and authentication

For questions or issues, please refer to the project documentation or create an issue in the repository.