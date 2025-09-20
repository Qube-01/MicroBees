#!/bin/bash

# MicroBees Test Automation Runner Script

echo "🚀 Starting MicroBees Test Automation Suite"
echo "============================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${2}${1}${NC}"
}

# Function to check if command was successful
check_status() {
    if [ $? -eq 0 ]; then
        print_status "✅ $1 completed successfully" $GREEN
    else
        print_status "❌ $1 failed" $RED
        exit 1
    fi
}

# Check prerequisites
print_status "🔍 Checking prerequisites..." $BLUE

# Check Java version
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    print_status "Java version: $JAVA_VERSION" $GREEN
else
    print_status "Java not found. Please install Java 17 or higher." $RED
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    print_status "Maven found: $MVN_VERSION" $GREEN
else
    print_status "Maven not found. Please install Maven." $RED
    exit 1
fi

# Check MongoDB (optional)
if command -v mongosh &> /dev/null || command -v mongo &> /dev/null; then
    print_status "MongoDB CLI found" $GREEN
else
    print_status "MongoDB CLI not found. Please ensure MongoDB is running on localhost:27017" $YELLOW
fi

# Parse command line arguments
TEST_TYPE="all"
SKIP_BUILD=false
HEADLESS=true
PROFILE="test"

while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--type)
            TEST_TYPE="$2"
            shift 2
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --headed)
            HEADLESS=false
            shift
            ;;
        -p|--profile)
            PROFILE="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 [options]"
            echo "Options:"
            echo "  -t, --type <type>    Test type: all, api, integration, automation, performance"
            echo "  --skip-build         Skip Maven build phase"
            echo "  --headed             Run Playwright tests in headed mode"
            echo "  -p, --profile        Spring profile (default: test)"
            echo "  -h, --help           Show this help message"
            exit 0
            ;;
        *)
            print_status "Unknown option: $1" $RED
            exit 1
            ;;
    esac
done

print_status "Test type: $TEST_TYPE" $BLUE
print_status "Profile: $PROFILE" $BLUE

# Build project if not skipped
if [ "$SKIP_BUILD" = false ]; then
    print_status "📦 Building project..." $BLUE
    mvn clean compile -DskipTests=true -q
    check_status "Project build"
fi

# Install Playwright dependencies if running automation tests
if [ "$TEST_TYPE" = "all" ] || [ "$TEST_TYPE" = "automation" ]; then
    print_status "🎭 Installing Playwright browsers..." $BLUE
    mvn exec:java -e -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install" -q
    check_status "Playwright browser installation"
fi

# Function to run specific test categories
run_tests() {
    local test_pattern=$1
    local test_name=$2
    
    print_status "🧪 Running $test_name..." $BLUE
    mvn test -Dtest="$test_pattern" -Dspring.profiles.active=$PROFILE -q
    check_status "$test_name"
}

# Run tests based on type
case $TEST_TYPE in
    "api")
        run_tests "**/*ApiTests" "API Tests"
        ;;
    "integration") 
        run_tests "**/*IntegrationTests" "Integration Tests"
        ;;
    "automation")
        run_tests "**/*PlaywrightTests" "Playwright Automation Tests"
        ;;
    "performance")
        run_tests "**/*PerformanceTests" "Performance Tests"
        ;;
    "all")
        print_status "🧪 Running complete test suite..." $BLUE
        mvn test -Dspring.profiles.active=$PROFILE
        check_status "Complete test suite"
        ;;
    *)
        print_status "Invalid test type: $TEST_TYPE" $RED
        print_status "Valid types: all, api, integration, automation, performance" $YELLOW
        exit 1
        ;;
esac

# Generate reports
print_status "📊 Generating test reports..." $BLUE

# Check if Surefire reports exist
if [ -d "target/surefire-reports" ]; then
    TESTS_RUN=$(find target/surefire-reports -name "TEST-*.xml" -exec grep -l "tests=" {} \; | wc -l)
    if [ $TESTS_RUN -gt 0 ]; then
        print_status "Test reports generated in target/surefire-reports/" $GREEN
        
        # Count test results
        TOTAL_TESTS=$(find target/surefire-reports -name "TEST-*.xml" -exec grep -h "tests=" {} \; | sed 's/.*tests="\([0-9]*\)".*/\1/' | awk '{sum += $1} END {print sum}')
        FAILED_TESTS=$(find target/surefire-reports -name "TEST-*.xml" -exec grep -h "failures=" {} \; | sed 's/.*failures="\([0-9]*\)".*/\1/' | awk '{sum += $1} END {print sum}')
        SKIPPED_TESTS=$(find target/surefire-reports -name "TEST-*.xml" -exec grep -h "skipped=" {} \; | sed 's/.*skipped="\([0-9]*\)".*/\1/' | awk '{sum += $1} END {print sum}')
        
        # Default values if empty
        TOTAL_TESTS=${TOTAL_TESTS:-0}
        FAILED_TESTS=${FAILED_TESTS:-0}
        SKIPPED_TESTS=${SKIPPED_TESTS:-0}
        PASSED_TESTS=$((TOTAL_TESTS - FAILED_TESTS - SKIPPED_TESTS))
        
        print_status "📈 Test Results Summary:" $BLUE
        print_status "   Total Tests: $TOTAL_TESTS" $NC
        print_status "   Passed: $PASSED_TESTS" $GREEN
        print_status "   Failed: $FAILED_TESTS" $RED
        print_status "   Skipped: $SKIPPED_TESTS" $YELLOW
        
        if [ $FAILED_TESTS -eq 0 ]; then
            print_status "🎉 All tests passed successfully!" $GREEN
        else
            print_status "⚠️  Some tests failed. Check reports for details." $YELLOW
        fi
    fi
fi

# Check for Jacoco coverage reports
if [ -d "target/site/jacoco" ]; then
    print_status "📊 Code coverage report generated in target/site/jacoco/" $GREEN
fi

# Playwright reports
if [ -d "playwright-report" ]; then
    print_status "🎭 Playwright report generated in playwright-report/" $GREEN
fi

print_status "✨ Test execution completed!" $GREEN
print_status "============================================" $NC

# Open reports if in interactive mode
if [ -t 1 ]; then
    read -p "Would you like to open the HTML test reports? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if [ -f "target/surefire-reports/index.html" ]; then
            open "target/surefire-reports/index.html" 2>/dev/null || echo "Please manually open target/surefire-reports/index.html"
        fi
        if [ -f "playwright-report/index.html" ]; then
            open "playwright-report/index.html" 2>/dev/null || echo "Please manually open playwright-report/index.html"
        fi
        if [ -f "target/site/jacoco/index.html" ]; then
            open "target/site/jacoco/index.html" 2>/dev/null || echo "Please manually open target/site/jacoco/index.html"
        fi
    fi
fi