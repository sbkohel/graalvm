# Data Model: Unit Test Coverage

## Test Data Entities

### TestCase
- **Purpose**: Represents individual test scenarios for validation
- **Attributes**:
  - `testName`: Unique identifier for the test case
  - `inputData`: Script content or file path being tested
  - `expectedResult`: Expected outcome (success/failure/exception type)
  - `language`: Target polyglot language (js, python, R)
  - `testType`: Category (unit, integration, edge-case)

### TestFixture
- **Purpose**: Sample data used across multiple test cases
- **Attributes**:
  - `scriptContent`: Valid/invalid script text content
  - `filePath`: Mock or real file paths for testing
  - `encoding`: File encoding type for cross-platform testing
  - `fileSize`: Size constraints for performance testing

### CoverageMetric
- **Purpose**: Quantifiable measurements of test coverage
- **Attributes**:
  - `className`: Class being measured (Application)
  - `methodName`: Specific method coverage
  - `linesCovered`: Number of lines exercised
  - `branchsCovered`: Number of decision branches tested
  - `coveragePercentage`: Calculated coverage ratio

### TestResult
- **Purpose**: Output data from test execution
- **Attributes**:
  - `testStatus`: Pass/Fail/Error/Skip
  - `executionTime`: Time taken for test execution
  - `errorMessage`: Details when test fails
  - `actualOutput`: Captured output from script execution
  - `contextState`: GraalVM context status after test

## Test Data Relationships

- **TestCase** contains multiple **TestFixture** instances
- **TestResult** is generated from **TestCase** execution
- **CoverageMetric** aggregates across all **TestResult** instances
- **TestFixture** can be shared across multiple **TestCase** instances

## Mock Data Structure

### Valid Scripts
```
validJavaScript: "console.log('Hello from JS test');"
validPython: "print('Hello from Python test')"
validR: "print('Hello from R test')"
```

### Invalid Scripts
```
malformedJS: "console.log('unclosed string"
syntaxErrorPython: "print('test' invalid syntax"
runtimeErrorJS: "undefined.property.access()"
```

### File Path Scenarios
```
existingFile: "/scripts/helloWorld.js"
nonExistentFile: "/scripts/missing.js"  
invalidPath: "/invalid/path/script.js"
emptyFile: "/scripts/empty.js"
```
