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
 * Comprehensive test coverage for Application class methods
 * This class provides end-to-end testing of the polyglot application functionality
 */
class ApplicationTest {

    private Context context;
    private MockFileSystem mockFileSystem;
    private Method readFileMethod;
    private Method runScriptMethod;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // Initialize GraalVM context
        context = TestFixtures.createTestContext();

        // Initialize mock file system
        mockFileSystem = new MockFileSystem();
        mockFileSystem.setupCommonTestFiles();

        // Get reflection access to private methods
        readFileMethod = Application.class.getDeclaredMethod("readFile", String.class);
        readFileMethod.setAccessible(true);

        runScriptMethod = Application.class.getDeclaredMethod("runScript", String.class, Context.class, String.class);
        runScriptMethod.setAccessible(true);
    }

    @AfterEach
    void tearDown() {
        TestFixtures.cleanupContext(context);
        mockFileSystem.clear();
    }

    // Comprehensive readFile() method tests

    @Test
    @DisplayName("readFile should handle valid JavaScript files")
    void testReadFile_validJavaScript() throws Exception {
        // Arrange
        Path jsFile = tempDir.resolve("test.js");
        Files.writeString(jsFile, TestFixtures.VALID_JAVASCRIPT);

        // Act
        String content = (String) readFileMethod.invoke(null, jsFile.toString());

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("console.log"));
    }

    @Test
    @DisplayName("readFile should handle valid Python files")
    void testReadFile_validPython() throws Exception {
        // Arrange
        Path pyFile = tempDir.resolve("test.py");
        Files.writeString(pyFile, TestFixtures.VALID_PYTHON);

        // Act
        String content = (String) readFileMethod.invoke(null, pyFile.toString());

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("print"));
    }

    @Test
    @DisplayName("readFile should handle files with special characters")
    void testReadFile_specialCharacters() throws Exception {
        // Arrange
        Path specialFile = tempDir.resolve("special.txt");
        Files.writeString(specialFile, TestFixtures.SPECIAL_CHARS_CONTENT);

        // Act
        String content = (String) readFileMethod.invoke(null, specialFile.toString());

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("Special chars:"));
        assertTrue(content.contains("🚀"));
    }

    @Test
    @DisplayName("readFile should handle large files efficiently")
    void testReadFile_largeFile() throws Exception {
        // Arrange
        String largeContent = TestFixtures.generateLargeFileContent(TestFixtures.LARGE_FILE_LINE_COUNT);
        Path largeFile = tempDir.resolve("large.txt");
        Files.writeString(largeFile, largeContent);

        // Act
        long startTime = System.currentTimeMillis();
        String content = (String) readFileMethod.invoke(null, largeFile.toString());
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("Line 0"));
        assertTrue(content.contains("Line 999"));
        assertTrue(duration < 5000, "Large file reading should complete within 5 seconds");
    }

    // Comprehensive runScript() method tests

    @Test
    @DisplayName("runScript should execute JavaScript successfully")
    void testRunScript_javascript() throws Exception {
        // Act
        Value result = (Value) runScriptMethod.invoke(null, "5 + 3", context, "js");

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(8, result.asInt());
    }

    @Test
    @DisplayName("runScript should execute Python successfully")
    void testRunScript_python() throws Exception {
        // Act
        Value result = (Value) runScriptMethod.invoke(null, "7 * 6", context, "python");

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(42, result.asInt());
    }

    @Test
    @DisplayName("runScript should handle JavaScript functions")
    void testRunScript_javascriptFunction() throws Exception {
        // Arrange
        String jsFunction = "function multiply(a, b) { return a * b; } multiply(4, 5);";

        // Act
        Value result = (Value) runScriptMethod.invoke(null, jsFunction, context, "js");

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(20, result.asInt());
    }

    @Test
    @DisplayName("runScript should handle Python functions")
    void testRunScript_pythonFunction() throws Exception {
        // Arrange
        String pyFunction = "def add(x, y):\n    return x + y\n\nadd(10, 15)";

        // Act
        Value result = (Value) runScriptMethod.invoke(null, pyFunction, context, "python");

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(25, result.asInt());
    }

    // Integration tests combining readFile and runScript

    @Test
    @DisplayName("Should read and execute JavaScript file end-to-end")
    void testIntegration_readAndRunJavaScript() throws Exception {
        // Arrange
        Path jsFile = tempDir.resolve("integration.js");
        Files.writeString(jsFile, "var result = 'Integration test passed'; result;");

        // Act
        String scriptContent = (String) readFileMethod.invoke(null, jsFile.toString());
        Value result = (Value) runScriptMethod.invoke(null, scriptContent, context, "js");

        // Assert
        assertNotNull(scriptContent);
        assertNotNull(result);
        assertTrue(result.isString());
        assertEquals("Integration test passed", result.asString());
    }

    @Test
    @DisplayName("Should read and execute Python file end-to-end")
    void testIntegration_readAndRunPython() throws Exception {
        // Arrange
        Path pyFile = tempDir.resolve("integration.py");
        Files.writeString(pyFile, "message = 'Python integration success'\nmessage");

        // Act
        String scriptContent = (String) readFileMethod.invoke(null, pyFile.toString());
        Value result = (Value) runScriptMethod.invoke(null, scriptContent, context, "python");

        // Assert
        assertNotNull(scriptContent);
        assertNotNull(result);
        assertTrue(result.isString());
        assertEquals("Python integration success", result.asString());
    }

    // Performance and reliability tests

    @Test
    @DisplayName("Should handle multiple script executions efficiently")
    void testPerformance_multipleExecutions() throws Exception {
        // Arrange
        String simpleScript = "1 + 1";

        // Act & Assert
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Value result = (Value) runScriptMethod.invoke(null, simpleScript, context, "js");
            assertEquals(2, result.asInt());
        }
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(duration < 10000, "100 script executions should complete within 10 seconds");
    }

    @Test
    @DisplayName("Context should remain stable across multiple operations")
    void testReliability_contextStability() throws Exception {
        // Act & Assert - Multiple operations should not corrupt context
        Value result1 = (Value) runScriptMethod.invoke(null, "var x = 5; x;", context, "js");
        assertEquals(5, result1.asInt());

        Value result2 = (Value) runScriptMethod.invoke(null, "y = 10\ny", context, "python");
        assertEquals(10, result2.asInt());

        // Context should still work after mixed language operations
        Value result3 = (Value) runScriptMethod.invoke(null, "3 * 7", context, "js");
        assertEquals(21, result3.asInt());
    }
}
