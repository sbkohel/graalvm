/**
 * Test fixtures utility class providing reusable test data and mock objects
 * for the polyglot application test suite.
 */
public class TestFixtures {

    // Valid script content for testing
    public static final String VALID_JAVASCRIPT = "console.log('Hello from JS test');";
    public static final String VALID_PYTHON = "print('Hello from Python test')";
    public static final String VALID_R = "print('Hello from R test')";

    // Invalid/malformed script content for error testing
    public static final String MALFORMED_JAVASCRIPT = "console.log('unclosed string";
    public static final String MALFORMED_PYTHON = "print('test' invalid syntax";
    public static final String SYNTAX_ERROR_JS = "function test() { console.log('missing closing brace');";
    public static final String RUNTIME_ERROR_JS = "var obj = null; obj.nonExistentProperty.call();";
    public static final String RUNTIME_ERROR_PYTHON = "x = 10 / 0";

    // File path scenarios for testing
    public static final String EXISTING_JS_FILE = "scripts/helloWorld.js";
    public static final String EXISTING_PY_FILE = "scripts/helloWorld.py";
    public static final String NON_EXISTENT_FILE = "scripts/missing.js";
    public static final String INVALID_PATH = "/invalid/path/script.js";
    public static final String EMPTY_PATH = "";

    // Test script content with special characters
    public static final String SPECIAL_CHARS_CONTENT = "Special chars: àáâãäåæçèéêë\nUnicode: 🚀🎯⭐\n";

    // Large file content for performance testing
    public static String generateLargeFileContent(int lineCount) {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < lineCount; i++) {
            content.append("Line ").append(i).append("\n");
        }
        return content.toString();
    }

    // Test contexts and configurations
    public static final String[] SUPPORTED_LANGUAGES = {"js", "python", "R"};

    // Expected test results
    public static final String EXPECTED_JS_RESULT = "test result";
    public static final int EXPECTED_MATH_RESULT = 8;
    public static final String EXPECTED_GREETING = "Hello World";

    // Performance test constants
    public static final int MAX_TEST_DURATION_SECONDS = 30;
    public static final int LARGE_FILE_LINE_COUNT = 1000;
    public static final int PERFORMANCE_ITERATIONS = 100;

    // Test data for edge cases
    public static final String WHITESPACE_ONLY_SCRIPT = "   \n\t   \n   ";
    public static final String EMPTY_SCRIPT = "";
    public static final String MEMORY_INTENSIVE_JS =
        "var bigArray = []; " +
        "for (var i = 0; i < 1000000; i++) { " +
        "    bigArray.push('memory consuming string ' + i); " +
        "} " +
        "bigArray.length;";

    /**
     * Creates a temporary script file with the given content
     * @param content The script content
     * @param extension The file extension (js, py, R)
     * @return The path to the created temporary file
     */
    public static String createTempScriptFile(String content, String extension) {
        try {
            java.io.File tempFile = java.io.File.createTempFile("test-script", "." + extension);
            tempFile.deleteOnExit();
            java.nio.file.Files.write(tempFile.toPath(), content.getBytes());
            return tempFile.getAbsolutePath();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to create temporary script file", e);
        }
    }

    /**
     * Creates a GraalVM context for testing with standard configuration
     * @return Configured GraalVM Context
     */
    public static org.graalvm.polyglot.Context createTestContext() {
        return org.graalvm.polyglot.Context.newBuilder(SUPPORTED_LANGUAGES)
                .allowAllAccess(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build();
    }

    /**
     * Utility method to clean up test resources
     * @param context GraalVM context to close
     */
    public static void cleanupContext(org.graalvm.polyglot.Context context) {
        if (context != null) {
            context.close();
        }
    }
}

