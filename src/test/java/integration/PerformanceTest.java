import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance test for script execution timing
 * Tests execution speed, throughput, and performance characteristics of the polyglot application
 */
class PerformanceTest {

    private Context context;
    private Method readFileMethod;
    private Method runScriptMethod;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        context = TestFixtures.createTestContext();

        readFileMethod = Application.class.getDeclaredMethod("readFile", String.class);
        readFileMethod.setAccessible(true);

        runScriptMethod = Application.class.getDeclaredMethod("runScript", String.class, Context.class, String.class);
        runScriptMethod.setAccessible(true);
    }

    @AfterEach
    void tearDown() {
        TestFixtures.cleanupContext(context);
    }

    @Test
    @DisplayName("Simple script execution should complete within performance thresholds")
    void testSimpleScriptPerformance() throws Exception {
        // Arrange
        String simpleScript = "5 + 3";

        // Act & Measure
        long startTime = System.nanoTime();
        Value result = (Value) runScriptMethod.invoke(null, simpleScript, context, "js");
        long duration = System.nanoTime() - startTime;

        // Assert
        assertEquals(8, result.asInt());
        assertTrue(duration < TimeUnit.MILLISECONDS.toNanos(100),
                   "Simple script should execute in less than 100ms, took: " + duration / 1_000_000 + "ms");
    }

    @Test
    @DisplayName("File reading performance should meet requirements")
    void testFileReadingPerformance() throws Exception {
        // Arrange - Create files of various sizes
        String smallContent = TestFixtures.generateLargeFileContent(10);
        String mediumContent = TestFixtures.generateLargeFileContent(100);
        String largeContent = TestFixtures.generateLargeFileContent(1000);

        Path smallFile = tempDir.resolve("small.txt");
        Path mediumFile = tempDir.resolve("medium.txt");
        Path largeFile = tempDir.resolve("large.txt");

        Files.writeString(smallFile, smallContent);
        Files.writeString(mediumFile, mediumContent);
        Files.writeString(largeFile, largeContent);

        // Act & Measure small file
        long startTime = System.nanoTime();
        String content = (String) readFileMethod.invoke(null, smallFile.toString());
        long smallFileDuration = System.nanoTime() - startTime;

        // Act & Measure medium file
        startTime = System.nanoTime();
        content = (String) readFileMethod.invoke(null, mediumFile.toString());
        long mediumFileDuration = System.nanoTime() - startTime;

        // Act & Measure large file
        startTime = System.nanoTime();
        content = (String) readFileMethod.invoke(null, largeFile.toString());
        long largeFileDuration = System.nanoTime() - startTime;

        // Assert
        assertTrue(smallFileDuration < TimeUnit.MILLISECONDS.toNanos(50),
                   "Small file reading took too long: " + smallFileDuration / 1_000_000 + "ms");
        assertTrue(mediumFileDuration < TimeUnit.MILLISECONDS.toNanos(100),
                   "Medium file reading took too long: " + mediumFileDuration / 1_000_000 + "ms");
        assertTrue(largeFileDuration < TimeUnit.MILLISECONDS.toNanos(1000),
                   "Large file reading took too long: " + largeFileDuration / 1_000_000 + "ms");
    }

    @Test
    @DisplayName("Multiple script executions should maintain consistent performance")
    void testConsistentExecutionPerformance() throws Exception {
        // Arrange
        String testScript = "Math.sqrt(16) * Math.pow(2, 3)";
        List<Long> executionTimes = new ArrayList<>();

        // Act - Execute multiple times and measure
        for (int i = 0; i < 50; i++) {
            long startTime = System.nanoTime();
            Value result = (Value) runScriptMethod.invoke(null, testScript, context, "js");
            long duration = System.nanoTime() - startTime;

            executionTimes.add(duration);
            assertEquals(32.0, result.asDouble(), 0.001); // 4 * 8 = 32
        }

        // Calculate statistics
        double averageTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long maxTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
        long minTime = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0L);

        // Assert
        assertTrue(averageTime < TimeUnit.MILLISECONDS.toNanos(10),
                   "Average execution time should be under 10ms: " + averageTime / 1_000_000 + "ms");
        assertTrue(maxTime < TimeUnit.MILLISECONDS.toNanos(100),
                   "Maximum execution time should be under 100ms: " + maxTime / 1_000_000 + "ms");

        // Variance should be reasonable (max should not be more than 10x min)
        assertTrue(maxTime / (double) minTime < 10.0,
                   "Performance should be consistent (max/min ratio: " + maxTime / (double) minTime + ")");
    }

    @Test
    @DisplayName("Concurrent script execution should scale appropriately")
    void testConcurrentExecutionPerformance() throws Exception {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(10);
        String concurrentScript = "var result = 0; for(var i = 0; i < 1000; i++) { result += i; } result;";

        try {
            // Act - Submit concurrent tasks
            List<Future<Long>> futures = new ArrayList<>();
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < 20; i++) {
                futures.add(executor.submit(() -> {
                    try {
                        long taskStart = System.nanoTime();
                        Value result = (Value) runScriptMethod.invoke(null, concurrentScript, context, "js");
                        long taskDuration = System.nanoTime() - taskStart;
                        assertEquals(499500, result.asInt()); // Sum of 0 to 999
                        return taskDuration;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            // Wait for all tasks to complete
            List<Long> taskDurations = new ArrayList<>();
            for (Future<Long> future : futures) {
                taskDurations.add(future.get(10, TimeUnit.SECONDS));
            }

            long totalTime = System.currentTimeMillis() - startTime;

            // Assert
            assertTrue(totalTime < 15000, "All concurrent tasks should complete within 15 seconds");

            double averageTaskTime = taskDurations.stream().mapToLong(Long::longValue).average().orElse(0.0);
            assertTrue(averageTaskTime < TimeUnit.MILLISECONDS.toNanos(1000),
                       "Average concurrent task time should be under 1000ms: " + averageTaskTime / 1_000_000 + "ms");

        } finally {
            executor.shutdown();
            executor.awaitTermination(20, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Large data processing should complete within time limits")
    void testLargeDataProcessingPerformance() throws Exception {
        // Arrange - Large data processing script
        String largeDataScript = """
            var data = [];
            for (var i = 0; i < 10000; i++) {
                data.push({id: i, value: Math.random() * 100});
            }
            
            var processed = data
                .filter(item => item.value > 50)
                .map(item => item.value * 2)
                .reduce((sum, value) => sum + value, 0);
            
            Math.round(processed);
            """;

        // Act & Measure
        long startTime = System.currentTimeMillis();
        Value result = (Value) runScriptMethod.invoke(null, largeDataScript, context, "js");
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertTrue(result.asInt() > 0, "Large data processing should return a positive result");
        assertTrue(duration < 10000, "Large data processing should complete within 10 seconds, took: " + duration + "ms");
    }

    @Test
    @DisplayName("Memory usage should remain stable during extended execution")
    void testMemoryStabilityDuringExecution() throws Exception {
        // Arrange
        String memoryTestScript = "var temp = []; for(var i = 0; i < 1000; i++) { temp.push(i); } temp.length;";

        // Act - Execute many operations to test memory stability
        for (int iteration = 0; iteration < 100; iteration++) {
            long startTime = System.nanoTime();
            Value result = (Value) runScriptMethod.invoke(null, memoryTestScript, context, "js");
            long duration = System.nanoTime() - startTime;

            // Assert each iteration
            assertEquals(1000, result.asInt());
            assertTrue(duration < TimeUnit.MILLISECONDS.toNanos(100),
                       "Iteration " + iteration + " took too long: " + duration / 1_000_000 + "ms");

            // Periodic memory check (every 20 iterations)
            if (iteration % 20 == 0) {
                Runtime runtime = Runtime.getRuntime();
                long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                assertTrue(usedMemory < 500_000_000, // 500MB limit
                           "Memory usage too high at iteration " + iteration + ": " + usedMemory / 1_000_000 + "MB");
            }
        }
    }

    @Test
    @DisplayName("Different language performance should be comparable")
    void testLanguagePerformanceComparison() throws Exception {
        // Arrange - Equivalent computations in different languages
        String jsScript = "var sum = 0; for(var i = 1; i <= 1000; i++) { sum += i; } sum;";
        String pyScript = "sum = 0\nfor i in range(1, 1001):\n    sum += i\nsum";

        // Act & Measure JavaScript
        long jsStartTime = System.nanoTime();
        Value jsResult = (Value) runScriptMethod.invoke(null, jsScript, context, "js");
        long jsDuration = System.nanoTime() - jsStartTime;

        // Act & Measure Python
        long pyStartTime = System.nanoTime();
        Value pyResult = (Value) runScriptMethod.invoke(null, pyScript, context, "python");
        long pyDuration = System.nanoTime() - pyStartTime;

        // Assert
        assertEquals(500500, jsResult.asInt()); // Sum of 1 to 1000
        assertEquals(500500, pyResult.asInt()); // Sum of 1 to 1000

        assertTrue(jsDuration < TimeUnit.MILLISECONDS.toNanos(1000),
                   "JavaScript execution took too long: " + jsDuration / 1_000_000 + "ms");
        assertTrue(pyDuration < TimeUnit.MILLISECONDS.toNanos(1000),
                   "Python execution took too long: " + pyDuration / 1_000_000 + "ms");

        // Performance should be within reasonable ratio
        double performanceRatio = Math.max(jsDuration, pyDuration) / (double) Math.min(jsDuration, pyDuration);
        assertTrue(performanceRatio < 50.0,
                   "Language performance difference too large: " + performanceRatio);
    }

    @Test
    @DisplayName("Overall test suite should complete within target time")
    void testOverallPerformanceTarget() throws Exception {
        // Arrange - Simulate running multiple test scenarios
        String[] testScripts = {
            "1 + 1",
            "Math.sqrt(16)",
            "var arr = [1,2,3]; arr.reduce((a,b) => a+b, 0)",
            "function test() { return 'performance'; } test()",
            "'hello'.toUpperCase()"
        };

        // Act - Execute all test scripts multiple times
        long overallStartTime = System.currentTimeMillis();

        for (int round = 0; round < 20; round++) {
            for (String script : testScripts) {
                Value result = (Value) runScriptMethod.invoke(null, script, context, "js");
                assertNotNull(result);
            }
        }

        long overallDuration = System.currentTimeMillis() - overallStartTime;

        // Assert - Target: all tests should complete well within 30 seconds
        assertTrue(overallDuration < TestFixtures.MAX_TEST_DURATION_SECONDS * 1000,
                   "Overall test execution exceeded target time: " + overallDuration + "ms");
    }
}
