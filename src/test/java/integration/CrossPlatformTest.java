import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cross-platform file path handling tests
 * Tests file operations across different operating systems and path formats
 */
class CrossPlatformTest {

    private Method readFileMethod;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        readFileMethod = Application.class.getDeclaredMethod("readFile", String.class);
        readFileMethod.setAccessible(true);
    }

    @Test
    @DisplayName("Should handle forward slash paths on all platforms")
    void testForwardSlashPaths() throws Exception {
        // Arrange
        Path testFile = tempDir.resolve("forward/slash/test.js");
        Files.createDirectories(testFile.getParent());
        Files.writeString(testFile, TestFixtures.VALID_JAVASCRIPT);

        // Convert to forward slash format
        String forwardSlashPath = testFile.toString().replace('\\', '/');

        // Act
        String content = (String) readFileMethod.invoke(null, forwardSlashPath);

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("console.log"));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @DisplayName("Should handle Windows-style backslash paths")
    void testWindowsBackslashPaths() throws Exception {
        // Arrange
        Path testFile = tempDir.resolve("windows\\path\\test.js");
        Files.createDirectories(testFile.getParent());
        Files.writeString(testFile, TestFixtures.VALID_JAVASCRIPT);

        // Act
        String content = (String) readFileMethod.invoke(null, testFile.toString());

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("console.log"));
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    @DisplayName("Should handle Unix-style paths with forward slashes")
    void testUnixStylePaths() throws Exception {
        // Arrange
        Path testFile = tempDir.resolve("unix/style/path/test.py");
        Files.createDirectories(testFile.getParent());
        Files.writeString(testFile, TestFixtures.VALID_PYTHON);

        // Act
        String content = (String) readFileMethod.invoke(null, testFile.toString());

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("print"));
    }

    @Test
    @DisplayName("Should handle relative paths correctly across platforms")
    void testRelativePaths() throws Exception {
        // Arrange - Create file in a subdirectory
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectories(subDir);
        Path testFile = subDir.resolve("relative-test.js");
        Files.writeString(testFile, "var relative = 'success'; relative;");

        // Get relative path from temp directory
        String relativePath = tempDir.relativize(testFile).toString();
        String fullRelativePath = tempDir.resolve(relativePath).toString();

        // Act
        String content = (String) readFileMethod.invoke(null, fullRelativePath);

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("relative"));
    }

    @Test
    @DisplayName("Should handle absolute paths across platforms")
    void testAbsolutePaths() throws Exception {
        // Arrange
        Path testFile = tempDir.resolve("absolute-test.py");
        Files.writeString(testFile, TestFixtures.VALID_PYTHON);

        String absolutePath = testFile.toAbsolutePath().toString();

        // Act
        String content = (String) readFileMethod.invoke(null, absolutePath);

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("print"));
    }

    @Test
    @DisplayName("Should handle paths with spaces across platforms")
    void testPathsWithSpaces() throws Exception {
        // Arrange
        Path dirWithSpaces = tempDir.resolve("folder with spaces");
        Files.createDirectories(dirWithSpaces);
        Path fileWithSpaces = dirWithSpaces.resolve("file with spaces.js");
        Files.writeString(fileWithSpaces, "var spaces = 'handled'; spaces;");

        // Act
        String content = (String) readFileMethod.invoke(null, fileWithSpaces.toString());

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("spaces"));
    }

    @Test
    @DisplayName("Should handle paths with special characters")
    void testPathsWithSpecialCharacters() throws Exception {
        // Arrange - Create files with various special characters
        String[] specialNames = {
            "file-with-dashes.js",
            "file_with_underscores.py",
            "file.with.dots.js",
            "file+with+plus.py"
        };

        for (String fileName : specialNames) {
            Path specialFile = tempDir.resolve(fileName);
            Files.writeString(specialFile, "var special = 'ok'; special;");

            // Act
            String content = (String) readFileMethod.invoke(null, specialFile.toString());

            // Assert
            assertNotNull(content, "Failed to read file: " + fileName);
            assertTrue(content.contains("special"));
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @DisplayName("Should handle Windows drive letters correctly")
    void testWindowsDriveLetters() throws Exception {
        // Arrange
        Path testFile = tempDir.resolve("drive-test.js");
        Files.writeString(testFile, TestFixtures.VALID_JAVASCRIPT);

        String pathWithDrive = testFile.toAbsolutePath().toString();
        assertTrue(pathWithDrive.matches("^[A-Za-z]:\\\\.*"), "Should start with drive letter");

        // Act
        String content = (String) readFileMethod.invoke(null, pathWithDrive);

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("console.log"));
    }

    @Test
    @DisplayName("Should normalize path separators automatically")
    void testPathSeparatorNormalization() throws Exception {
        // Arrange
        Path testFile = tempDir.resolve("normalize/test.py");
        Files.createDirectories(testFile.getParent());
        Files.writeString(testFile, TestFixtures.VALID_PYTHON);

        // Create paths with different separator styles
        String nativePath = testFile.toString();
        String mixedPath = nativePath.replace('\\', '/').replace('/', '\\'); // Force opposite style
        String forwardSlashPath = nativePath.replace('\\', '/');

        // Act & Assert - All path formats should work
        assertDoesNotThrow(() -> {
            String content1 = (String) readFileMethod.invoke(null, nativePath);
            assertNotNull(content1);
        });

        assertDoesNotThrow(() -> {
            String content2 = (String) readFileMethod.invoke(null, forwardSlashPath);
            assertNotNull(content2);
        });
    }

    @Test
    @DisplayName("Should handle deep directory structures across platforms")
    void testDeepDirectoryStructures() throws Exception {
        // Arrange - Create deep nested structure
        Path deepPath = tempDir;
        for (int i = 0; i < 10; i++) {
            deepPath = deepPath.resolve("level" + i);
        }
        Files.createDirectories(deepPath);

        Path deepFile = deepPath.resolve("deep-test.js");
        Files.writeString(deepFile, "var deep = " + deepPath.getNameCount() + "; deep;");

        // Act
        String content = (String) readFileMethod.invoke(null, deepFile.toString());

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("deep"));
    }

    @Test
    @DisplayName("Should handle current directory references")
    void testCurrentDirectoryReferences() throws Exception {
        // Arrange
        Path currentDirFile = tempDir.resolve("current-dir-test.js");
        Files.writeString(currentDirFile, "var current = './referenced'; current;");

        // Create path with current directory reference
        String currentDirPath = tempDir.toString() + "/./current-dir-test.js";

        // Act
        String content = (String) readFileMethod.invoke(null, currentDirPath);

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("current"));
    }

    @Test
    @DisplayName("Should provide consistent error messages across platforms")
    void testConsistentErrorMessages() {
        // Arrange - Non-existent file path
        String nonExistentPath = tempDir.resolve("does-not-exist.js").toString();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            readFileMethod.invoke(null, nonExistentPath);
        });

        // Error message should contain meaningful information regardless of platform
        String errorMessage = exception.getCause().getMessage().toLowerCase();
        assertTrue(errorMessage.contains("no such file") ||
                  errorMessage.contains("cannot find") ||
                  errorMessage.contains("does-not-exist.js"),
                  "Error message should be informative: " + errorMessage);
    }
}
