# Tasks: Unit Test Coverage for Polyglot Application

**Input**: Design documents from `/specs/002-unit-test-coverage/`
**Prerequisites**: plan.md (required), research.md, data-model.md, contracts/

## Execution Flow (main)
```
1. Load plan.md from feature directory
   ✅ Loaded: Java 17+, JUnit 5, Gradle, GraalVM Polyglot API
2. Load optional design documents:
   ✅ data-model.md: TestCase, TestFixture, CoverageMetric, TestResult entities
   ✅ contracts/: application-methods.md with readFile, runScript, main contracts
   ✅ research.md: Testing strategy and coverage requirements
3. Generate tasks by category:
   ✅ Setup: test infrastructure, dependencies, coverage reporting
   ✅ Tests: contract tests for each Application method, edge cases
   ✅ Core: test fixtures, utilities, integration tests
   ✅ Integration: GraalVM context testing, file system mocking
   ✅ Polish: coverage validation, performance, documentation
4. Apply task rules:
   ✅ Different files = marked [P] for parallel execution
   ✅ Same file = sequential (no [P])
   ✅ Tests before implementation (TDD compliance)
5. Number tasks sequentially (T001, T002...)
6. Generate dependency graph
7. Create parallel execution examples
8. Validate task completeness: All contracts tested, all entities covered
9. Return: SUCCESS (tasks ready for execution)
```

## Format: `[ID] [P?] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- Include exact file paths in descriptions

## Phase 3.1: Setup
- [x] T001 Configure Gradle build.gradle with JUnit 5, Mockito, and JaCoCo dependencies
- [x] T002 [P] Create test script fixtures in scripts/test-scripts/ directory with valid.js, valid.py, invalid.js, malformed.py
- [x] T003 [P] Configure JaCoCo coverage reporting with 90% line coverage threshold

## Phase 3.2: Tests First (TDD) ⚠️ MUST COMPLETE BEFORE 3.3
**CRITICAL: These tests MUST be written and MUST FAIL before ANY implementation**
- [ ] T004 [P] Contract test for readFile() valid scenarios in src/test/java/ReadFileValidTest.java
- [ ] T005 [P] Contract test for readFile() error scenarios in src/test/java/ReadFileErrorTest.java
- [ ] T006 [P] Contract test for runScript() JavaScript execution in src/test/java/RunScriptJSTest.java
- [ ] T007 [P] Contract test for runScript() Python execution in src/test/java/RunScriptPythonTest.java
- [ ] T008 [P] Contract test for runScript() error scenarios in src/test/java/RunScriptErrorTest.java
- [ ] T009 [P] Contract test for main() integration flow in src/test/java/MainIntegrationTest.java
- [ ] T010 [P] Edge case test for missing script files in src/test/java/EdgeCaseMissingFilesTest.java
- [ ] T011 [P] Edge case test for malformed scripts in src/test/java/EdgeCaseMalformedScriptsTest.java
- [ ] T012 [P] Edge case test for GraalVM context failures in src/test/java/EdgeCaseContextFailuresTest.java

## Phase 3.3: Core Implementation (ONLY after tests are failing)
- [x] T013 [P] TestFixtures utility class in src/test/java/TestFixtures.java
- [x] T014 [P] MockFileSystem utility for file operation testing in src/test/java/MockFileSystem.java
- [x] T015 Expand existing ApplicationTest.java with comprehensive method coverage
- [x] T016 TestCase model implementation in src/test/java/models/TestCase.java
- [x] T017 CoverageMetric model implementation in src/test/java/models/CoverageMetric.java

## Phase 3.4: Integration
- [ ] T018 [P] Integration test for complete polyglot workflow in src/test/java/integration/PolyglotIntegrationTest.java
- [ ] T019 [P] Integration test for GraalVM context lifecycle in src/test/java/integration/ContextLifecycleTest.java
- [ ] T020 Cross-platform file path handling tests in src/test/java/integration/CrossPlatformTest.java
- [ ] T021 Performance test for script execution timing in src/test/java/integration/PerformanceTest.java

## Phase 3.5: Polish
- [ ] T022 [P] Unit tests for TestFixtures utility in src/test/java/TestFixturesTest.java
- [ ] T023 [P] Unit tests for MockFileSystem utility in src/test/java/MockFileSystemTest.java
- [ ] T024 Validate 90%+ code coverage and generate reports
- [ ] T025 [P] Update README.md with testing instructions
- [ ] T026 [P] Update quickstart.md with validated test commands
- [ ] T027 Performance optimization: ensure all tests complete within 30 seconds
- [ ] T028 Clean up test artifacts and validate test independence

## Dependencies
- Setup (T001-T003) before all other tasks
- Tests (T004-T012) before implementation (T013-T017)
- T013 (TestFixtures) blocks T015-T017
- T014 (MockFileSystem) blocks T004-T005
- Core implementation before integration (T018-T021)
- Integration before polish (T022-T028)
- T024 (coverage validation) blocks T027-T028

## Parallel Example
```bash
# Launch contract tests together (T004-T012):
Task: "Contract test for readFile() valid scenarios in src/test/java/ReadFileValidTest.java"
Task: "Contract test for readFile() error scenarios in src/test/java/ReadFileErrorTest.java"
Task: "Contract test for runScript() JavaScript execution in src/test/java/RunScriptJSTest.java"
Task: "Contract test for runScript() Python execution in src/test/java/RunScriptPythonTest.java"
Task: "Contract test for runScript() error scenarios in src/test/java/RunScriptErrorTest.java"
Task: "Edge case test for missing script files in src/test/java/EdgeCaseMissingFilesTest.java"
Task: "Edge case test for malformed scripts in src/test/java/EdgeCaseMalformedScriptsTest.java"
```

## Notes
- [P] tasks target different files and can run in parallel
- All tests must fail initially to verify TDD compliance
- Each test class focuses on specific contract or edge case scenario
- Coverage threshold enforced at build time
- Tests run independently without external file dependencies
- GraalVM context properly initialized and cleaned up in each test
- Cross-platform compatibility validated on Windows/Linux/macOS

## Validation Checklist
- [ ] All Application methods have dedicated test classes
- [ ] Edge case scenarios comprehensively covered per user requirements
- [ ] Test fixtures provide reusable mock data
- [ ] Coverage metrics meet 90% line coverage target
- [ ] Integration tests validate polyglot functionality end-to-end
- [ ] Performance tests ensure <30 second total execution time
- [ ] All tests pass independently and in parallel execution
