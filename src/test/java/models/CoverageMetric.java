import java.util.HashMap;
import java.util.Map;

/**
 * CoverageMetric model for quantifiable measurements of test coverage
 * Based on the data model specification for unit test coverage
 */
public class CoverageMetric {
    private String className;
    private String methodName;
    private int linesCovered;
    private int totalLines;
    private int branchesCovered;
    private int totalBranches;
    private double coveragePercentage;
    private Map<String, Integer> methodCoverage;

    // Constructors
    public CoverageMetric() {
        this.methodCoverage = new HashMap<>();
    }

    public CoverageMetric(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
        this.methodCoverage = new HashMap<>();
        this.linesCovered = 0;
        this.totalLines = 0;
        this.branchesCovered = 0;
        this.totalBranches = 0;
        this.coveragePercentage = 0.0;
    }

    // Getters and setters
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getLinesCovered() {
        return linesCovered;
    }

    public void setLinesCovered(int linesCovered) {
        this.linesCovered = linesCovered;
        updateCoveragePercentage();
    }

    public int getTotalLines() {
        return totalLines;
    }

    public void setTotalLines(int totalLines) {
        this.totalLines = totalLines;
        updateCoveragePercentage();
    }

    public int getBranchesCovered() {
        return branchesCovered;
    }

    public void setBranchesCovered(int branchesCovered) {
        this.branchesCovered = branchesCovered;
    }

    public int getTotalBranches() {
        return totalBranches;
    }

    public void setTotalBranches(int totalBranches) {
        this.totalBranches = totalBranches;
    }

    public double getCoveragePercentage() {
        return coveragePercentage;
    }

    public Map<String, Integer> getMethodCoverage() {
        return methodCoverage;
    }

    // Business methods

    /**
     * Updates the coverage percentage based on lines covered and total lines
     */
    private void updateCoveragePercentage() {
        if (totalLines > 0) {
            this.coveragePercentage = (double) linesCovered / totalLines * 100.0;
        } else {
            this.coveragePercentage = 0.0;
        }
    }

    /**
     * Adds method coverage data
     * @param methodName Name of the method
     * @param linesCovered Number of lines covered in this method
     */
    public void addMethodCoverage(String methodName, int linesCovered) {
        this.methodCoverage.put(methodName, linesCovered);
    }

    /**
     * Calculates branch coverage percentage
     * @return Branch coverage percentage
     */
    public double getBranchCoveragePercentage() {
        if (totalBranches > 0) {
            return (double) branchesCovered / totalBranches * 100.0;
        }
        return 0.0;
    }

    /**
     * Checks if coverage meets the minimum threshold
     * @param threshold Minimum coverage percentage required
     * @return true if coverage meets or exceeds threshold
     */
    public boolean meetsThreshold(double threshold) {
        return coveragePercentage >= threshold;
    }

    /**
     * Checks if branch coverage meets the minimum threshold
     * @param threshold Minimum branch coverage percentage required
     * @return true if branch coverage meets or exceeds threshold
     */
    public boolean meetsBranchThreshold(double threshold) {
        return getBranchCoveragePercentage() >= threshold;
    }

    /**
     * Gets the number of uncovered lines
     * @return Number of lines not covered by tests
     */
    public int getUncoveredLines() {
        return totalLines - linesCovered;
    }

    /**
     * Gets the number of uncovered branches
     * @return Number of branches not covered by tests
     */
    public int getUncoveredBranches() {
        return totalBranches - branchesCovered;
    }

    /**
     * Creates a summary report of the coverage metrics
     * @return Formatted coverage report
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append(String.format("Coverage Report for %s",
                methodName != null ? className + "." + methodName : className));
        report.append("\n");
        report.append("=====================================\n");
        report.append(String.format("Line Coverage: %d/%d (%.2f%%)\n",
                linesCovered, totalLines, coveragePercentage));
        report.append(String.format("Branch Coverage: %d/%d (%.2f%%)\n",
                branchesCovered, totalBranches, getBranchCoveragePercentage()));

        if (!methodCoverage.isEmpty()) {
            report.append("\nMethod Coverage Details:\n");
            for (Map.Entry<String, Integer> entry : methodCoverage.entrySet()) {
                report.append(String.format("  %s: %d lines\n",
                        entry.getKey(), entry.getValue()));
            }
        }

        return report.toString();
    }

    /**
     * Merges coverage data from another CoverageMetric
     * @param other Another CoverageMetric to merge with
     */
    public void merge(CoverageMetric other) {
        if (other == null) return;

        this.linesCovered += other.linesCovered;
        this.totalLines += other.totalLines;
        this.branchesCovered += other.branchesCovered;
        this.totalBranches += other.totalBranches;

        // Merge method coverage maps
        for (Map.Entry<String, Integer> entry : other.methodCoverage.entrySet()) {
            this.methodCoverage.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        updateCoveragePercentage();
    }

    /**
     * Validates that the coverage metric has consistent data
     * @return true if the metric data is valid
     */
    public boolean isValid() {
        return className != null && !className.trim().isEmpty() &&
               linesCovered >= 0 && totalLines >= linesCovered &&
               branchesCovered >= 0 && totalBranches >= branchesCovered;
    }

    @Override
    public String toString() {
        return "CoverageMetric{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", coveragePercentage=" + String.format("%.2f", coveragePercentage) + "%" +
                ", linesCovered=" + linesCovered + "/" + totalLines +
                ", branchesCovered=" + branchesCovered + "/" + totalBranches +
                '}';
    }
}
