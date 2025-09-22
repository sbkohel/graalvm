import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for complete polyglot workflow testing end-to-end functionality
 * Tests the full pipeline from file reading through script execution across multiple languages
 */
class PolyglotIntegrationTest {

    private Context context;
    private Method readFileMethod;
    private Method runScriptMethod;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        context = TestFixtures.createTestContext();

        // Get reflection access to Application methods
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
    @DisplayName("Should execute complete JavaScript workflow from file to result")
    void testCompleteJavaScriptWorkflow() throws Exception {
        // Arrange
        String jsScript = "function calculate(a, b) { return a * b + 10; } calculate(5, 6);";
        Path jsFile = tempDir.resolve("workflow.js");
        Files.writeString(jsFile, jsScript);

        // Act - Complete workflow: read file -> execute script -> get result
        String fileContent = (String) readFileMethod.invoke(null, jsFile.toString());
        Value result = (Value) runScriptMethod.invoke(null, fileContent, context, "js");

        // Assert
        assertNotNull(fileContent);
        assertTrue(fileContent.contains("calculate"));
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(40, result.asInt()); // 5 * 6 + 10 = 40
    }

    @Test
    @DisplayName("Should execute complete Python workflow from file to result")
    void testCompletePythonWorkflow() throws Exception {
        // Arrange
        String pythonScript = """
            def process_data(numbers):
                return sum(x * 2 for x in numbers)
            
            data = [1, 2, 3, 4, 5]
            process_data(data)
            """;
        Path pyFile = tempDir.resolve("workflow.py");
        Files.writeString(pyFile, pythonScript);

        // Act - Complete workflow
        String fileContent = (String) readFileMethod.invoke(null, pyFile.toString());
        Value result = (Value) runScriptMethod.invoke(null, fileContent, context, "python");

        // Assert
        assertNotNull(fileContent);
        assertTrue(fileContent.contains("process_data"));
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(30, result.asInt()); // (1+2+3+4+5) * 2 = 30
    }

    @Test
    @DisplayName("Should handle mixed language execution in sequence")
    void testMixedLanguageSequentialExecution() throws Exception {
        // Arrange - Multiple script files
        String jsScript1 = "var step1 = 100; step1;";
        String pyScript = "step2 = 200\nstep2";
        String jsScript2 = "var step3 = 300; step3;";

        Path jsFile1 = tempDir.resolve("step1.js");
        Path pyFile = tempDir.resolve("step2.py");
        Path jsFile2 = tempDir.resolve("step3.js");

        Files.writeString(jsFile1, jsScript1);
        Files.writeString(pyFile, pyScript);
        Files.writeString(jsFile2, jsScript2);

        // Act - Execute in sequence
        String js1Content = (String) readFileMethod.invoke(null, jsFile1.toString());
        Value result1 = (Value) runScriptMethod.invoke(null, js1Content, context, "js");

        String pyContent = (String) readFileMethod.invoke(null, pyFile.toString());
        Value result2 = (Value) runScriptMethod.invoke(null, pyContent, context, "python");

        String js2Content = (String) readFileMethod.invoke(null, jsFile2.toString());
        Value result3 = (Value) runScriptMethod.invoke(null, js2Content, context, "js");

        // Assert - All executions should work correctly
        assertEquals(100, result1.asInt());
        assertEquals(200, result2.asInt());
        assertEquals(300, result3.asInt());
    }

    @Test
    @DisplayName("Should handle complex data processing workflow")
    void testComplexDataProcessingWorkflow() throws Exception {
        // Arrange - Complex JavaScript with data manipulation
        String complexJS = """
            // Complex data processing simulation
            const data = [
                {name: 'Alice', score: 85},
                {name: 'Bob', score: 92},
                {name: 'Charlie', score: 78}
            ];
            
            const processedData = data
                .filter(student => student.score > 80)
                .map(student => ({...student, grade: student.score > 90 ? 'A' : 'B'}))
                .reduce((acc, student) => acc + student.score, 0);
            
            processedData; // Should return sum of scores > 80
            """;

        Path complexFile = tempDir.resolve("complex.js");
        Files.writeString(complexFile, complexJS);

        // Act
        String fileContent = (String) readFileMethod.invoke(null, complexFile.toString());
        Value result = (Value) runScriptMethod.invoke(null, fileContent, context, "js");

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(177, result.asInt()); // 85 + 92 = 177 (Charlie filtered out)
    }

    @Test
    @DisplayName("Should handle workflow with error recovery")
    void testWorkflowWithErrorRecovery() throws Exception {
        // Arrange - Valid backup script in case primary fails
        String primaryScript = "this.will.cause.error";
        String backupScript = "var backup = 'success'; backup;";

        Path primaryFile = tempDir.resolve("primary.js");
        Path backupFile = tempDir.resolve("backup.js");
        Files.writeString(primaryFile, primaryScript);
        Files.writeString(backupFile, backupScript);

        // Act - Try primary, fall back to backup on error
        String primaryContent = (String) readFileMethod.invoke(null, primaryFile.toString());
        Value result = null;

        try {
            result = (Value) runScriptMethod.invoke(null, primaryContent, context, "js");
            fail("Should have thrown an exception");
        } catch (Exception e) {
            // Expected - now try backup
            String backupContent = (String) readFileMethod.invoke(null, backupFile.toString());
            result = (Value) runScriptMethod.invoke(null, backupContent, context, "js");
        }

        // Assert
        assertNotNull(result);
        assertTrue(result.isString());
        assertEquals("success", result.asString());
    }

    @Test
    @DisplayName("Should handle large-scale workflow processing")
    void testLargeScaleWorkflow() throws Exception {
        // Arrange - Script that processes substantial data
        String largeProcessingScript = """
            // Simulate large data processing
            let total = 0;
            for (let i = 1; i <= 1000; i++) {
                total += Math.sqrt(i);
            }
            Math.round(total);
            """;

        Path largeFile = tempDir.resolve("large-processing.js");
        Files.writeString(largeFile, largeProcessingScript);

        // Act - Measure performance
        long startTime = System.currentTimeMillis();
        String fileContent = (String) readFileMethod.invoke(null, largeFile.toString());
        Value result = (Value) runScriptMethod.invoke(null, fileContent, context, "js");
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertTrue(result.asInt() > 20000); // Sum of sqrt(1..1000) ≈ 21097
        assertTrue(duration < 5000, "Large-scale processing should complete within 5 seconds");
    }

    @Test
    @DisplayName("Should maintain context isolation between workflows")
    void testContextIsolationBetweenWorkflows() throws Exception {
        // Arrange - Scripts that should not interfere with each other
        String workflow1Script = "var isolatedVar = 'workflow1'; isolatedVar;";
        String workflow2Script = "var isolatedVar = 'workflow2'; isolatedVar;";

        Path file1 = tempDir.resolve("workflow1.js");
        Path file2 = tempDir.resolve("workflow2.js");
        Files.writeString(file1, workflow1Script);
        Files.writeString(file2, workflow2Script);

        // Act - Execute both workflows
        String content1 = (String) readFileMethod.invoke(null, file1.toString());
        Value result1 = (Value) runScriptMethod.invoke(null, content1, context, "js");

        String content2 = (String) readFileMethod.invoke(null, file2.toString());
        Value result2 = (Value) runScriptMethod.invoke(null, content2, context, "js");

        // Assert - Each workflow should maintain its own state
        assertEquals("workflow1", result1.asString());
        assertEquals("workflow2", result2.asString());
    }
}
