# MicroBees Application Testing Guide

## Overview
This project contains comprehensive automation test cases for the MicroBees Spring Boot microservice application. The tests are designed to verify functionality without requiring external dependencies like MongoDB.

## Test Structure

### 🧪 Test Classes Created

1. **Controller Tests**
   - `UserInfoControllerTest` - Tests REST API endpoints with mocking
   - `UserInfoControllerIntegrationTest` - Integration tests for the API layer

2. **Service Tests** 
   - `UserInfoServiceTest` - Unit tests for business logic
   - `MicroBeesMapperTest` - Tests for DTO ↔ JPA entity mapping

3. **Configuration Tests**
   - `MultiTenantMongoTemplateTest` - Tests for MongoDB multi-tenancy setup
   - `TestConfig` - Test configuration with mocked dependencies

4. **Application Tests**
   - `MicroBeesApplicationTests` - Context loading and basic application tests
   - `MicroBeesTestSuite` - Documentation and test organization

### 📁 Test Categories

#### Unit Tests
- Test individual components in isolation
- Use Mockito for mocking dependencies
- Fast execution without external dependencies

#### Integration Tests  
- Test component interactions
- Mock external services (MongoDB, etc.)
- Verify API contract compliance

#### Configuration Tests
- Test Spring Boot configuration
- Verify dependency injection
- Test multi-tenant setup

## 🚀 How to Run Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=MicroBeesMapperTest
```

### Run Tests Without MongoDB Dependencies
```bash
mvn test -Dspring.profiles.active=none
```

### Run Tests with Coverage Report
```bash
mvn test jacoco:report
```

### View Coverage Report
After running tests with coverage, open:
```
target/site/jacoco/index.html
```

## 🎯 Test Coverage

The test suite covers:
- ✅ REST API endpoints (`/v1/microBees/userInfo`, `/v1/microBees/token`)
- ✅ Service layer business logic
- ✅ Data mapping between DTOs and entities  
- ✅ Error handling (duplicate keys, validation, etc.)
- ✅ Multi-tenant MongoDB configuration
- ✅ Security configuration
- ✅ Application context loading

## 🏗️ Test Features

### Mocking Strategy
- MongoDB connections are mocked to avoid database dependencies
- External services are mocked for isolated testing
- Spring Boot test slices for focused testing

### Error Scenarios Tested
- Invalid input data
- Duplicate key exceptions
- Database connection failures
- Missing required parameters
- Authentication/authorization failures

### Edge Cases
- Null/empty inputs
- Special characters in data
- Long strings exceeding validation limits
- Invalid tenant IDs

## 📊 Running Tests in CI/CD

These tests are designed to work in Jenkins pipeline:

```bash
# In Jenkinsfile
mvn test jacoco:report -Dspring.profiles.active=test
```

Coverage threshold is set to 60% in the Jenkins pipeline.

## 🛠️ Adding New Tests

To add new test cases:

1. **Controller Tests**: Add to `UserInfoControllerTest`
2. **Service Tests**: Add to respective service test classes  
3. **Integration Tests**: Add to `UserInfoControllerIntegrationTest`
4. **Configuration Tests**: Add to `MultiTenantMongoTemplateTest`

### Test Naming Convention
- `methodName_condition_expectedResult()`
- Example: `createUser_DuplicateKeyException_ReturnsBadRequest()`

## 🔍 Test Utilities

### Mock Objects
- `@MockBean` for Spring components
- `@Mock` for regular mocks
- `MockMvc` for API testing

### Test Data
- Standardized test DTOs and entities
- Reusable test data builders
- Proper cleanup between tests

## 📝 Notes

- Tests are designed to run without actual MongoDB instance
- All external dependencies are mocked
- Tests follow Spring Boot testing best practices
- Coverage reports include detailed line-by-line analysis

## 🚨 Troubleshooting

### Common Issues

1. **MongoDB Connection Errors**: Tests are designed to mock MongoDB - if you see connection errors, ensure you're using the correct test profile.

2. **Missing Dependencies**: Run `mvn clean install` to ensure all dependencies are resolved.

3. **Test Failures**: Check that you have Java 17+ and Maven 3.9+ installed.

### Getting Help
If tests fail, check:
1. Java version: `java -version` (should be 17+)
2. Maven version: `mvn -version` (should be 3.9+)
3. Dependencies: `mvn dependency:tree`