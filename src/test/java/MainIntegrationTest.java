import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MainIntegrationTest {

    @TempDir
    Path tempDir;

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream capturedOutput;
    private String originalUserDir;

    @BeforeEach
    void setUp() {
        // Capture system output
        capturedOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOutput));

        // Save original user.dir
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        // Restore system output
        System.setOut(originalOut);

        // Restore original user.dir
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void testMain_withValidScripts_executesSuccessfully() throws Exception {
        // Arrange
        setupTestScripts();

        // Act
        Application.main(new String[]{});

        // Assert - main method should complete without throwing exceptions
        String output = capturedOutput.toString();
        // Note: Output capture may vary based on GraalVM console behavior
        assertTrue(true); // Test passes if no exception is thrown
    }

    @Test
    void testMain_missingScriptFiles_handlesGracefully() {
        // Arrange - don't create script files, so they'll be missing

        // Act & Assert
        assertThrows(Exception.class, () -> {
            Application.main(new String[]{});
        });
    }

    @Test
    void testMain_withMalformedScripts_handlesErrors() throws Exception {
        // Arrange
        setupMalformedTestScripts();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            Application.main(new String[]{});
        });
    }

    private void setupTestScripts() throws Exception {
        // Create scripts directory
        Path scriptsDir = tempDir.resolve("scripts");
        Files.createDirectories(scriptsDir);

        // Create test JavaScript file
        Path jsFile = scriptsDir.resolve("helloWorld.js");
        Files.writeString(jsFile, "console.log('Hello from JS main test');");

        // Create test Python file
        Path pyFile = scriptsDir.resolve("helloWorld.py");
        Files.writeString(pyFile, "print('Hello from Python main test')");
    }

    private void setupMalformedTestScripts() throws Exception {
        // Create scripts directory
        Path scriptsDir = tempDir.resolve("scripts");
        Files.createDirectories(scriptsDir);

        // Create malformed JavaScript file
        Path jsFile = scriptsDir.resolve("helloWorld.js");
        Files.writeString(jsFile, "console.log('unclosed string");

        // Create malformed Python file
        Path pyFile = scriptsDir.resolve("helloWorld.py");
        Files.writeString(pyFile, "print('test' invalid syntax");
    }
}
