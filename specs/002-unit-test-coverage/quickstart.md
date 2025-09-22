# Quick Start: Unit Test Coverage

## Prerequisites
- Java 21 with GraalVM
- Gradle build tool
- Existing polyglot application in working state

## Running Tests

### Execute All Tests
```bash
./gradlew test
```

### Run with Coverage Report
```bash
./gradlew test jacocoTestReport
```

### View Coverage Results
- **Console Output**: Coverage summary displayed after test execution
- **HTML Report**: Open `build/reports/jacoco/test/html/index.html`
- **XML Report**: `build/reports/jacoco/test/jacocoTestReport.xml` for CI integration

## Test Categories

### Unit Tests
Test individual methods in isolation:
```bash
./gradlew test --tests "ApplicationTest"
```

### Integration Tests
Test GraalVM polyglot interactions:
```bash
./gradlew test --tests "*Integration*"
```

### Edge Case Tests
Test error conditions and boundary scenarios:
```bash
./gradlew test --tests "*EdgeCase*"
```

## Validation Checklist

### Coverage Goals
- [ ] Line coverage ≥ 90%
- [ ] Branch coverage ≥ 80%
- [ ] All public methods tested
- [ ] All exception scenarios covered

### Test Quality
- [ ] Tests run independently (no order dependency)
- [ ] All tests pass consistently
- [ ] Test execution time < 30 seconds total
- [ ] No external file dependencies in tests

### Functionality Coverage
- [ ] `readFile()` method: success and error cases
- [ ] `runScript()` method: all supported languages
- [ ] `main()` method: full integration flow
- [ ] Edge cases: missing files, malformed scripts, context failures

## Troubleshooting

### Common Issues
1. **GraalVM Context Initialization Fails**
   - Verify GraalVM installation
   - Check supported language modules installed

2. **File Path Issues**
   - Ensure test scripts exist in `/scripts` directory
   - Check file permissions and accessibility

3. **Low Coverage Warnings**
   - Review untested branches in coverage report
   - Add tests for missing scenarios

### Debug Mode
Run tests with detailed output:
```bash
./gradlew test --info --stacktrace
```

## Expected Output
After successful test execution:
- All tests pass (green output)
- Coverage metrics displayed
- No compilation or runtime errors
- Generated reports in `build/reports/` directory
