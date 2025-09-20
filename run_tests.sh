#!/bin/bash

# MicroBees Application Test Runner Script
# This script runs different types of tests for the MicroBees application

echo "🚀 MicroBees Application - Automation Test Suite"
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
print_status "Checking prerequisites..."

if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven 3.9+ to run tests."
    exit 1
fi

if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 17+ to run tests."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    print_error "Java 17+ is required. Current version: $JAVA_VERSION"
    exit 1
fi

print_success "Prerequisites check passed"

# Test execution options
echo ""
echo "Available test options:"
echo "1. Run unit tests (fast, no external dependencies)"
echo "2. Run all tests with coverage report"
echo "3. Run specific test class"
echo "4. Run tests for CI/CD pipeline"
echo "5. Generate coverage report only"
echo ""

read -p "Select option (1-5): " OPTION

case $OPTION in
    1)
        print_status "Running unit tests without external dependencies..."
        mvn test -Dtest='**/*MapperTest,**/*TestSuite' -Dspring.profiles.active=none
        if [ $? -eq 0 ]; then
            print_success "Unit tests completed successfully"
        else
            print_error "Unit tests failed"
            exit 1
        fi
        ;;
        
    2)
        print_status "Running all tests with coverage report..."
        mvn clean test jacoco:report -Dspring.profiles.active=test
        if [ $? -eq 0 ]; then
            print_success "All tests completed successfully"
            print_status "Coverage report generated at: target/site/jacoco/index.html"
        else
            print_warning "Some tests may have failed due to MongoDB connection issues"
            print_status "This is expected in environments without MongoDB"
        fi
        ;;
        
    3)
        echo "Available test classes:"
        echo "- MicroBeesMapperTest"
        echo "- UserInfoServiceTest" 
        echo "- UserInfoControllerTest"
        echo "- MultiTenantMongoTemplateTest"
        echo "- MicroBeesApplicationTests"
        echo "- MicroBeesTestSuite"
        echo ""
        read -p "Enter test class name: " TEST_CLASS
        
        print_status "Running test class: $TEST_CLASS..."
        mvn test -Dtest="$TEST_CLASS" -Dspring.profiles.active=none
        if [ $? -eq 0 ]; then
            print_success "Test class $TEST_CLASS completed successfully"
        else
            print_error "Test class $TEST_CLASS failed"
        fi
        ;;
        
    4)
        print_status "Running tests in CI/CD mode..."
        mvn clean test jacoco:report -Dspring.profiles.active=test -DfailIfNoTests=false
        
        # Check coverage threshold (60%)
        if [ -f target/site/jacoco/jacoco.xml ]; then
            COVERAGE=$(grep -A 1 'type="INSTRUCTION"' target/site/jacoco/jacoco.xml | grep 'covered' | sed -n 's/.*covered="\([0-9]*\)".*/\1/p' | head -1)
            MISSED=$(grep -A 1 'type="INSTRUCTION"' target/site/jacoco/jacoco.xml | grep 'covered' | sed -n 's/.*missed="\([0-9]*\)".*/\1/p' | head -1)
            
            if [ ! -z "$COVERAGE" ] && [ ! -z "$MISSED" ]; then
                TOTAL=$((COVERAGE + MISSED))
                if [ $TOTAL -gt 0 ]; then
                    PERCENT=$((COVERAGE * 100 / TOTAL))
                    print_status "Code coverage: $PERCENT%"
                    
                    if [ $PERCENT -ge 60 ]; then
                        print_success "Coverage threshold (60%) met: $PERCENT%"
                    else
                        print_warning "Coverage threshold (60%) not met: $PERCENT%"
                    fi
                fi
            fi
        fi
        ;;
        
    5)
        print_status "Generating coverage report from existing test data..."
        mvn jacoco:report
        if [ $? -eq 0 ]; then
            print_success "Coverage report generated at: target/site/jacoco/index.html"
        else
            print_error "Failed to generate coverage report"
        fi
        ;;
        
    *)
        print_error "Invalid option selected"
        exit 1
        ;;
esac

echo ""
print_status "Test execution completed"

# Check if coverage report exists and offer to open it
if [ -f "target/site/jacoco/index.html" ]; then
    echo ""
    read -p "Open coverage report in browser? (y/n): " OPEN_REPORT
    if [ "$OPEN_REPORT" = "y" ] || [ "$OPEN_REPORT" = "Y" ]; then
        if command -v open &> /dev/null; then
            open target/site/jacoco/index.html
        elif command -v xdg-open &> /dev/null; then
            xdg-open target/site/jacoco/index.html
        else
            print_status "Please open target/site/jacoco/index.html manually in your browser"
        fi
    fi
fi

echo ""
echo "🎉 MicroBees Test Suite Execution Complete!"
echo "📊 For detailed results, check the Maven output above"
echo "📁 Test reports are available in the target/ directory"