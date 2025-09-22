/**
 * TestCase model representing individual test scenarios for validation
 * Based on the data model specification for unit test coverage
 */
public class TestCase {
    private String testName;
    private String inputData;
    private String expectedResult;
    private String language;
    private String testType;
    private boolean passed;
    private String errorMessage;
    private long executionTime;

    // Constructors
    public TestCase() {}

    public TestCase(String testName, String inputData, String expectedResult,
                    String language, String testType) {
        this.testName = testName;
        this.inputData = inputData;
        this.expectedResult = expectedResult;
        this.language = language;
        this.testType = testType;
        this.passed = false;
        this.executionTime = 0L;
    }

    // Getters and setters
    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    // Business methods

    /**
     * Executes the test case and updates the result
     * @param actualResult The actual result from test execution
     * @param executionTimeMs Time taken for execution in milliseconds
     */
    public void updateResult(String actualResult, long executionTimeMs) {
        this.executionTime = executionTimeMs;
        this.passed = expectedResult.equals(actualResult);
        if (!passed) {
            this.errorMessage = "Expected: " + expectedResult + ", but got: " + actualResult;
        } else {
            this.errorMessage = null;
        }
    }

    /**
     * Marks the test case as failed with an error message
     * @param error The error that caused the failure
     * @param executionTimeMs Time taken before failure
     */
    public void markAsFailed(String error, long executionTimeMs) {
        this.passed = false;
        this.errorMessage = error;
        this.executionTime = executionTimeMs;
    }

    /**
     * Creates a summary string of the test case
     * @return Formatted test case summary
     */
    public String getSummary() {
        return String.format("TestCase[%s]: %s (%s) - %s in %dms",
                testName,
                testType,
                language,
                passed ? "PASSED" : "FAILED",
                executionTime);
    }

    /**
     * Validates that the test case has all required fields
     * @return true if test case is valid
     */
    public boolean isValid() {
        return testName != null && !testName.trim().isEmpty() &&
               inputData != null &&
               expectedResult != null &&
               language != null && !language.trim().isEmpty() &&
               testType != null && !testType.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "testName='" + testName + '\'' +
                ", language='" + language + '\'' +
                ", testType='" + testType + '\'' +
                ", passed=" + passed +
                ", executionTime=" + executionTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCase testCase = (TestCase) o;
        return testName != null ? testCase.testName.equals(testCase.testName) : testCase.testName == null;
    }

    @Override
    public int hashCode() {
        return testName != null ? testName.hashCode() : 0;
    }
}

